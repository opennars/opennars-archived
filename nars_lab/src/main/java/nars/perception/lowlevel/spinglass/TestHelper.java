package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.List;

/**
 *
 */
public class TestHelper {
    // TODO< use method in SpinAttribute to add to direction >
    public static void additiveLine(List<SpatialDot> spatialDots, ArrayRealVector a, ArrayRealVector b, double maxDistanceSquared) {
        ArrayRealVector diff = b.subtract(a);
        ArrayRealVector diffNormalized = Helper.normalize(diff);

        for( SpatialDot iterationSpatialDot : spatialDots ) {
            boolean inRange = Helper.projectInRange(a, diff, iterationSpatialDot.spatialPosition);
            if( !inRange ) {
                continue;
            }

            ArrayRealVector projectedPosition = Helper.projectOntoNotNormalized(a, diffNormalized, iterationSpatialDot.spatialPosition);

            ArrayRealVector positionToProjectedPositionDiff = iterationSpatialDot.spatialPosition.subtract(projectedPosition);
            double distanceSquared = positionToProjectedPositionDiff.dotProduct(positionToProjectedPositionDiff);

            if( distanceSquared > maxDistanceSquared ) {
                continue;
            }

            iterationSpatialDot.spinAttributes.get(0).direction = iterationSpatialDot.spinAttributes.get(0).direction.add(diffNormalized);
        }
    }
}
