package nars.perception.lowlevel.spinglass;


import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class SpinAttribute {
    ArrayRealVector direction; // with strength

    //public float strengthOverTime;

    public ArrayRealVector calcNormalizedDirection() {
        return Helper.normalize(direction);
    }

    // interprets the direction as  a two way direction (-direction is a valid direction too)
    public void addTwoWayToDirection(ArrayRealVector twoWayDelta) {
        ArrayRealVector normalizedDirection = Helper.normalizeWithExceptionAsZeroVector(direction);
        ArrayRealVector normalizedTwoWayDelta = Helper.normalizeWithExceptionAsZeroVector(twoWayDelta);

        double dotResult = normalizedDirection.dotProduct(normalizedTwoWayDelta);
        if( dotResult > 0.0 ) {
            direction = direction.add(twoWayDelta);

            int x = 0;
        }
        else {
            direction = direction.subtract(twoWayDelta);

            int x = 0;
        }
    }
}
