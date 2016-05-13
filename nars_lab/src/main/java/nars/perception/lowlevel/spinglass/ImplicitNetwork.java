package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class ImplicitNetwork {
    public List<SpatialDot> spatialDots = new ArrayList<>();

    public float weakenScale = 0.2f;
    public float influenceStrengthFactor;

    public void initialize() {
        initializeNeightbors();
    }

    private void initializeNeightbors() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            SpatialDot spatialDotUnderInvestigation = spatialDots.get(ia);

            for (int ib = 0; ib < spatialDots.size(); ib++) {
                if( ia == ib ) {
                    continue;
                }

                ArrayRealVector dotAPosition = spatialDotUnderInvestigation.spatialPosition;
                ArrayRealVector dotBPosition = spatialDots.get(ib).spatialPosition;

                double distance = dotAPosition.getDistance(dotBPosition);
                if( !isDistanceBelowInfluenceDistance(distance) ) {
                    continue;
                }

                spatialDotUnderInvestigation.neightborIndices.add(ib);
            }
        }
    }

    public void step() {
        pertubeSpin();

        copyDirections();
        interactSpin();
        swapDirections();

        spreadSpin();

        weakenSpin();
    }

    public void resetIgnitions() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).wasIgnited = false;
        }
    }

    private void copyDirections() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).nextDirection = spatialDots.get(ia).spinAttributes.get(0).direction.copy();
        }
    }

    private void swapDirections() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).swap();
        }
    }

    private void spreadSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            for( int ib : spatialDots.get(ia).neightborIndices ) {
                double strengthBetweenSpins = calcStrengthBetweenPositions(spatialDots.get(ia).spatialPosition, spatialDots.get(ib).spatialPosition);

                // add spin

                spatialDots.get(ia).spinAttributes.get(0).direction = spatialDots.get(ia).spinAttributes.get(0).direction.add(spatialDots.get(ib).spinAttributes.get(0).direction.mapMultiply(strengthBetweenSpins));
            }
        }
    }

    // adds noise to all spins
    private void pertubeSpin() {
        double scale = 0.1f;

        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).direction = spatialDots.get(ia).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{getRandomDirection()*scale, getRandomDirection()*scale}));
        }
    }

    private void weakenSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).direction = new ArrayRealVector(spatialDots.get(ia).spinAttributes.get(0).direction.mapMultiply(weakenScale));
        }
    }

    private void interactSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            for( int ib : spatialDots.get(ia).neightborIndices ) {
                ArrayRealVector dotAPosition = spatialDots.get(ia).spatialPosition;
                ArrayRealVector dotBPosition = spatialDots.get(ib).spatialPosition;

                double distance = dotAPosition.getDistance(dotBPosition);
                if( !isDistanceBelowInfluenceDistance(distance) ) {
                    continue;
                }

                double strengthBetweenSpins = calcStrengthByDistance(distance);

                double forceBetweenNeedles = calcForceBetweenNeedles(Helper.normalizeWithExceptionAsZeroVector(spatialDots.get(ia).spinAttributes.get(0).direction), Helper.normalizeWithExceptionAsZeroVector(spatialDots.get(ib).spinAttributes.get(0).direction));

                double realForceBetweenNeedles = strengthBetweenSpins * forceBetweenNeedles * needleForce;

                ArrayRealVector normalizedDirectionBetweenDots = Helper.normalize(spatialDots.get(ib).spatialPosition.subtract(spatialDots.get(ia).spatialPosition));
                ArrayRealVector normalizedOrthogonalDirectionBetweenDots = new ArrayRealVector(new double[]{-normalizedDirectionBetweenDots.getDataRef()[1], normalizedDirectionBetweenDots.getDataRef()[0]});

                double realForceBetweenNeedlesInDirection = Math.max(0.0, realForceBetweenNeedles);
                double realForceBetweenNeedlesInOrthogonalDirection =  Math.max(0.0, -realForceBetweenNeedles);

                spatialDots.get(ia).spinAttributes.get(0).addTwoWayToNextDirection(new ArrayRealVector(normalizedDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInDirection)));
                spatialDots.get(ib).spinAttributes.get(0).addTwoWayToNextDirection(new ArrayRealVector(normalizedDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInDirection)));

                spatialDots.get(ia).spinAttributes.get(0).addTwoWayToNextDirection(new ArrayRealVector(normalizedOrthogonalDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInOrthogonalDirection)));
                spatialDots.get(ib).spinAttributes.get(0).addTwoWayToNextDirection(new ArrayRealVector(normalizedOrthogonalDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInOrthogonalDirection)));
            }
        }
    }

    // calculates forces between needles, without distance
    // a needle can be imagined like two poles in the direction and the reverse, they are attracting between the needles
    // the opposite is true for the vector which lies orthogonal on the direction

    // this leads to a self organisation of the needle directions
    private static double calcForceBetweenNeedles(ArrayRealVector aNormalized, ArrayRealVector bNormalized) {
        // dot product is 1.0 if the direction "look" into the same direction
        // abs of it has the effect that the reverse directions also give 1.0
        double absDot = Math.abs(aNormalized.dotProduct(bNormalized));
        return absDot * 2.0 - 1.0;
    }

    private double getRandomDirection() {
        double randomValue = random.nextDouble();
        return randomValue * 2.0 - 1.0;
    }

    private double calcStrengthBetweenPositions(ArrayRealVector a, ArrayRealVector b) {
        double distance = a.getDistance(b);
        if( isDistanceBelowInfluenceDistance(distance) ) {
            return calcStrengthByDistance(distance) * influenceStrengthFactor;
        }
        return 0.0;
    }

    private static double calcStrengthByDistance(double distance) {
        // we use strength reduction as if it were a signal in 3d space

        return 1.0 / (distance*distance);
    }

    private boolean isDistanceBelowInfluenceDistance(double distance) {
        return distance < maxInfluenceDistance;
    }

    public double maxInfluenceDistance = 1.0;
    public double needleForce = 1.0;

    private Random random = new Random();
}
