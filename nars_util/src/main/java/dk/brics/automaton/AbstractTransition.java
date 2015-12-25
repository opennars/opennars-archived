package dk.brics.automaton;

import nars.util.data.Util;

import java.io.Serializable;

/**
 * Created by me on 12/25/15.
 */
abstract public class AbstractTransition implements Serializable {

    public final State to;

    public AbstractTransition(State to) {
        this.to = to;
    }

    abstract public int max();
    abstract public int min();
    abstract public int min(int newMin);

    /**
     * Checks for equality.
     * @param obj object to compare with
     * @return true if <tt>obj</tt> is a transition with same
     *         character interval and destination state as this transition.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof AbstractTransition) {
            AbstractTransition t = (AbstractTransition)obj;
            return t.min() == min() && t.max() == max() && t.to == to;
        }
        return false;
    }

    /**
     * Returns hash code.
     * The hash code is based on the character interval (not the destination state).
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Util.hashCombine(min(), max());
    }


}
