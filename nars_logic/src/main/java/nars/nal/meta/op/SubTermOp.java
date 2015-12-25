package nars.nal.meta.op;

import nars.Op;
import nars.nal.meta.FindSubst;
import nars.term.compound.Compound;

/**
 * requires a specific subterm type
 */
public final class SubTermOp extends PatternOp {
    public final int subterm;
    public final Op op;
    private final transient String id;


    public SubTermOp(int subterm, Op op) {
        this.subterm = subterm;
        this.op = op;
        id = "t" + subterm + ':' + op;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean run(FindSubst ff) {
        Compound parent = (Compound) ff.term.get();
        return parent.term(subterm).op() == op;
    }
}
