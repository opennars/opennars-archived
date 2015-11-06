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
public class VariableNormalization implements VariableTransform {

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


    /** for use with compounds that have exactly one variable */
    public static final VariableTransform singleVariableNormalization =
            (containing, current, depth) -> Variable.the(current.op(), 1);

    Map<Variable, Variable> rename;

    final Compound result;
    boolean renamed = false;
    int serial = 0;


    public static VariableNormalization normalize(Compound target, boolean destructive) {
        return new VariableNormalization(target, null, destructive);
    }

    /** allows using the single variable normalization,
     * which is safe if the term doesnt contain pattern variables */
    public static VariableNormalization normalizeFast(Compound target, boolean destructive) {
        return new VariableNormalization(target, target.vars() == 1 ?
                singleVariableNormalization : null, destructive);
    }

    public VariableNormalization(Compound target, boolean destructively) {
        this(target, null, destructively);
    }

    public VariableNormalization(Compound target, CompoundTransform tx, boolean destructively) {

        final Compound result1;

        if (tx == null) tx = this;

        if (destructively) {
            target.transform(tx);
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
    public Variable apply(final Compound ct, final Variable v, int depth) {
        //            if (!v.hasVarIndep() && v.isScoped()) //already scoped; ensure uniqueness?
//                vname = vname.toString() + v.getScope().name();


        Map<Variable, Variable> rename = this.rename;

        if (rename == null) this.rename = rename = Global.newHashMap(0); //lazy allocate

//        Variable vv = rename.get(vname);
//        if (vv == null) {
//            //type + id
//            vv = newVariable(v.getType(), rename.size() + 1);
//            rename.put(vname, vv);
//            renamed = !vv.name().equals(v.name());
//        }

        /* 1.6.4:
        CharSequence vname = v.name();
        if (!v.hasVarIndep())
            vname = vname + " " + v.getScope().name(); */


        //int context = v.op() == Op.VAR_DEPENDENT ? -1 : serial;
        //int context = -1; //(v.op() != Op.VAR_DEPENDENT) ? -1 : serial;
        //int context = v.op() == Op.VAR_INDEPENDENT ? System.identityHashCode(v) : -1;

        //IntObjectPair<Variable> scoping = PrimitiveTuples.pair(-1, v);



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
