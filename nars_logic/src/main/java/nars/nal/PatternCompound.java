package nars.nal;

import nars.Op;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.Ellipsis;
import nars.term.transform.FindSubst;

final class PatternCompound extends GenericCompound {


    public final int sizeCached;
    public final int volCached;
    public final int structureCachedWithoutVars;
    public final Term[] termsCached;
    protected final boolean ellipsis;
    private final boolean commutative;
    private final boolean ellipsisTransform;

    public PatternCompound(Compound seed, TermVector subterms) {
        super(seed.op(), subterms, seed.relation());

        sizeCached = seed.size();
        structureCachedWithoutVars =
                //seed.structure() & ~(Op.VariableBits);
                seed.structure() & ~(Op.VAR_PATTERN.bit());

        this.ellipsis = Ellipsis.hasEllipsis(this);
        this.ellipsisTransform = Ellipsis.hasEllipsisTransform(this);
        this.volCached = seed.volume();
        this.termsCached = subterms.terms();
        this.commutative = isCommutative();
    }

    @Override
    public Term[] terms() {
        return termsCached;
    }

    @Override
    public boolean match(Compound y, FindSubst subst) {
        if (!prematch(y)) return false;


        if (!ellipsis) {

            if (commutative && y.size() > 1) {
                return subst.matchPermute(this, y);
            }

            return matchLinear(y, subst);

        } else {
            return subst.matchCompoundWithEllipsis(this, y);
        }

    }

    final public boolean prematch(Compound y) {
        int yStructure = y.structure();
        if ((yStructure | structureCachedWithoutVars) != yStructure)
            return false;

        if (!ellipsis) {
            if (sizeCached != y.size())
                return false;
        }

        if (volCached > y.volume())
            return false;

        if (!ellipsisTransform) {
            if (relation != y.relation())
                return false;
        }

        return true;
    }

}
