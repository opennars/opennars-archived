package dk.brics.automaton;

/**
* <tt>Automaton</tt> transition.
* <p>
* A transition, which belongs to a source state, consists of a Unicode character interval
* and a destination state.
* @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
*/
public final class CharTransition extends AbstractTransition{

/*
 * CLASS INVARIANT: min<=max
 */

char min;
final char max;


/**
 * Constructs a new singleton interval transition.
 * @param max transition character
 * @param to destination state
 */
public CharTransition(char max, State to)	{
    this(max, max, to);
}

/**
 * Constructs a new transition.
 * Both end points are included in the interval.
 * @param min transition interval minimum
 * @param max transition interval maximum
 * @param to destination state
 */
public CharTransition(char min, char max, State to)	{
    super(to);
    if (max < min) {
        this.max = min; this.min = max;
    } else {
        this.min = min; this.max = max;
    }
}

@Override
public int min(int newMin) {
    return this.min = (char)newMin;
}

    /** Returns minimum of this transition interval. */
public char getMin() {
    return min;
}

@Override
public int max() {
    return max;
}

@Override
public int min() {
    return min;
}


//	/**
//	 * Clones this transition.
//	 * @return clone with same character interval and destination state
//	 */
//	@Override
//	public CharTransition clone() {
//		try {
//			return (CharTransition)super.clone();
//		} catch (CloneNotSupportedException e) {
//			throw new RuntimeException(e);
//		}
//	}

static void appendCharString(int c, StringBuilder b) {
    if (c >= 0x21 && c <= 0x7e && c != '\\' && c != '"')
        b.append(c);
    else {
        b.append("\\u");
        String s = Integer.toHexString(c);
        if (c < 0x10)
            b.append("000").append(s);
        else if (c < 0x100)
            b.append("00").append(s);
        else if (c < 0x1000)
            b.append('0').append(s);
        else
            b.append(s);
    }
}

/**
 * Returns a string describing this state. Normally invoked via
 * {@link AbstractAutomaton#toString()}.
 */
@Override
public String toString() {
    StringBuilder b = new StringBuilder();
    appendCharString(min, b);
    if (min != max) {
        b.append('-');
        appendCharString(max, b);
    }
    b.append(" -> ").append(to.number);
    return b.toString();
}

void appendDot(StringBuilder b) {
    b.append(" -> ").append(to.number).append(" [label=\"");
    appendCharString(min, b);
    if (min != max) {
        b.append('-');
        appendCharString(max, b);
    }
    b.append("\"]\n");
}
}
