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
}
