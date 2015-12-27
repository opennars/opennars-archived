package nars.term.compile;

import javassist.scopedpool.SoftValueHashMap;
import nars.*;
import nars.bag.impl.CacheBag;
import nars.nal.Compounds;
import nars.nal.LocalRules;
import nars.nal.PremiseAware;
import nars.nal.RuleMatch;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.transform.CompoundTransform;
import nars.term.transform.FindSubst;
import nars.term.transform.MapSubst;
import nars.term.transform.Subst;
import nars.util.WeakValueHashMap;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 *
 */
public interface TermIndex extends Compounds, CacheBag<Termed, Termed> {


    /**
     * The task and belief have the same content
     * <p>
     * called in RuleTables.reason
     *
     * @param question The task
     * @param solution The belief
     * @return null if no match
     */
    static Task match(Task question, Task solution, NAR nar, Consumer<Task> eachSolution) {

        if (question.isQuestion() || question.isGoal()) {
            if (Tense.matchingOrder(question, solution)) {
                Term[] u = {question.term(), solution.term()};
                unify(Op.VAR_QUERY, u, nar.memory.random, (st) -> {
                    Task s;
                    if (!st.equals(solution.term())) {
                        s = MutableTask.clone(solution).term((Compound)st);
                    } else {
                        s = solution;
                    }
                    LocalRules.trySolution(question, s, nar, eachSolution);
                });
            }
        }

        return solution;
    }

    void forEach(Consumer<? super Termed> c);

    default <T extends Termed> T getTerm(Termed t) {
        Termed u = get(t);
        if (u == null)
            return null;
        return (T)u.term();
    }

    class UncachedTermIndex implements TermIndex {

        /** build a new instance on the heap */
        @Override public Termed compile(Op op, Term[] t, int relation) {
            return new UncachedGenericCompound(op, t, relation);
        }

        @Override
        public Termed get(Object key) {
            return (Termed)key;
        }

        @Override
        public void forEach(Consumer<? super Termed> c) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Object remove(Termed key) {
            throw new RuntimeException("n/a");
        }

        @Override
        public Termed put(Termed termed, Termed termed2) {
            throw new RuntimeException("n/a");
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void start(Memory n) {
            throw new RuntimeException("should not be used by Memory");
        }

        private class UncachedGenericCompound extends GenericCompound {

            public UncachedGenericCompound(Op op, Term[] t, int relation) {
                super(op, t, relation);
            }

//            @Override
//            public Term clone(Term[] replaced) {
//                if (subterms().equals(replaced))
//                    return this;
//                return get(op(), replaced, relation);
//            }
        }
    }

//    class GuavaIndex extends GuavaCacheBag<Term,Termed> implements TermIndex {
//
//
//        public GuavaIndex(CacheBuilder<Term, Termed> data) {
//            super(data);
//        }
//
//        @Override
//        public void forEachTerm(Consumer<Termed> c) {
//            data.asMap().forEach((k,v) -> {
//                c.accept(v);
//            });
//        }
//
//
//
//    }

    /** default memory-based (Guava) cache */
    static TermIndex memory(int capacity) {
//        CacheBuilder builder = CacheBuilder.newBuilder()
//            .maximumSize(capacity);
        return new MapIndex(
            new HashMap(capacity),new HashMap(capacity*2)
            //new UnifriedMap()
        );
    }
    static TermIndex memorySoft(int capacity) {
        return new MapIndex(
                new SoftValueHashMap(capacity),
                new SoftValueHashMap(capacity*2)
        );
    }
    static TermIndex memoryWeak(int capacity) {
        return new MapIndex(
                new WeakValueHashMap(capacity),
                new WeakValueHashMap(capacity*2)
        );
    }
    default void print(PrintStream out) {
        forEach(out::println);
    }

//    /** for long-running processes, this uses
//     * a weak-value policy */
//    static TermIndex memoryAdaptive(int capacity) {
//        CacheBuilder builder = CacheBuilder.newBuilder()
//            .maximumSize(capacity)
//            .recordStats()
//            .weakValues();
//        return new GuavaIndex(builder);
//    }

    default Termed transform(Compound src, CompoundTransform t, boolean requireEqualityForNewInstance) {
        if (t.testSuperTerm(src)) {

            Term[] cls = new Term[size()];

            int mods = transform(src, t, cls, 0);

            if (mods == -1) {
                return null;
            }
            else if (!requireEqualityForNewInstance || (mods > 0)) {
                return clone(src, cls);
            }
            //else if mods==0, fall through:
        }
        return src; //nothing changed
    }

