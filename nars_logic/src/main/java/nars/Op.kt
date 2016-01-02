package nars


import com.gs.collections.api.tuple.primitive.IntIntPair
import com.gs.collections.impl.tuple.primitive.PrimitiveTuples.pair
import nars.nal.nal7.Order
import nars.term.atom.Atom

/**
 * NAL symbol table
 */
enum class Op private constructor(
        /**
         * symbol representation of this getOperator
         */
        val str: String, val isCommutative: Boolean,
        /**
         * minimum NAL level required to use this operate, or 0 for N/A
         */
        val minLevel: Int, val type: Op.OpType, size: IntIntPair) {

    //TODO include min/max arity for each operate, if applicable

    /**
     * an atomic term (includes interval and variables); this value is set if not a compound term
     */
    ATOM(".", Op.ANY, OpType.Other),
    //        public final Atom get(String i) {
    //            return Atom.the(i);
    //        }}
    //
    VAR_INDEP(Symbols.VAR_INDEPENDENT, 6 /*NAL6 for Indep Vars */, OpType.Variable),
    VAR_DEP(Symbols.VAR_DEPENDENT, Op.ANY, OpType.Variable),
    VAR_QUERY(Symbols.VAR_QUERY, Op.ANY, OpType.Variable),

    OPERATOR("^", 8, Args.One),

    NEGATE("--", 5, Args.One) {

    },

    /* Relations */
    INHERIT("-->", 1, OpType.Relation, Args.Two),
    SIMILAR("<->", true, 2, OpType.Relation, Args.Two),


    /* CompountTerm operators */
    INTERSECT_EXT("&", true, 3, Args.GTETwo),
    INTERSECT_INT("|", true, 3, Args.GTETwo),

    DIFF_EXT("-", 3, Args.Two),
    DIFF_INT("~", 3, Args.Two),

    PRODUCT("*", 4, Args.GTEZero),

    IMAGE_EXT("/", 4, Args.GTEOne),
    IMAGE_INT("\\", 4, Args.GTEOne),

    /* CompoundStatement operators, length = 2 */
    DISJUNCTION("||", true, 5, Args.GTEOne),
    CONJUNCTION("&&", true, 5, Args.GTETwo),

    SEQUENCE("&/", 7, Args.GTETwo), /* NOTE: after cycle terms intermed, it may appear to have one term. but at construction when this is tested, it will need multiple terms even if they are intervals */
    PARALLEL("&|", true, 7, Args.GTETwo),


    /* CompountTerm delimiters, must use 4 different pairs */
    SET_INT_OPENER("[", true, 2, Args.GTEOne), //OPENER also functions as the symbol for the entire compound
    SET_EXT_OPENER("{", true, 2, Args.GTEOne), //OPENER also functions as the symbol for the entire compound


    IMPLICATION("==>", 5, OpType.Relation, Args.Two),
    IMPLICATION_AFTER("=/>", 7, OpType.Relation, Args.Two),
    IMPLICATION_WHEN("=|>", 7, OpType.Relation, Args.Two),
    IMPLICATION_BEFORE("=\\>", 7, OpType.Relation, Args.Two),

    EQUIV("<=>", true, 5, OpType.Relation, Args.Two),
    EQUIV_AFTER("</>", 7, OpType.Relation, Args.Two),
    EQUIV_WHEN("<|>", true, 7, OpType.Relation, Args.Two),


//    // keep all items which are invlved in the lower 32 bit structuralHash above this line
//    // so that any of their ordinal values will not exceed 31
//    //-------------
//    NONE(2205.toChar(), Op.ANY, null),

    VAR_PATTERN(Symbols.VAR_PATTERN, Op.ANY, OpType.Variable),

    INTERVAL(//TODO decide what this value should be, it overrides with IMAGE_EXT
            //but otherwise it's not used
            Symbols.INTERVAL_PREFIX.toString() + '/', Op.ANY, Args.None),

    INSTANCE("{--", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing
    PROPERTY("--]", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing
    INSTANCE_PROPERTY("{-]", 2, OpType.Relation);

    /**
     * character representation of this getOperator if symbol has length 1; else ch = 0
     */
    val ch: Char

    /** arity limits, range is inclusive >= <=
     * -1 for unlimited  */
    val minSize: Int
    val maxSize: Int

    /**
     * opener?
     */
    val opener: Boolean

    /**
     * closer?
     */
    val closer: Boolean

    /**
     * should be null unless a 1-character representation is not possible.
     */
    val bytes: ByteArray
    val temporalOrder: Order

    private constructor(s: String, commutative: Boolean, minLevel: Int) : this(s, minLevel, OpType.Other, Args.None) {
    }

    private constructor(s: String, commutative: Boolean, minLevel: Int, size: IntIntPair) : this(s, commutative, minLevel, OpType.Other, size) {
    }

    private constructor(c: Char, minLevel: Int, type: OpType, size: IntIntPair = Args.None) : this(c.toString(), minLevel, type, size) {
    }

    private constructor(string: String, minLevel: Int, size: IntIntPair) : this(string, minLevel, OpType.Other, size) {
    }

    private constructor(string: String, minLevel: Int, type: OpType) : this(string, false /* non-commutive */, minLevel, type, Args.None) {
    }
    private constructor(string: String, minLevel: Int, type: OpType, size: IntIntPair) : this(string, false /* non-commutive */, minLevel, type, size) {
    }

    init {

        bytes = str.toByteArray()

        ch = if (str.length == 1) str[0] else 0.toChar()

        opener = name.endsWith("_OPENER")
        closer = name.endsWith("_CLOSER")

        this.minSize = size.one
        this.maxSize = size.two

        var o = Order.None
        when (str) {
        //has to be done by string..
            "=/>", "</>", "&/" -> o = Order.Forward
            "=|>", "<|>", "&|" -> o = Order.Concurrent
            "=\\>" -> o = Order.Backward
        }
        this.temporalOrder = o

    }


    override fun toString(): String {
        return str
    }


//    /**
//     * writes this operator to a Writer in (human-readable) expanded UTF16 mode
//     */
//    fun append(w: Appendable) {
//        if (ch.toInt() == 0)
//            w.append(str)
//        else
//            w.append(ch)
//    }

    fun bit(): Int {
        return 1 shl ordinal
    }

    val isVar: Boolean
        get() = type == Op.OpType.Variable

    fun validSize(length: Int): Boolean {
        if (minSize != -1 && length < minSize) return false
        return !(maxSize != -1 && length > maxSize)
    }

    val isImage: Boolean
        get() = isA(ImageBits)

    val isConjunctive: Boolean
        get() = isA(ConjunctivesBits)


    val isStatement: Boolean
        get() = isA(StatementBits)

    fun isA(vector: Int): Boolean {
        return isA(bit(), vector)
    }

    val isSet: Boolean
        get() = isA(Op.SetsBits)

    val isTemporal: Boolean
        get() = isA(TemporalBits)


    /** top-level Op categories  */
    public enum class OpType {
        Relation,
        Variable,
        Other
    }


    object Args {

        val None = pair(0, 0)
        val One = pair(1, 1)
        val Two = pair(2, 2)

        val GTEZero = pair(0, -1)
        val GTEOne = pair(1, -1)
        val GTETwo = pair(2, -1)

    }

    companion object {
        //should not be given a compact representation because this will not exist internally after parsing


        //-----------------------------------------------------


        /** Image index ("imdex") symbol  */
        /*Deprecated*/ val Imdex = Atom.Imdex;


        /**
         * alias
         */
        val SET_EXT = Op.SET_EXT_OPENER
        val SET_INT = Op.SET_INT_OPENER

        fun or(vararg i: Int): Int {
            var bits = 0
            for (x in i) {
                bits = bits or x
            }
            return bits
        }

        fun or(vararg o: Op): Int {
            var bits = 0
            for (n in o)
                bits = bits.or(n.bit())
            return bits
        }

        fun or(bits: Int, o: Op): Int {
            return bits.or(o.bit())
        }


        /**
         * specifier for any NAL level
         */
        val ANY = 0

        internal fun isA(needle: Int, haystack: Int): Boolean {
            return needle.and(haystack) == needle
        }


        val ImplicationsBits = Op.or(Op.IMPLICATION, Op.IMPLICATION_BEFORE, Op.IMPLICATION_WHEN, Op.IMPLICATION_AFTER)

        val ConjunctivesBits = Op.or(Op.CONJUNCTION, Op.PARALLEL, Op.SEQUENCE)

        val EquivalencesBits = Op.or(Op.EQUIV, Op.EQUIV_WHEN, Op.EQUIV_AFTER)

        val SetsBits = Op.or(Op.SET_EXT, Op.SET_INT)

        /** all Operations will have these 3 elements in its subterms:  */
        val OperationBits = Op.or(Op.INHERIT, Op.PRODUCT, OPERATOR)

        val StatementBits = Op.or(Op.INHERIT.bit(), Op.SIMILAR.bit(),
                EquivalencesBits,
                ImplicationsBits)

        val ImplicationOrEquivalenceBits = or(Op.EquivalencesBits, Op.ImplicationsBits)

        val ImageBits = Op.or(Op.IMAGE_EXT, Op.IMAGE_INT)

        val VariableBits = Op.or(Op.VAR_PATTERN, Op.VAR_INDEP, Op.VAR_DEP, Op.VAR_QUERY)
        val WildVariableBits = Op.or(Op.VAR_PATTERN, Op.VAR_QUERY)

        val TemporalBits = Op.or(
                Op.PARALLEL, Op.SEQUENCE,
                Op.EQUIV_AFTER, Op.EQUIV_WHEN,
                Op.IMPLICATION_AFTER, Op.IMPLICATION_WHEN, Op.IMPLICATION_BEFORE)

        val NALLevelEqualAndAbove = IntArray(8 + 1) //indexed from 0..7, meaning index 7 is NAL8, index 0 is NAL1

        init {
            for (o in Op.values()) {
                var l = o.minLevel
                if (l < 0) l = 0 //count special ops as level 0, so they can be detected there
                for (i in l..8) {
                    NALLevelEqualAndAbove[i] = NALLevelEqualAndAbove[i] or o.bit()
                }
            }
        }
    }
}//    Op(char c, int minLevel) {
//        this(c, minLevel, Args.NoArgs);
//    }
