package nars.prolog;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.Events;
import nars.Events.*;
import nars.Global;
import nars.NAR;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal3.SetTensional;
import nars.nal.nal4.Product;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.stamp.Stamp;
import nars.nal.term.*;
import nars.nal.term.Term;
import nars.tuprolog.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * Causes a NARProlog to mirror certain activity of a NAR.  It generates
 * prolog terms from NARS beliefs, and answers NARS questions with the results
 * of a prolog solution (converted to NARS terms), which are input to NARS memory
 * with the hope that this is sooner than NARS can solve it by itself.
 */
public class NARPrologMirror extends AbstractMirror {

    public final NAR nar;
    public final NARTuprolog prolog;



    private float trueThreshold = 0.90f;
    private float falseThreshold = 0.10f;
    private float confidenceThreshold;
    private final Map<Sentence, nars.tuprolog.Term> beliefs = new HashMap();

    //counter of how many beliefs support the term. added and removed for each belief
    private final ObjectIntHashMap<Term> beliefCount = new ObjectIntHashMap<>();

    private boolean eternalJudgments = true;
    private boolean presentJudgments = false;

    /**
     * how much to scale the memory's duration parameter for this reasoner's "now" duration; default=1.0
     */
    float durationMultiplier = 1.0f;
    float answerConfidence = 0.5f;

    /**
     * how often to remove temporally irrelevant beliefs
     */
    @Deprecated
    float forgetCyclePeriod; ///TODO use a Map<Long,belief> indexed by expiration time, so they can be removed efficiently
    private long lastFlush;
    private int durationCycles;

    static boolean allTerms = false;

    /**
     * in seconds
     */
    float maxSolveTime;
    float minSolveTime;

    /**
     * max # answers returned in response to a question
     */
    int maxAnswers = 32;

    boolean reportAssumptions = false;
    boolean reportForgets = false;
    boolean reportAnswers = false;


    public static final Class[] telepathicEvents = {
            Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class,
            Events.ConceptQuestionAdd.class,
            IN.class, OUT.class, Answer.class
    };

    public static final Class[] inputOutputEvents = {IN.class, OUT.class};
    private InputMode inputMode = InputMode.DirectProcess;

    //serial #'s
    static long nextQueryID = 0;
    static long variableContext = 0;

    public NARPrologMirror(NAR nar, float minConfidence, float answerConfidence, boolean telepathic, boolean eternalJudgments, boolean presentJudgments) throws InvalidLibraryException {
        super(nar, true, telepathic ? telepathicEvents : inputOutputEvents);
        this.nar = nar;
        this.confidenceThreshold = minConfidence;
        this.answerConfidence = answerConfidence;
        this.prolog = new NARTuprolog(nar);
        this.forgetCyclePeriod = nar.memory.duration() / 2;

        this.maxSolveTime = 70.0f * 1e-3f;
        this.minSolveTime = 20f * 1e-3f;

//        this.maxSolveTime = 0;
//        this.minSolveTime = 0;

        List<String> l = initAxioms();
        for (String axiom : l) {
            Struct clause = (Struct) new Parser(axiom).nextTerm(true);
            addTheory(clause);
        }
//        String axiomString = Joiner.on(" \n").join(l);
//        try {
//            nars.tuprolog.Term[] ax = toArray(new Theory(axiomString + '\n').iterator(prolog.prolog), nars.tuprolog.Term.class);
////            nars.tuprolog.Term[] ax = Lists.transform(l, x -> new Struct(x)).toArray(new nars.tuprolog.Term[l.size()]);
//            //axioms = new Theory(ax);
//            //prolog.setTheory(axioms);
//        } catch (InvalidTheoryException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }


        setTemporalMode(eternalJudgments, presentJudgments);
    }

    public ClauseInfo retractFact(Struct fact) {
        return prolog.prolog.getTheories().retract(fact);
    }