    default Term clone(Compound src, Term[] cls) {
        return get(src.op(), cls, src.relation());
    }


    /** returns how many subterms were modified, or -1 if failure (ex: results in invalid term) */
    default int transform(Compound src, CompoundTransform trans, Term[] target, int level) {
        int n = size();

        int modifications = 0;

        Compound parent = (Compound)(src.term());

        for (int i = 0; i < n; i++) {
            Term x = src.term(i).term();

            if (x == null)
                throw new RuntimeException("null subterm");

            if (trans.test(x)) {

                Term y = trans.apply( parent, x, level);
                if (y == null)
                    return -1;

                if (x!=y) {
                    modifications++;
                    x = y;
                }

            } else if (x instanceof Compound) {
                //recurse
                Compound cx = (Compound) x;
                if (trans.testSuperTerm(cx)) {

                    Term[] yy = new Term[cx.size()];
                    int submods = clone(cx, trans, yy, level + 1);

                    if (submods == -1) return -1;
                    if (submods > 0) {
                        x = clone(cx, yy);
                        modifications+= (cx!=x) ? 1 : 0;
                    }
                }
            }
            target[i] = x;
        }

        return modifications;
    }

    /** returns the resolved term according to the substitution    */
    @Override default Term apply(Compound src, Subst f, boolean fullMatch) {

        Term y = f.getXY(this);
        if (y!=null)
            return y;

        int len = size();
        List<Term> sub = Global.newArrayList(len /* estimate */);

        for (int i = 0; i < len; i++) {
            Term t = src.term(i);
            if (!t.applyTo(f, sub, fullMatch)) {
                if (fullMatch)
                    return null;
            }
        }

        Term result = apply(src, sub);

        //apply any known immediate transform operators
        if (Op.isOperation(result)) {
            ImmediateTermTransform tf = f.getTransform(Operator.operatorTerm((Compound)result));
            if (tf!=null) {
                return applyImmediateTransform(src, f, result, tf);
            }
        }

        return result;
    }

    default <X extends Compound> X transform(Compound src, CompoundTransform t) {
        return transform(src, t, true);
    }


    static Term applyImmediateTransform(Subst f, Term result, ImmediateTermTransform tf) {

        //Compound args = (Compound) Operator.opArgs((Compound) result).apply(f);
        Compound args = Operator.opArgs((Compound) result);

        if ((tf instanceof PremiseAware) && (f instanceof RuleMatch)) {
            return ((PremiseAware)tf).function(args, (RuleMatch)f);
        } else {
            return tf.function(args);
        }

    }

    /**
     * To unify two terms
     *
     * @param varType The varType of variable that can be substituted
     * @param t       The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     * <p>
     * only sets the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
     */
    static void unify(Op varType, Term[] t, Random random, Consumer<Term> solution) {

        FindSubst f = new FindSubst(varType, random) {

            @Override public boolean onMatch() {

                //TODO combine these two blocks to use the same sub-method

                Term a = t[0];
                Term aa = a;

                //FORWARD
                if (a instanceof Compound) {

                    aa = a.applyOrSelf(this);

                    if (aa == null) return false;

                    Op aaop = aa.op();
                    if (a.op() == Op.VAR_QUERY && (aaop == Op.VAR_INDEP || aaop == Op.VAR_DEP))
                        return false;

                }

                Term b = t[1];
                Term bb = b;

                //REVERSE
                if (b instanceof Compound) {
                    bb = applySubstituteAndRenameVariables(
                            ((Compound) b),
                            (Map<Term, Term>)yx //inverse map
                    );

                    if (bb == null) return false;

                    Op bbop = bb.op();
                    if (b.op() == Op.VAR_QUERY && (bbop == Op.VAR_INDEP || bbop == Op.VAR_DEP))
                        return false;
                }

                t[0] = aa;
                t[1] = bb;

                solution.accept(t[1]);

                return false; //just the first
            }

            Term applySubstituteAndRenameVariables(Compound t, Map<Term,Term> subs) {
                if ((subs == null) || (subs.isEmpty())) {
                    //no change needed
                    return t;
                }
                return t.apply(new MapSubst(subs));
            }

        };
        f.matchAll(t[0], t[1]);
    }

}
