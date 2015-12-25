package nars.nal.meta.op;

import nars.nal.meta.FindSubst;
import nars.term.compound.Compound;

/**
 * sets the term to its parent, and the parent to a hardcoded value (its parent)
 */
public final class ParentTerm extends PatternOp {
    public final Compound parent;

    public ParentTerm(Compound parent) {
        this.parent = parent;
    }

    @Override
    public boolean run(FindSubst f) {
        f.term.set(f.parent.get());
        f.parent.set(parent);

        return true;
    }

    @Override
    public String toString() {
        return "parent(" + parent + ')'; //s for subterm and sibling
    }
}
