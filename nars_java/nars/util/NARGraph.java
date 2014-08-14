package nars.util;

import com.syncleus.dann.graph.AbstractDirectedEdge;
import com.syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.util.NARGraph.NAREdge;


/**
 * Stores the contents of some, all, or of multiple NAR memory snapshots.
 *
 * @author me
 */
public class NARGraph extends MutableDirectedAdjacencyGraph<Object, NAREdge> {


    /**
     * determines which NARS term can result in added graph features
     */
    public static interface Filter {

        boolean includePriority(float l);

        boolean includeConcept(Concept c);
    }

    
    public final static Filter IncludeEverything = new Filter() {
        @Override public boolean includePriority(float l) { return true;  }
        @Override public boolean includeConcept(Concept c) { return true;  }
    };
    public final static class ExcludeBelowPriority implements Filter { 

        final float thresh;
        
        public ExcludeBelowPriority(float l) { this.thresh = l;         }        
        @Override public boolean includePriority(float l) { return l >= thresh;  }
        @Override public boolean includeConcept(Concept c) { return true;  }
    };
            
    /**
     * creates graph features from NARS term
     */
    public static interface Graphize {

        /**
         * called at beginning of operation
         * @param g
         * @param time 
         */
        void onTime(NARGraph g, long time);


        /**
         * called per concept
         * @param g
         * @param c 
         */
        void onConcept(NARGraph g, Concept c);
        
        /**
         * called at end of operation
         * @param g 
         */
        void onFinish(NARGraph g);
    }
    
    public abstract static class NAREdge extends AbstractDirectedEdge<Object>    {


        public NAREdge(Object source, Object target) {
            super(source, target);
        }
        
        
    }
            

    public static class TermBelief extends NAREdge  {

        public TermBelief(Object source, Object target) { super(source, target); }
        @Override public String toString() { return "belief"; }        

    }
    public static class TermQuestion extends NAREdge  {

        public TermQuestion(Object source, Object target) {
            super(source, target);
        }
        
        @Override public String toString() { return "question"; }        

    }
    public static class TermDerivation extends NAREdge  {

        public TermDerivation(Object source, Object target) {
            super(source, target);
        }
        @Override public String toString() { return "derives"; }

    }
    public static class TermContent extends NAREdge  {

        public TermContent(Object source, Object target) {
            super(source, target);
        }
        @Override public String toString() { return "has"; }        

    }
    public static class TermType extends NAREdge  {

        public TermType(Object source, Object target) {
            super(source, target);
        }
        @Override public String toString() { return "type"; }        

    }
    public static class SentenceContent extends NAREdge  {

        public SentenceContent(Object source, Object target) {
            super(source, target);
        }
        @Override public String toString() { return "sentence"; }

    }
    public static class TaskInherit extends NAREdge  {

        public TaskInherit(Object child, Object parent) {
            super(child, parent);
        }
        @Override public String toString() { return "task"; }

    }
        
    
    public NARGraph() {
        super();
    }

    public List<Concept> currentLevel = new ArrayList();
    
    public void add(NAR n, Filter filter, Graphize graphize) {
        graphize.onTime(this, n.getTime());

        //TODO support AbstractBag
        AbstractBag<Concept> bag = n.memory.concepts;

        for (Concept c : bag) {
            
            //TODO use more efficient iterator so that the entire list does not need to be traversed when excluding ranges
            
            float p = c.getPriority();
            
            if (!filter.includePriority(p)) continue;

            //graphize.preLevel(this, p);


            if (!filter.includeConcept(c)) continue;

            graphize.onConcept(this, c);

            //graphize.postLevel(this, level);
            
        }
        
        graphize.onFinish(this);

    }

    public boolean addEdge(NAREdge e, boolean allowMultiple) {
        if (!allowMultiple) {            
            Iterator<NAREdge> existing = iterateAdjacentEdges(e.getSourceNode(), e.getDestinationNode());
            if (existing.hasNext())
                return false;            
        }
                
        return add(e);
    }
    
    

    public static class DefaultGraphizer implements Graphize {
        private final boolean includeBeliefs;
        private final boolean includeQuestions;


        public final Set<Term> terms = new HashSet();
        public final Map<Sentence,Term> sentenceTerms = new HashMap();
        
        private final boolean includeTermContent;
        private final boolean includeDerivations;
        private int includeSyntax; //how many recursive levels to decompose per Term
        
