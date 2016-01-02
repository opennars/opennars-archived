package nars.term

import nars.Op
import nars.nal.nal7.Order
import nars.term.match.VarPattern
import nars.term.visit.SubtermVisitor



public interface Term : Termlike {


    fun term(): Term {
        return this
    }

    fun op(): Op

    /** syntactic help  */
    fun op(equalTo: Op): Boolean {
        return op() === equalTo
    }




    open fun recurseTerms(v: SubtermVisitor) {
        recurseTerms(v, null)
    }

    fun recurseTerms(v: SubtermVisitor, parent: Term?)


    /**
     * Commutivity in NARS means that a Compound term's
     * subterms will be unique and arranged in order (compareTo)

     *
     *
     * commutative CompoundTerms: Sets, Intersections Commutative Statements:
     * Similarity, Equivalence (except the one with a temporal order)
     * Commutative CompoundStatements: Disjunction, Conjunction (except the one
     * with a temporal order)

     * @return The default value is false
     */
    val isCommutative: Boolean

    /**
     * Whether this compound term contains any variable term
     */
    fun hasVar(): Boolean {
        return vars() > 0
    }

    val temporalOrder: Order
        get() = op().temporalOrder

    //boolean hasVar(final Op type);


    /** tests if contains a term in the structural hash
     * WARNING currently this does not detect presence of pattern variables
     */
    fun hasAny(op: Op): Boolean {
        //        if (op == Op.VAR_PATTERN)
        //            return Variable.hasPatternVariable(this);
        return hasAny(op.bit())
    }




    //    default boolean hasAll(int structuralVector) {
    //        final int s = structure();
    //        return (s & structuralVector) == s;
    //    }
    //

    //@Override
    fun isAny(structuralVector: Int): Boolean {
        val s = op().bit()
        return s and structuralVector == s
    }

    /** for multiple Op comparsions, use Op.or to produce an int and call isAny(int vector)  */
    fun isAny(op: Op): Boolean {
        return isAny(op.bit())
    }

    /** # of contained independent variables  */
    fun varIndep(): Int

    /** # of contained dependent variables  */
    fun varDep(): Int

    /** # of contained query variables  */
    fun varQuery(): Int


    /** total # of variables, excluding pattern variables  */
    fun vars(): Int

    fun hasVarIndep(): Boolean {
        return varIndep() != 0
    }

    open fun hasEllipsis(): Boolean {
        return false
    }

    fun hasVarPattern(): Boolean {
        return VarPattern.hasPatternVariable(this)
    }

    fun hasVarDep(): Boolean {
        return varDep() != 0
    }

    fun hasVarQuery(): Boolean {
        return varQuery() != 0
    }

    /** set the system's perceptual duration (cycles)
     * for measuring event timings
     * can return a different term than 'this', in
     * case the duration causes a reduction or some
     * other transformation. in any case, the callee should
     * replace the called term with the result
     */
    open fun setDuration(duration: Int) {
        //nothing
    }


//    //@Throws(IOException::class)
//    fun append(w: java.lang.Appendable, pretty: Boolean)

    //    default public void append(Writer w, boolean pretty) throws IOException {
    //        //try {
    //            name().append(w, pretty);
    ////        } catch (IOException e) {
    ////            e.printStackTrace();
    ////        }
    //    }

//    fun toStringBuilder(pretty: Boolean): java.lang.StringBuilder

    //    default public StringBuilder toStringBuilder(boolean pretty) {
    //        return name().toStringBuilder(pretty);
    //    }

    fun toString(pretty: Boolean): String
    //    default public String toString(boolean pretty) {
    //        return toStringBuilder(pretty).toString();
    //    }

    open fun toStringCompact(): String {
        return toString()
    }


    fun levelValid(nal: Int): Boolean {

        if (nal >= 8) return true

        val mask = Op.NALLevelEqualAndAbove[nal]
        return structure().or(mask) == mask
    }

//    fun structureString(): String {
//        return "%16s".format(Integer.toBinaryString(structure())).replace(" ", "0")
//    }


    fun containsTemporal(): Boolean {
        //TODO construct bit vector for one comparison
        return isAny(Op.TemporalBits)
    }


    open fun opRel(): Int {
        return (-1.shl(16)).or(op().ordinal)
    }

    //    default public boolean hasAll(final Op... op) {
    //        //TODO
    //    }
    //    default public boolean hasAny(final Op... op) {
    //        //TODO
    //    }

}

