package nars.term


/**
 * Features exhibited by, and which can classify terms
 * and termlike productions
 */
interface Termlike : Comparable<Any> {

    /** volume = total number of terms = complexity + # total variables  */
    fun volume(): Int

    /** total number of leaf terms, excluding variables which have a complexity of zero  */
    fun complexity(): Int

    fun structure(): Int

    /** number of subterms. if atomic, size=0  */
    fun size(): Int

    /** if contained within; doesnt match this term (if it's a term);
     * false if term is atomic since it can contain nothing
     */
    fun containsTerm(t: Term): Boolean

    val isNormalized: Boolean
        get() = true

    fun hasAny(structuralVector: Int): Boolean {
        return structure() and structuralVector != 0
    }


    fun impossibleStructureMatch(possibleSubtermStructure: Int): Boolean {
        return impossibleStructureMatch(
                structure(),
                possibleSubtermStructure)
    }

    fun containsTermRecursively(target: Term): Boolean

    fun impossibleSubterm(target: Term): Boolean {
        return impossibleStructureMatch(structure(), target.structure()) || impossibleSubTermVolume(target.volume())
    }

    /** if it's larger than this term it can not be equal to this.
     * if it's larger than some number less than that, it can't be a subterm.
     */
    fun impossibleSubTermOrEqualityVolume(otherTermsVolume: Int): Boolean {
        return otherTermsVolume > volume()
    }


    fun impossibleSubTermVolume(otherTermVolume: Int): Boolean {
        //        return otherTermVolume >
        //                volume()
        //                        - 1 /* for the compound itself */
        //                        - (size() - 1) /* each subterm has a volume >= 1, so if there are more than 1, each reduces the potential space of the insertable */

        /*
        otherTermVolume > volume - 1 - (size - 1)
                        > volume - size
         */
        return otherTermVolume > volume() - size()
    }


    fun impossibleSubTermOrEquality(target: Term): Boolean {
        return impossibleStructureMatch(target.structure()) || impossibleSubTermOrEqualityVolume(target.volume())
    }

    /** recurses all subterms while the result of the predicate is true;
     * returns true if all true

     * @param v
     */
    fun and(v: (Term?) -> Boolean): Boolean

    /** recurses all subterms until the result of the predicate becomes true;
     * returns true if any true

     * @param v
     */
    fun or(v: (Term?) -> Boolean): Boolean


    fun impossibleStructureMatch(existingStructure: Int, possibleSubtermStructure: Int): Boolean {
        //if the OR produces a different result compared to subterms,
        // it means there is some component of the other term which is not found
        return possibleSubtermStructure or existingStructure != existingStructure
    }



}
