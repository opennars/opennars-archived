package nars.term

import com.gs.collections.impl.factory.Sets
import nars.Global
import nars.term.compound.Compound


/**
 * Created by me on 1/2/16.
 */
interface TermContainer<T : Term> : Termlike, Iterable<T> {

    fun varDep(): Int

    fun varIndep(): Int

    fun varQuery(): Int

    fun vars(): Int

    /** gets subterm at index i  */
    fun term(i: Int): T

    fun termOr(index: Int, resultIfInvalidIndex: T): T

    //fun termsCopy(): Array<T>

    //fun setNormalized(b: Boolean)


//    fun termsCopy(vararg additional: T): Array<T> {
//        if (additional.size == 0) return termsCopy()
//        return Terms.concat(terms(), additional)
//    }

    fun toSet(): Set<T> {
        return terms().toSet()
    }

    fun toSortedSet(): Set<T> {
        return terms().toSortedSet()
    }

    //fun addAllTo(set: Collection<Term>)


    /** expected to provide a non-copy reference to an internal array,
     * if it exists. otherwise it should create such array.
     * if this creates a new array, consider using .term(i) to access
     * subterms iteratively.
     */
    fun terms(): List<T>


    open fun terms(filter: (Int,Term)->Boolean): Array<Term> {
        val l = Global.newArrayList<Term>(size())
        val s = size()
        for (i in 0..s - 1) {
            val t = term(i)
            if (filter.invoke(i, t))
                l.add(t)
        }
        return l.toTypedArray()
        //l.toArray(arrayOfNulls<Term>(l.size))
    }


    //fun forEach(action: () -> T, start: Int, stop: Int)

    /** extract a sublist of terms as an array  */
    fun terms(start: Int, end: Int): Array<Term?> {
        val t = arrayOfNulls<Term>(end - start)
        var j = 0
        for (i in start..end - 1)
            t[j++] = term(i)
        return t
    }

    /** follows normal indexOf() semantics; -1 if not found  */
    fun indexOf(t: Term?): Int {
        val s = size()
        for (i in 0..s - 1) {
            if (t == term(i))
                return i
        }
        return -1
    }


    //    /** writes subterm bytes, including any attached metadata preceding or following it */
    //    default void appendSubtermBytes(ByteBuf b) {
    //
    //        int n = size();
    //
    //        for (int i = 0; i < n; i++) {
    //            Term t = term(i);
    //
    //            if (i != 0) {
    //                b.add(ARGUMENT_SEPARATORbyte);
    //            }
    //
    //            try {
    //                byte[] bb = t.bytes();
    //                if (bb.length!=t.bytesLength())
    //                    System.err.println("wtf");
    //                b.add(bb);
    //            }
    //            catch (ArrayIndexOutOfBoundsException a) {
    //                System.err.println("Wtf");
    //            }
    //        }
    //
    //    }

    override fun containsTermRecursively(target: Term): Boolean {

        for (x in terms()) {
            if (impossibleSubTermOrEquality(target))
                continue
            if (x == target) return true
            if (x is Compound<*>) {
                if (x.containsTermRecursively(target)) {
                    return true
                }
            }
        }
        return false

    }

    fun equivalent(sub: List<Term?>): Boolean {
        val s = size()
        if (s != sub.size) return false
        for (i in 0..size() - 1) {
            if (term(i) != sub[i]) return false
        }
        return true
    }


    /** returns true if evaluates true for any terms
     * @param p
     */
    override fun or(p: (Term?)->Boolean): Boolean {
        for (t in terms()) {
            if (t.or(p))
                return true
        }
        return false
    }

    /** returns true if evaluates true for all terms
     * @param p
     */
    override fun and(p: (Term?)->Boolean): Boolean {
        for (t in terms()) {
            if (!p.invoke(t))
                return false
        }
        return true
    }

    companion object {
        fun intersect(a: TermVector<Term>, b: TermVector<Term>): TermVector<Term> {
            return TermVector(Sets.intersect(a.toSet(), b.toSet()))
        }

//        fun union(a: Compound<Term?>, b: Compound<Term?>): Set<Term?> {
//            var x = a.toSet();
//            x += b.toSet();
//            return x;
//        }

        /** returns null if empty set; not sorted  */
        fun difference(a: TermVector<Term>, b: TermVector<Term>): TermVector<Term> {
            if (a.size() == 1 && b.size() == 1) {
                //special case
                return if (a.term(0) == b.term(0)) TermVector.Empty
                else a
            } else {
                val dd = Sets.difference(a.toSet(), b.toSet())
                return if (dd.isEmpty()) TermVector.Empty
                else TermVector(dd)
            }
        }


//        fun copyByIndex(c: TermContainer<Term>): Array<Term?> {
//            val s = c.size()
//            val x = arrayOfNulls<Term>(s)
//            for (i in 0..s - 1) {
//                x[i] = c.term(i)
//            }
//            return x
//        }


//        fun toString(t: TermContainer<Term>): String {
//            val sb = StringBuilder("{[(")
//            val s = t.size()
//            for (i in 0..s - 1) {
//                sb.append(t.term(i))
//                if (i < s - 1)
//                    sb.append(", ")
//            }
//            sb.append(")]}")
//            return sb.toString()
//
//        }
    }

}
