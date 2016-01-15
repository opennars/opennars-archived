package nars.perception.lowlevel.spinglass;


import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class SpinAttribute {
    ArrayRealVector direction; // with strength

    ArrayRealVector nextDirection; // for swaping

    //public float strengthOverTime;

    public ArrayRealVector calcNormalizedDirection() {
        return Helper.normalize(direction);
    }

    public void swap() {
        direction = nextDirection;
    }

    // interprets the direction as  a two way direction (-direction is a valid direction too)
    public void addTwoWayToNextDirection(ArrayRealVector twoWayDelta) {
        ArrayRealVector normalizedDirection = Helper.normalizeWithExceptionAsZeroVector(nextDirection);
        ArrayRealVector normalizedTwoWayDelta = Helper.normalizeWithExceptionAsZeroVector(twoWayDelta);

        double dotResult = normalizedDirection.dotProduct(normalizedTwoWayDelta);
        if( dotResult > 0.0 ) {
            nextDirection = direction.add(twoWayDelta);
        }
        else {
            nextDirection = direction.subtract(twoWayDelta);
        }
    }
}
