package nars.nal.meta;

import nars.Global;
import nars.nal.meta.op.Solve;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * http://aleph.sagemath.org/?q=qwssnn
 <patham9> only strong rules are allowing overlap
 <patham9> except union and revision
 <patham9> if you look at the graph you see why
 <patham9> its both rules which allow the conclusion to be stronger than the premises
 */
public enum BeliefFunction implements BinaryOperator<Truth>{

    Revision{
        @Override public Truth apply(Truth T, Truth B) {
            //if (B == null) return null;
            return TruthFunctions.revision(T, B, new DefaultTruth(0, 0));
        }
    },
    StructuralIntersection{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.intersection(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    StructuralDeduction{
        @Override public Truth apply(Truth T, Truth B) {
            //if (B == null) return null;
            return TruthFunctions.deduction(T, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    StructuralAbduction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.abduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    Deduction  {{{boolean add = Solve.allowOverlap.add((BinaryOperator<Truth>) this);}}
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.deduction(T, B);
        }
    },
    Induction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.induction(T, B);
        }
    },
    Abduction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.abduction(T, B);
        }
    },
    Comparison{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.comparison(T, B);
        }
    },
    Conversion{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.conversion(B);
        }
    },
    Negation{
        @Override public Truth apply(Truth T, /* nullable */ Truth B) {
            return TruthFunctions.negation(T);
        }
    },
    Contraposition{
        @Override public Truth apply(Truth T, /* nullable */ Truth B) {
            return TruthFunctions.contraposition(T);
        }
    },
    Resemblance  {{{boolean add = Solve.allowOverlap.add(this);}}
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.resemblance(T,B);
        }
    },
    Union{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.union(T,B);
        }
    },
    Intersection  {{{boolean add = Solve.allowOverlap.add(this);}}
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.intersection(T,B);
        }
    },
    Difference  {{{boolean add = Solve.allowOverlap.add(this);}}
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.difference(T,B);
        }
    },
    Analogy  {{{boolean add = Solve.allowOverlap.add(this);}}
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.analogy(T,B);
        }
    },
    ReduceConjunction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.reduceConjunction(T,B);
        }
    },
    ReduceDisjunction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.reduceDisjunction(T, B);
        }
    },
    ReduceConjunctionNeg{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.reduceConjunctionNeg(T, B);
        }
    },
    AnonymousAnalogy{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.anonymousAnalogy(T,B);
        }
    },
    Exemplification{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.exemplification(T,B);
        }
    },
    DecomposeNegativeNegativeNegative{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativeNegativeNegative(T,B);
        }
    },
    DecomposePositiveNegativePositive{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposePositiveNegativePositive(T,B);
        }
    },
    DecomposeNegativePositivePositive{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativePositivePositive(T,B);
        }
    },
    DecomposePositivePositivePositive{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativePositivePositive(TruthFunctions.negation(T), B);
        }
    },
    DecomposePositiveNegativeNegative{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.decomposePositiveNegativeNegative(T,B);
        }
    },
    Identity{
        @Override public Truth apply(Truth T, /* nullable*/ Truth B) {
            return new DefaultTruth(T.getFrequency(), T.getConfidence());
        }
    },
    BeliefIdentity{
        @Override public Truth apply(Truth T, /* nullable*/ Truth B) {
            if (B == null) return null;
            return new DefaultTruth(B.getFrequency(), B.getConfidence());
        }
    },
    BeliefStructuralDeduction{
        @Override public Truth apply(Truth T, /* nullable*/ Truth B) {
            if (B == null) return null;
            return TruthFunctions.deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    BeliefStructuralDifference{
        @Override public Truth apply(Truth T, /* nullable*/ Truth B) {
            if (B == null) return null;
            Truth res =  TruthFunctions.deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
            return new DefaultTruth(1.0f-res.getFrequency(), res.getConfidence());
        }
    },
    BeliefNegation{
        @Override public Truth apply(Truth T, /* nullable*/ Truth B) {
            if (B == null) return null;
            return TruthFunctions.negation(B);
        }
    }
    ;


    static final Map<Term, BeliefFunction> atomToTruthModifier = Global.newHashMap(BeliefFunction.values().length);

    static {
        for (BeliefFunction tm : BeliefFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

    public static BeliefFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }
    @Override
    public Truth apply(Truth truth, Truth truth2) {
        return null;
    }
    @Override
    public <V> BiFunction<Truth, Truth, V> andThen(Function<? super Truth, ? extends V> after) {
        return null;
    }
}
