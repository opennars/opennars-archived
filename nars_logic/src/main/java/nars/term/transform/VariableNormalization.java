package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.term.Compound;
import nars.term.Variable;
import nars.util.utf8.Byted;

import java.util.Map;

/**
 * Variable normalization
 *
 * Destructive mode modifies the input Compound instance, which is
 * fine if the concept has been created and unreferenced.
 *
 * The term 'destructive' is used because it effectively destroys some
 * information - the particular labels the input has attached.
 *
 */
public class VariableNormalization extends VariableTransform {


    /** for use with compounds that have exactly one variable */
    public static final VariableTransform singleVariableNormalization = new VariableTransform() {

        @Override
        public final Variable apply(Compound containing, Variable current, int depth) {
            //      (containing, current, depth) ->
            return Variable.the(current.op(), 1);
        }
    };


    Map<Variable, Variable> rename;

    final Compound result;
    boolean renamed = false;
    int serial = 0;


    public static VariableNormalization normalize(Compound target, boolean destructive) {
        return new VariableNormalization(target, null, destructive);
    }

    /** allows using the single variable normalization,
     * WARNING will not work if the term contains pattern variables */
    public static VariableNormalization normalizeFastIfContainsNoPatternVariables(Compound target, boolean destructive) {
        return new VariableNormalization(target,
                /* TODO if target contains NO pattern variables */  target.vars() == 1 ?
                singleVariableNormalization : null, destructive);
    }

    public VariableNormalization(Compound target, boolean destructively) {
        this(target, null, destructively);
    }

    public VariableNormalization(Compound target, CompoundTransform tx, boolean destructively) {

        if (tx == null) tx = this;

        final Compound result1;
        if (destructively) {
            renamed = (target.transform(tx));
            result1 = target;
        }
        else {
            result1 = target.cloneTransforming(tx);
        }

        this.result = result1;

        if (rename != null)
            rename.clear(); //assists GC
    }


    @Override
    public final Variable apply(final Compound ct, final Variable v, int depth) {

        Map<Variable, Variable> rename = this.rename;

        if (rename == null) this.rename = rename = Global.newHashMap(0); //lazy allocate


        final Map<Variable,Variable> finalRename = rename;
        Variable vv = rename.computeIfAbsent(v, _vname -> {
            //type + id
            Variable rvv = newVariable(v.op(), finalRename.size() + 1);
            if (!renamed) //test for any rename to know if we need to rehash
                renamed |= !Byted.equals(rvv, v);
            return rvv;
        });

        serial++; //identifies terms by their unique final position

        return vv;
    }

    final static protected Variable newVariable(final Op type, final int i) {
        return Variable.the(type, i);
    }

    public final Compound getResult() {
        return result;
    }
}


//    final static Comparator<Map.Entry<Variable, Variable>> comp = new Comparator<Map.Entry<Variable, Variable>>() {
//        @Override
//        public int compare(Map.Entry<Variable, Variable> c1, Map.Entry<Variable, Variable> c2) {
//            return c1.getKey().compareTo(c2.getKey());
//        }
//    };

//    /**
//     * overridden keyEquals necessary to implement alternate variable hash/equality test for use in normalization's variable transform hashmap
//     */
//    static final class VariableMap extends FastPutsArrayMap<Pair<Variable,Term>, Variable> {
//
//
//
//        public VariableMap(int initialCapacity) {
//            super(initialCapacity);
//        }
//
//        @Override
//        public final boolean keyEquals(final Variable a, final Object ob) {
//            if (a == ob) return true;
//            Variable b = ((Variable) ob);
//            return Byted.equals(a, b);
//        }
//
////        @Override
////        public Variable put(Variable key, Variable value) {
////            Variable removed = super.put(key, value);
////            /*if (size() > 1)
////                Collections.sort(entries, comp);*/
////            return removed;
////        }
//    }
