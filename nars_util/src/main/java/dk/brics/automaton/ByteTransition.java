package dk.brics.automaton;

/**
 * Created by me on 12/25/15.
 */
public final class ByteTransition extends AbstractTransition {


    byte min;
    final byte max;

    public ByteTransition(byte min, byte max, State to)	{
        super(to);
        if (max < min) {
            this.max = min; this.min = max;
        } else {
            this.min = min; this.max = max;
        }
    }

    public ByteTransition(byte max, State to) {
        this(max, max, to);
    }

    @Override
    public int min(int newMin) {
        return this.min = (byte)newMin;
    }


    public byte getMin() {
        return min;
    }

    @Override
    public int min() {
        return min;
    }

    @Override
    public int max() {
        return max;
    }
}
