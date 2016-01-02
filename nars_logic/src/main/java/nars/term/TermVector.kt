package nars.term

import com.google.common.base.Joiner
import nars.term.compound.Compound
import nars.util.data.Util


/**
 * Holds a vector or tuple of terms.
 * Useful for storing a fixed number of subterms

 * TODO make this class immutable and term field private
 * provide a MutableTermVector that holds any write/change methods
 */
public class TermVector<T : Term> : TermContainer<T> {

    /**
     * list of (direct) term
     * TODO make not public
     */
    val term: List<T>


    /**
     * bitvector of subterm types, indexed by NALOperator's .ordinal() and OR'd into by each subterm
     */
    @Transient protected var structureHash: Int = 0
    @Transient protected var contentHash: Int = 0
    @Transient protected var volume: Short = 0
    @Transient protected var complexity: Short = 0

    /**
     * # variables contained, of each type & total
     */
    @Transient protected var varTotal: Byte = 0
    @Transient protected var hasVarQueries: Byte = 0
    @Transient protected var hasVarIndeps: Byte = 0
    @Transient protected var hasVarDeps: Byte = 0

    private var _normalized: Boolean = false
    protected var normalized: Boolean
        get() = _normalized
        set(value) {
            _normalized = value
        }

    //        internal set(value: Boolean) {
//            super.isNormalized = value
//        }

    //    public TermVector() {
    //        this(null);
    //    }


    constructor(t: List<T>) {
        term = t
        init()
    }

    constructor(t: Collection<T>) {
        term = t.toList()
        init()
    }

    /** first n items  */
    constructor(t: Collection<T>, n: Int){
        term = t.toList().subList(0, n)
        init()
    }
    constructor(vararg t: T) {
        term = t.toArrayList()
        init()
    }

//    constructor() {
//        term = Collections.emptyList();
//        init()
//    }

//    constructor(source: Array<T>, mapping: Function<T, T>) {
//        val len = source.size
//        val t = this.term = arrayOfNulls<Term>(len) as Array<T>
//        for (i in 0..len - 1)
//            t[i] = mapping.valueOf(source[i])
//        init()
//    }


    override fun terms(): List<T> {
        return term
    }


//    override fun terms(filter: IntObjectPredicate<T>): List<Term> {
//        return Terms.filter(term, filter)
//    }


    override fun structure(): Int {
        return structureHash
    }

    override fun term(i: Int): T {
        return term.get(i)
    }

//    fun equals(t: Array<Term>): Boolean {
//        return Arrays.equals(term, t)
//    }

    override fun termOr(index: Int, resultIfInvalidIndex: T): T {
        val term = this.term
        return if (term.size <= index)
            resultIfInvalidIndex
        else
            term[index]
    }

    override fun volume(): Int {
        return volume.toInt()
    }

    /**
     * report the term's syntactic complexity

     * @return the complexity value
     */
    override fun complexity(): Int {
        return complexity.toInt()
    }

    /**
     * get the number of term

     * @return the size of the component list
     */
    override fun size(): Int {
        return term.size
    }

//    /**
//     * (shallow) Clone the component list
//     */
//    override fun termsCopy(): Array<T> {
//        return copyOf<T>(term, size())
//    }


    override fun toString(): String {
        return "(" + Joiner.on(",").join(term.asIterable<T>()) + ")"
    }

    override fun varDep(): Int {
        return hasVarDeps.toInt()
    }

    override fun varIndep(): Int {
        return hasVarIndeps.toInt()
    }

    override fun varQuery(): Int {
        return hasVarQueries.toInt()
    }

    override fun vars(): Int {
        return varTotal.toInt()
    }

    //    public Term[] cloneTermsReplacing(int index, Term replaced) {
    //        Term[] y = termsCopy();
    //        y[index] = replaced;
    //        return y;
    //    }

    val isEmpty: Boolean
        get() = size() != 0


    /**
     * first level only, not recursive
     */
    operator fun contains(o: Any): Boolean {
        return if (o is Term)
            containsTerm(o)
        else
            false
    }

    override fun iterator(): Iterator<T> {
        return term.iterator()
    }


