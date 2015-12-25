package nars.term.match

import nars.term.Term
import nars.term.TermVector
import nars.term.compound.Compound
import nars.term.compound.GenericCompound
import nars.term.transform.Subst

public class PatternCompound(seed: Compound<Term>, subterms: TermVector<Term>) :
        GenericCompound<Term>(seed.op(), subterms, seed.relation()) {


    val sizeCached: Int
    val volCached: Int
    val structureCachedWithoutVars: Int
    val termsCached: Array<Term>
    protected val ellipsis: Boolean
    private val commutative: Boolean
    private val ellipsisTransform: Boolean

    init {

        sizeCached = seed.size()
        structureCachedWithoutVars = //seed.structure() & ~(Op.VariableBits);
                seed.structure() and (nars.Op.VAR_PATTERN.bit()).inv()

        this.ellipsis = Ellipsis.hasEllipsis(this)
        this.ellipsisTransform = Ellipsis.hasEllipsisTransform(this)
        this.volCached = seed.volume()
        this.termsCached = subterms.terms()
        this.commutative = isCommutative
    }



    override fun terms(): Array<Term> {
        return termsCached
    }

    override fun match(y: Compound<Term>, subst: Subst): Boolean {
        if (!prematch(y)) return false


        if (!ellipsis) {

            if (commutative && y.size() > 1) {
                return subst.matchPermute(this, y)
            }

            return matchLinear(y, subst)

        } else {
            return subst.matchCompoundWithEllipsis(this, y)
        }

    }

    fun prematch(y: Compound<Term>): Boolean {
        val yStructure = y.structure()
        if ((yStructure or structureCachedWithoutVars) != yStructure)
            return false

        if (!ellipsis) {
            if (sizeCached != y.size())
                return false
        }

        if (volCached > y.volume())
            return false

        if (!ellipsisTransform) {
            if (relation != y.relation())
                return false
        }

        return true
    }

}
