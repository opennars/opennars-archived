package nars.term

import nars.Op

/**
 * Created by me on 1/2/16.
 */
interface Termed<TT : Term> {

    fun term(): TT

    fun op(): Op {
        return term().op()
    }

    fun isAny(vector: Int): Boolean {
        return term().isAny(vector)
    }

    fun opRel(): Int {
        return term().opRel()
    }

    fun levelValid(nal: Int): Boolean {
        return term().levelValid(nal)
    }

}