    public boolean assertFact(Struct fact) {
        try {
            boolean b = prolog.prolog.getTheories().assertA(fact, true, null, true);
            return b;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addTheory(Struct clause) {
        try {
            SolveInfo s = prolog.prolog.addTheory(clause);
            System.err.println("Prolog theory: " + clause + " (" + s +")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public NARPrologMirror setInputMode(InputMode i) {
        this.inputMode = i;
        return this;
    }

    public NARPrologMirror setTemporalMode(boolean eternalJudgments, boolean presentJudgments) {
        this.eternalJudgments = eternalJudgments;
        this.presentJudgments = presentJudgments;
        return this;
    }

    boolean validTemporal(Sentence s) {
        long e = s.getOccurrenceTime();

        if (eternalJudgments && (e == Stamp.ETERNAL))
            return true;

        if (presentJudgments) {
            long now = nar.time();
            if (TemporalRules.concurrent(now, e, (int) (durationCycles * durationMultiplier)))
                return true;
        }

        return false;
    }

    public void setReportAnswers(boolean reportAnswers) {
        this.reportAnswers = reportAnswers;
    }

    public void setReportAssumptions(boolean reportAssumptions) {
        this.reportAssumptions = reportAssumptions;
    }

    public Map<Sentence, nars.tuprolog.Term> getBeliefs() {
        return beliefs;
    }

    protected void beliefsChanged() {
    }

    protected boolean forget(Sentence belief) {

        nars.tuprolog.Term removed;
        if ((removed = beliefs.remove(belief)) != null) {

            if (beliefCount.addToValue(belief.getTerm(), -1) == 0) {
                if (!apply("retract", removed))
                    throw new RuntimeException("Could not retract: " + belief + " === " + removed);

                beliefsChanged();

                if (reportForgets) {
                    System.err.println("Prolog forget: " + belief);
                }

            }


            return true;
        }
        return false;
    }

    public boolean apply(String functor, String term) throws NoMoreSolutionException, NoSolutionException, InvalidTheoryException {
        return apply(functor, new Parser(term).nextTerm(true));
    }

    public boolean apply(String functor, nars.tuprolog.Term removed)  {
        Struct r = new Struct(functor,removed);
        r.resolveTerm();
        System.out.println("Prolog apply: " + r);
        try {
            SolveInfo solution = solve(r);
            if (solution==null) return false;
            return solution.isSuccess();
        }
        catch (Exception e) {
            e.printStackTrace();;
        }
        return false;
    }

    public SolveInfo solve(Struct r) throws NoMoreSolutionException, NoSolutionException, InvalidTheoryException {
        return solve(r, 0, x -> {
            System.err.println(x);
        });

    }

    protected void updateBeliefs() {
        if (presentJudgments) {
            long now = nar.time();
            durationCycles = (nar.param).duration.get();
            if (now - lastFlush > (long) (durationCycles / forgetCyclePeriod)) {

                Set<Sentence> toRemove = new HashSet();
                for (Sentence s : beliefs.keySet()) {
                    if (!validTemporal(s)) {
                        toRemove.add(s);
                    }
                }
                for (Sentence s : toRemove) {
                    forget(s);
                }

                lastFlush = now;
            }
        }
    }

    @Override
    public void event(final Class channel, final Object... arg) {

        if (channel == ConceptBeliefAdd.class) {
            Concept c = (Concept) arg[0];
            Task task = (Task) arg[1];
            add(task);
        } else if (channel == ConceptBeliefRemove.class) {
            Concept c = (Concept) arg[0];
            remove(c, (Sentence) arg[1]);
        } else if (channel == Events.ConceptQuestionAdd.class) {
            Concept c = (Concept) arg[0];
            Task task = (Task) arg[1];
            add(task);
        } else if ((channel == IN.class) || (channel == OUT.class) || (channel == Answer.class)) {
            Object o = arg[0];
            if (o instanceof Task) {
                Task task = (Task) o;
                add(task);
            }
        }
    }

    /**
     * remove belief unless there are other similar beliefs remaining in 'c'
     */
    private void remove(Concept c, Sentence forgotten) {
        for (Task x : c.beliefs) {
            if (x.equals(forgotten)) continue;
            if (believable(x.getTruth()) && similarTruth(x.getTruth(), forgotten.truth)
                    && similarTense(x.sentence, forgotten)) {
                //there still remains evidence for this belief in the concept
                return;
            }
        }

        remove(forgotten, null);
    }

    protected void remove(Sentence s, Task task) {
        //TODO
    }

    protected void add(Task task) {
        Sentence s = task.sentence;

        variableContext = s.term.hashCode();

        if (!(s.term instanceof Compound))
            return;

        if (!validTemporal(s))
            return;

        updateBeliefs();

        //only interpret input judgments, or any kind of question
        if (s.isJudgment()) {

            processBelief(s, task, true);
        } else if (s.isQuestion()) {


            onQuestion(s);

            float priority = task.getPriority();
            float solveTime = ((maxSolveTime - minSolveTime) * priority) + minSolveTime;

            if (beliefCount.containsKey(s.getTerm())) {
                //TODO search for opposite belief

                //already determined it to be true
                answer(task, s.term, null);
                return;
            }

            try {
                Struct qh = newQuestion(s);

                if (qh != null) {

                    //System.err.println("Prolog asked: " + s + " === " + qh);

                    solve(qh, solveTime, solution -> {

                        try {
                            Term n = nterm(solution);
                            if (n != null && (n instanceof Compound)) {
                                if (matchingSolution(s.getTerm(), (Compound)n))
                                    answer(task, n, solution);
                            }
                        } catch (Exception e) {
                            //problem generating a result
                            e.printStackTrace();
                        }

                    });

                } else {
                    onUnrecognizable(s);
                }
            } catch (NoSolutionException nse) {
                //no solution, ok
            } catch (InvalidTermException nse) {
                nar.emit(NARPrologMirror.class, s + " : not supported yet");
                nse.printStackTrace();
            } catch (Exception ex) {
                nar.emit(ERR.class, ex.toString());
                ex.printStackTrace();
            }

        }

    }

    private boolean matchingSolution(Compound a, Compound b) {
        if (a.hasVar()) {
            //handle variables
        }
        else {
            return a.equals(b);
        }
        return false;
    }

    public SolveInfo solve(Struct qh, float solveTime, Consumer<nars.tuprolog.Term> withSolution) throws NoMoreSolutionException, NoSolutionException, InvalidTheoryException {
        return solve(qh, solveTime, withSolution, maxAnswers);
    }

    public SolveInfo solve(Struct qh, float solveTime, Consumer<nars.tuprolog.Term> withSolution, int maxAnswers) throws InvalidTheoryException, NoSolutionException, NoMoreSolutionException {
        //System.out.println("Prolog question: " + s.toString() + " | " + qh.toString() + " ? (" + Texts.n2(priority) + ")");


        SolveInfo si = prolog.query(qh, solveTime);


        int answers = 0;

        nars.tuprolog.Term lastSolution = null;

        long start = System.currentTimeMillis();

        try {
            do {
                if (si == null) break;

                nars.tuprolog.Term solution = si.getSolution();
                if (solution == null)
                    break;

                if (lastSolution != null && solution.equals(lastSolution))
                    continue;

                lastSolution = solution;

                withSolution.accept(solution);

                try {
                    si = prolog.prolog.solveNext(solveTime);
                }
                catch (NoMoreSolutionException e) {
                    break;
                }

                long now = System.currentTimeMillis();
                double timeDiff = (now - start) * 1e-3f;
                if (timeDiff > solveTime)
                    break;
            }
            while ((answers++) < maxAnswers);
        }
        catch (NoSolutionException nse) {
            si = null;
        }
        finally {
            prolog.prolog.solveEnd();
        }


        return si;

    }

    protected void onUnrecognizable(Sentence s) {
        System.err.println(this + " unable to express question in Prolog: " + s);
    }

    protected void onUnanswerable(nars.tuprolog.Term solution) {
        System.err.println(this + " unable to answer solution: " + solution);

    }

    protected void processBelief(Sentence s, Task task, boolean addOrRemove) {

        Truth tv = s.truth;
        if (believable(tv)) {

            boolean exists = beliefs.containsKey(s);
            if ((addOrRemove) && (exists))
                return;
            else if ((!addOrRemove) && (!exists))
                return;

            try {
                Struct th = newJudgmentTheory(s);
                if (th != null) {

                    if (tv.getFrequency() < falseThreshold) {
                        th = negation(th);
                    }

                    if (addOrRemove) {
                        if (beliefs.putIfAbsent(s, th) == null) {

                            if (beliefCount.addToValue(s.getTerm(), 1) == 1) {

                                if (reportAssumptions)
                                    System.err.println("Prolog fact: " + th + " | " + s);

                                if (!assertFact(th))
                                    throw new RuntimeException("Unable to fact: " + s + " === " + th);

                                beliefsChanged();

                            }

                        }
                    } else {
                        forget(s);
                    }

                }
            } catch (Exception ex) {
                nar.emit(ERR.class, ex.toString());
            }
        }


    }

    /**
     * creates a theory from a judgment Statement
     */
    public static Struct newJudgmentTheory(final Sentence judgment) throws InvalidTheoryException {

        nars.tuprolog.Term s;
        /*if (judgment.truth!=null) {            
            s = pInfer(pterm(judgment.content), judgment.truth);
        }
        else {*/
        try {
            s = pterm(judgment.term);
            if (s!=null)
                s.resolveTerm();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //}

        return (Struct) s;
    }

    Struct newQuestion(final Sentence question) {
        Struct s = (Struct)pterm(question.term);
        if (s!=null) {
            //introduce variables to make the query work
            Compound ct = question.getTerm();
            if (!question.getTerm().hasVar()) {

                //introduce variable into the term with the biggest complexity, because it is more specific in matching
                int biggest = 0;
                int biggestSize = 0;
                int j = 0;
                for (Term t : ct.term) {
                    int c = t.getComplexity();
                    if (c > biggestSize) {
                        biggestSize = c;
                        biggest = j;
                    }
                    j++;
                }
                s.replaceSubtermWithVariable(biggest);
            }
            s.resolveTerm();
            return  s;
        }
        return null;
    }

    //NOT yet working
    public Struct pInfer(nars.tuprolog.Term t, Truth tv) {
        double freq = tv.getFrequency();
        double conf = tv.getConfidence();
        Struct lt = new Struct(new nars.tuprolog.Term[]{t,
                new Struct(new nars.tuprolog.Term[]{
                        new nars.tuprolog.Double(freq),
                        new nars.tuprolog.Double(conf)
                })
        });
        return new Struct("infer", lt);
        //TODO resolveTerm
    }

    public static Struct negation(nars.tuprolog.Term t) {
        return new Struct("not", t);
    }

    public static String pquote(final String x) {
        return "'" + x + '\'';
    }

    public static String pescape(final String p) {
        if (!Parser.isAtom(p)) {
            return pquote(p);
        }
        if (Character.isDigit(p.charAt(0))) {
            return pquote(p);
        }
        return p;
    }

    public static String unpescape(String p) {
        return p.toString();
    }

    public boolean believable(Truth tv) {
        return (tv.getConfidence() >= confidenceThreshold) && ((tv.getFrequency() >= trueThreshold) || (tv.getFrequency() < falseThreshold));
    }

    public boolean similarTense(Sentence a, Sentence b) {
        boolean ae = a.isEternal();
        boolean be = b.isEternal();
        if (ae && be) return true;
        else if (ae && !be) return false;
        else if (!ae && be) return false;
        else {
            return (TemporalRules.concurrent(a.getOccurrenceTime(), b.getOccurrenceTime(), nar.memory.duration()));
        }
    }


    public boolean similarTruth(Truth a, Truth b) {
        float af = a.getFrequency();
        float bf = b.getFrequency();
        if ((af < falseThreshold) && (bf < falseThreshold))
            return true;
        if ((af > trueThreshold) && (bf > trueThreshold))
            return true;
        return false;
    }

    protected static String classPredicate(final Class c) {
        String s = c.getSimpleName();
        switch (s) {
            case "SetInt1":
            case "SetIntN":
                s = "setint";
                break;
            case "SetExt1":
            case "SetExtN":
                s = "setext";
                break;
        }
        return s.toLowerCase();
    }

    //NARS term -> Prolog term
    public static nars.tuprolog.Term pterm(final Term term) {

        //CharSequence s = termString(term);
        if (term instanceof Statement) {
            Statement i = (Statement) term;
            String predicate = classPredicate(i.getClass());
            nars.tuprolog.Term subj = pterm(i.getSubject());
            nars.tuprolog.Term obj = pterm(i.getPredicate());
            if ((subj != null) && (obj != null))
                return new Struct(predicate, subj, obj);
        } else if ((term instanceof SetTensional) || (term instanceof Product) /* conjunction */) {
            Compound s = (Compound) term;
            String predicate = classPredicate(s.getClass());
            nars.tuprolog.Term[] args = pterms(s.term);
            if (args != null)
                return new Struct(predicate, args);
        }
        //Image...
        //Conjunction...
        else if (term instanceof Negation) {
            nars.tuprolog.Term np = pterm(((Negation) term).term[0]);
            if (np == null) return null;
            return new Struct("not", np);
        } else if (term.getClass().equals(Variable.class)) {
            return getVariable((Variable) term);
        } else if (term.getClass().equals(Atom.class)) {
            return new Struct(pescape(term.toString()));
        } else if (term instanceof Compound) {
            //unhandled type of compound term, store as an atomic string            
            //NOT ready yet
            if (allTerms) {
                return new Struct('_' + pescape(term.toString()));
            }
        }

        return null;
    }

    private static Term getVar(Var v) {
        //assume it is a dependent variable
        return new Variable('#' + v.getName());
    }


    private static Var getVariable(Variable v) {
        if (v.hasVarIndep())
            return new Var('I' + v.getIdentifier());
        if (v.hasVarQuery())
            return new Var("Q" + nextQueryID++);
        if (v.hasVarDep()) //check this
            return new Var("D" + (variableContext) + '_' + v.getIdentifier());
        return null;
    }

    /**
     * Prolog term --> NARS statement
     */
    public static Term nterm(final nars.tuprolog.Term term) {

        if (term instanceof Struct) {
            Struct s = (Struct) term;
            int arity = s.getArity();
            String predicate = s.getName();
            if (arity == 0) {
                return Atom.get(unpescape(predicate));
            }
            if (arity == 1) {
                switch (predicate) {
                    case "not":
                        return Negation.make(nterm(s.getTerms(0)));
//                    default:
//                        throw new RuntimeException("Unknown 1-arity nars predicate: " + predicate);
                }
            }
            switch (predicate) {
                case "product":
                    Term[] a = nterm(s.getTerms());
                    if (a != null) return Product.make(a);
                    else return null;
                case "setint":
                    Term[] b = nterm(s.getTerms());
                    if (b != null) return SetInt.make(b);
                    else return null;
                case "setext":
                    Term[] c = nterm(s.getTerms());
                    if (c != null) return SetExt.make(c);
                    else return null;

            }

            if (arity == 2) {
                Term a = nterm(s.getTerms(0));
                Term b = nterm(s.getTerms(1));
                if ((a != null) && (b != null)) {
                    switch (predicate) {
                        case "inheritance":
                            return Inheritance.make(a, b);
                        case "similarity":
                            return Similarity.make(a, b);
                        case "implication":
                            return Implication.make(a, b);
                        case "equivalence":
                            return Equivalence.makeTerm(a, b);
                        //TODO more types
//                        default:
//                            throw new RuntimeException("Unknown 2-arity nars predicate: " + predicate);


                    }
                }
            }
            System.err.println("nterm() does not yet support translation to NARS terms of Prolog: " + term);
        } else if (term instanceof Var) {
            Var v = (Var) term;
            nars.tuprolog.Term t = v.getTerm();
            if (t != v) {
                //System.out.println("Bound: " + v + " + -> " + t + " " + nterm(t));
                return nterm(t);
            } else {
                //System.out.println("Unbound: " + v);
                //unbound variable, is there anything we can do with it?
                return getVar(v);
            }
        } else if (term instanceof nars.tuprolog.Number) {
            nars.tuprolog.Number n = (nars.tuprolog.Number) term;
            return Atom.get('"' + String.valueOf(n.doubleValue()) + '"');
        }

        return null;
    }


    public Sentence getBeliefSentence(Sentence question, Term belief, Task parentTask, float conf) {
        float freq = 1.0f;
        float priority = Global.DEFAULT_JUDGMENT_PRIORITY;
        float durability = Global.DEFAULT_JUDGMENT_DURABILITY;
        Tense tense = question.isEternal() ? Tense.Eternal : Tense.Present;

        //TODO use derivation of prolog result to create a correct stamp

        return new Sentence(belief, '.', new Truth(freq, conf),
                new Stamp(nar.memory, tense));
    }

    /**
     * reflect a result to NARS, and remember it so that it doesn't get reprocessed here later
     */
    public Term answer(Task question, Term t, nars.tuprolog.Term pt) {
        if (reportAnswers)
            System.err.println("Prolog answer: " + t);

        Sentence a = getBeliefSentence(question.sentence, t, question, answerConfidence);

        input(a, inputMode, question);

//        if (pt != null) {
//            beliefs.put(a, pt);
//            beliefsChanged();
//        }

        return t;
    }

    /*
    public static class NARStruct extends Struct {
        
        Sentence sentence = null;

        public NARStruct(Sentence sentence, String predicate, nars.prolog.Term[] args) {
            super(predicate, args);
            
            this.sentence = sentence;
        }
        
        public NARStruct(String predicate, nars.prolog.Term... args) {
            this(null, predicate, args);
        }

        public Sentence getSentence() {
            return sentence;
        }

        public void setSentence(Sentence sentence) {
            this.sentence = sentence;
        }
        
        
    }
    */


    public List<String> initAxioms() {
        List<String> l = new ArrayList();

        l.add("inheritance(A, C) :- inheritance(A, B), inheritance(B,C).");
        //l.add("inheritance(A, C) :- inheritance(A,B),inheritance(B,C).");
        l.add("similarity(A, B) :- inheritance(A,B), inheritance(B,A).");
        //l.add("inheritance(A,B),inheritance(B,A) :- similarity(A, B).");
        l.add("implication(A, C) :- implication(A,B),implication(B,C).");
        l.add("similarity(A, B) :- A\\=B, similarity(B,A).");
        //l.add("not(similar(A, B)) :- not(inheritance(A,B)),inheritance(B,A).");
        l.add("equivalence(A, B) :- A\\=B, equivalence(B,A).");


        //l.add("similarity(A, B) :- equivalence(A,B).");
        //l.add("not(equivalence(A, B)) :- not(similar(A,B)).");

        //l.add("A :- negation(negation(A)).");

        //        l.add("connected(A,B) :- product(A, B).");
//        l.add("connected(A,B) :- similarity(A, B).");
//        l.add("connected(A,B) :- equivalence(A, B).");
//        l.add("connected(A,B) :- conjunction(A, B).");
//        l.add("connected(A,B) :- setint(A, B).");
//        l.add("connected(A,B) :- setext(A, B).");
//        l.add("connected(B,A) :- connected(A,B).");
        l.add("[A] :- product(A).");
        l.add("[A,B] :- product(A,B).");
        l.add("[A,B,C] :- product(A,B,C).");

        l.add("subject(S) :- inheritance(S,P).");
        l.add("predicate(P) :- inheritance(S,P).");
        return l;
    }


    public static Theory getTheory(Map<Sentence, nars.tuprolog.Term> beliefMap) throws InvalidTheoryException {
        return new Theory(new Struct(beliefMap.values().toArray(new Struct[beliefMap.size()])));
    }

    public Theory getBeliefsTheory() throws InvalidTheoryException {
        return prolog.prolog.getDynamicTheoryCopy();
    }

    protected void onQuestion(Sentence s) {
    }

    public static nars.tuprolog.Term[] pterms(Term[] term) {
        nars.tuprolog.Term[] tt = new nars.tuprolog.Term[term.length];
        int i = 0;
        for (Term x : term) {
            if ((tt[i++] = pterm(x).resolveTerm()) == null) return null;
        }
        return tt;
    }

    public static Term[] nterm(final nars.tuprolog.Term[] term) {
        Term[] tt = new Term[term.length];
        int i = 0;
        for (nars.tuprolog.Term x : term) {
            if ((tt[i++] = nterm(x)) == null) return null;
        }
        return tt;
    }


    public boolean solve(String s, float maxTime, Consumer<nars.tuprolog.Term> withSolution) throws InvalidTheoryException {
        return solve(s, maxTime, withSolution, maxAnswers);
    }
    public boolean solve(String s, float maxTime, Consumer<nars.tuprolog.Term> withSolution, int maxAnswers) throws InvalidTheoryException {
        if (!s.endsWith(".")) s = s + '.';
        nars.tuprolog.Term x = new Parser(s).nextTerm(true);

        if (x == null) return false;

        try {
            SolveInfo si = solve((Struct) x, maxTime, withSolution, maxAnswers);
            return true;
        } catch (NoSolutionException e) {
            return false;

        } catch (NoMoreSolutionException e) {
            return true;
        }

    }
}

