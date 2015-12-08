package nars.nal.meta;

import nars.Global;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * Created by me on 8/1/15.
 */
public enum DesireFunction implements BinaryOperator<Truth> {

    Negation{
        @Override public Truth apply(Truth T, Truth B) {
            return TruthFunctions.negation(T); }
    },

    Strong{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.desireStrong(T,B);
        }
    },
    Weak{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.desireWeak(T, B);
        }
    },
    Induction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B == null) return null;
            return TruthFunctions.desireInd(T,B);
        }
    },
    Deduction{
        @Override public Truth apply(Truth T, Truth B) {
            if (B==null) return null;
            return TruthFunctions.desireDed(T,B);
        }
    },
    Identity{
        @Override public Truth apply(Truth T, /* N/A: */ Truth B) {
            return new DefaultTruth(T.getFrequency(), T.getConfidence());
        }
    },
    StructuralStrong{
        @Override public Truth apply(Truth T, Truth B) {
            return TruthFunctions.desireStrong(T, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    }
    ;



    static final Map<Term, DesireFunction> atomToTruthModifier = Global.newHashMap(DesireFunction.values().length);

    static {
        for (DesireFunction tm : DesireFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

 
    public static DesireFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }

    /**
     * @param T taskTruth
     * @param B beliefTruth (possibly null)
     * @return
     */
    @Override
    public abstract Truth apply(Truth T, Truth B);
}