    fun forEach(action: (T)->Any, start: Int, stop: Int) {
        val tt = term
        for (i in start..stop - 1)
            action.invoke(tt[i])
    }

    fun forEach(action: (T)->Any) {
        val tt = term
        for (t in tt)
            action.invoke(t)
    }

    /**
     * Check the subterms (first level only) for a target term

     * @param t The term to be searched
     * *
     * @return Whether the target is in the current term
     */
    override fun containsTerm(t: Term): Boolean {
        return if (impossibleSubterm(t))
            false
        else
            Terms.contains(term, t)
    }


    //    static int nextContentHash(int hash, int subtermHash) {
    //        return Util.PRIME2 * hash + subtermHash;
    //        //return (hash << 4) +  subtermHash;
    //        //(Util.PRIME2 * contentHash)
    //    }


    /** returns hashcode  */
    fun init(): Int {

        var deps = 0
        var indeps = 0
        var queries = 0
        var compl = 1
        var vol = 1

        var subt = 0
        var contentHash = 1

        for (t in term) {

            if (t === this)
                throw RecursiveTermContentException(t)
//            if (t == null)
//                throw NullPointerException()

            contentHash = Util.hashCombine(contentHash, t!!.hashCode())

            compl += t!!.complexity()
            vol += t!!.volume()
            deps += t!!.varDep()
            indeps += t!!.varIndep()
            queries += t!!.varQuery()
            subt = subt or t!!.structure()
        }

        //MAX 255 variables
        hasVarDeps = deps.toByte()
        hasVarIndeps = indeps.toByte()
        hasVarQueries = queries.toByte()
        structureHash = subt
        varTotal = (deps + indeps + queries).toByte()
        normalized = varTotal.toInt() ==0

        complexity = compl.toShort()
        volume = vol.toShort()

        if (contentHash == 0) contentHash = 1 //nonzero to indicate hash calculated
        this.contentHash = contentHash
        return contentHash
    }

//    fun addAllTo(set: Collection<T>) {
//        set.addAll(term)
//        //Collections.addAll<T>(set, *term)
//    }

    override fun hashCode(): Int {
        return contentHash
        //        final int h = contentHash;
        //        if (h == 0) {
        //            //if hash is zero, it means it needs calculated
        //            //return init(term);
        //            throw new RuntimeException("unhashed");
        //        }
        //        return h;
    }

    override fun equals(that: Any?): Boolean {

        if (this === that) return true

        if (that !is TermVector<*>) return false

        return contentHash == that.contentHash &&
                structureHash == that.structureHash &&
                volume == that.volume &&
                equalTerms(that)
    }

    private fun equalTerms(c: TermVector<*>): Boolean {
        val s = size()
        if (s != c.size())
            return false

        val tt = this.term
        for (i in 0..s - 1) {
            if (tt[i] != c.term(i))
                return false
        }

        return true
    }


    override fun compareTo(o: Any): Int {
        if (this === o) return 0

        val diff = hashCode().compareTo(o.hashCode())
        if (diff != 0)
            return diff

        //TODO dont assume it's a TermVector
        val c = o as TermVector<*>
        val diff2 = structure().compareTo(c.structure());
        if (diff2 != 0)
            return diff2

        return compareContent(c)
    }

    fun compareContent(c: TermVector<*>): Int {

        val s = size()
        val diff = s.compareTo(c.size());
        if (diff != 0)
            return diff

        val thisTerms = this.term
        val thatTerms = c.term
        for (i in 0..s - 1) {
            val d = thisTerms[i].compareTo(thatTerms[i])

            /*
        if (Global.DEBUG) {
            int d2 = b.compareTo(a);
            if (d2!=-d)
                throw new RuntimeException("ordering inconsistency: " + a + ", " + b );
        }
        */

            if (d != 0) return d
        }

        return 0
    }

    fun visit(v: (T /* child */,Term /* parent */)->Any, parent: Compound<Term>) {
        for (t in term)
            v.invoke(t, parent)
    }


    /** thrown if a compound term contains itself as an immediate subterm  */
    class RecursiveTermContentException(val term: Term) : Throwable(term.toString())

    companion object {

        val Empty = TermVector<Term>()
    }
}