        public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, boolean includeSyntax) {
            this(includeBeliefs, includeDerivations, includeQuestions, includeTermContent, includeSyntax ? 2 : 0);
        }
        
        public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, int includeSyntax) {
            this.includeBeliefs = includeBeliefs;
            this.includeQuestions = includeQuestions;
            this.includeTermContent = includeTermContent;
            this.includeDerivations = includeDerivations;
            this.includeSyntax = includeSyntax;
        }
        
        @Override
        public void onTime(NARGraph g, long time) {
            terms.clear();
            sentenceTerms.clear();
        }

        protected void addTerm(NARGraph g, Term t) {
            if (terms.add(t)) {
                g.add(t);
                onTerm(t);
            }
        }
        
        public void onTerm(Term t) {
            
        }
        
        public void onBelief(Sentence kb) {
            
        }

        public void onQuestion(Task q) {
            
        }
        
        @Override
        public void onConcept(NARGraph g, Concept c) {

            g.add(c);
            
            final Term term = c.term;
            addTerm(g, term);

            if (includeBeliefs) {
                for (final Sentence belief : c.beliefs) {
                    onBelief(belief);

                    sentenceTerms.put(belief, term);
                    
                    g.add(belief);
                    g.addEdge(new SentenceContent(belief,term), false);
                    
                    //TODO extract to onBelief                    

                    
                    //TODO check if kb.getContent() is never distinct from c.getTerm()
                    if (term.equals(belief.content))
                        continue;
                    
                    addTerm(g, belief.content);                    
                    g.addEdge(new TermBelief(term, belief.content), false);                    
                }
            }
            
            
            if (includeQuestions) {
                for (final Task q : c.getQuestions()) {
                    if (term.equals(q.getContent()))
                        continue;
                    
                    //TODO extract to onQuestion
                    
                    addTerm(g, q.getContent());
                    
                    //TODO q.getParentBelief()
                    //TODO q.getParentTask()                    
                            
                    g.addEdge(new TermQuestion(term, q.getContent()), false);
                    
                    onQuestion(q);
                }
            }
            
        }
        
        void recurseTermComponents(NARGraph g, CompoundTerm c, int level) {

            for (Term b : c.term) {
                if (!g.contains(b))
                    g.add(b);
                
                
                if (!includeTermContent)
                    g.addEdge(new TermContent(c, b), false);

                if ((level > 1) && (b instanceof CompoundTerm)) {                
                    recurseTermComponents(g, (CompoundTerm)b, level-1);
                }
            }
        }
        
        @Override
        public void onFinish(NARGraph g) {
            if (includeSyntax > 0) {
                for (final Term a : terms) {
                    if (a instanceof CompoundTerm) {
                        CompoundTerm c = (CompoundTerm)a;
                        g.add(c.operator());
                        g.addEdge(new TermType(c.operator(), c), false);

                        if (includeSyntax-1 > 0)
                            recurseTermComponents(g, c, includeSyntax-1);
                    }
                  }            

            }
                        
            if (includeTermContent) {
                for (final Term a : terms) {

                      for (final Term b : terms) {
                          if (a == b) continue;

                          if (a.containsTerm(b)) {
                              g.addEdge(new TermContent(a, b), false);
                          }
                          if (b.containsTerm(a)) {
                              g.addEdge(new TermContent(b, a), false);
                          }
                      }
                  }            

            }
            
            if (includeDerivations && includeBeliefs) {
                for (final Entry<Sentence,Term> s : sentenceTerms.entrySet()) {
                    
                    final Collection<Term> schain = s.getKey().stamp.getChain();
                    final Term derived = s.getValue();

                    for (final Entry<Sentence,Term> t : sentenceTerms.entrySet()) {
                        if (s == t) continue;

                        final Term deriver = t.getValue();
                        if (derived==deriver) //avoid loops
                            continue;

                        final Collection<Term> tchain = s.getKey().stamp.getChain();                        
                        final Sentence deriverSentence = t.getKey();
                        
                        if (schain.contains(deriverSentence.content)) {
                            g.addEdge(new TermDerivation(deriver, derived), false);
                        }
                        if (tchain.contains(derived)) {
                            g.addEdge(new TermDerivation(derived, deriver), false);
                        }
                    }
                }                
            }
        }

        public void setShowSyntax(boolean showSyntax) {
            this.includeSyntax = showSyntax ? 1 : 0;
        }

        
    }
    
    
}
