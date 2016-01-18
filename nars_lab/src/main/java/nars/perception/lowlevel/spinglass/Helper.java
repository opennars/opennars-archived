package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Created by r0b3 on 15.01.2016.
 */
public class Helper {
    public static ArrayRealVector normalize(ArrayRealVector input) {
        double length = Math.sqrt(input.dotProduct(input));
        return new ArrayRealVector(input.mapDivide(length));
    }

    public static ArrayRealVector normalizeWithExceptionAsZeroVector(ArrayRealVector input) {
        double length = Math.sqrt(input.dotProduct(input));
        if(length == 0.0) {
            return new ArrayRealVector(new double[]{0.0, 0.0});
        }
        return normalize(input);
    }

    /*

    public static ArrayRealVector projectOntoNormalized(ArrayRealVector start, ArrayRealVector normalizedDirection, ArrayRealVector position) {
        ArrayRealVector diff = position.subtract(start);

        double length = normalizedDirection.dotProduct(diff);
        return start.add(normalizedDirection.mapMultiply(length));
    }
    */
    public static ArrayRealVector projectOntoNotNormalized(ArrayRealVector start, ArrayRealVector direction, ArrayRealVector position) {
        ArrayRealVector diff = position.subtract(start);

        double length = direction.dotProduct(diff);
        return start.add(direction.mapMultiply(length));
    }

    public static boolean projectInRange(ArrayRealVector start, ArrayRealVector delta, ArrayRealVector position) {
        ArrayRealVector diff = position.subtract(start);

        double dotResult = delta.dotProduct(diff);
        return dotResult > 0.0 && dotResult < delta.dotProduct(delta);
    }
}
