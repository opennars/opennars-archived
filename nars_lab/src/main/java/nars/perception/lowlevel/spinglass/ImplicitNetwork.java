package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by r0b3 on 14.01.2016.
 */
public class ImplicitNetwork {
    public List<SpatialDot> spatialDots = new ArrayList<>();

    public float weakenScale = 0.2f;

    public void step() {
        addPerceptionToSpin();
        pertubeSpin();
        interactSpin();
        spreadSpin();
        weakenSpin();
    }

    private void spreadSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            for( int ib = ia+1; ib < spatialDots.size(); ib++) {
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

    private void addPerceptionToSpin() {
        // for testing we just add some vectors

        // TODO< calc from simple lines as a dummy input >

        spatialDots.get(0).spinAttributes.get(0).direction = spatialDots.get(0).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{1.0, 0.0}));
        spatialDots.get(1).spinAttributes.get(0).direction = spatialDots.get(1).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{1.0, 0.0}));
        spatialDots.get(2).spinAttributes.get(0).direction = spatialDots.get(2).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{0.0, 1.0}));
    }

    private void weakenSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).direction = new ArrayRealVector(spatialDots.get(ia).spinAttributes.get(0).direction.mapMultiply(weakenScale));
        }
    }

    private void interactSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            for (int ib = ia + 1; ib < spatialDots.size(); ib++) {
                ArrayRealVector dotAPosition = spatialDots.get(ia).spatialPosition;
                ArrayRealVector dotBPosition = spatialDots.get(ib).spatialPosition;

                double distance = dotAPosition.getDistance(dotBPosition);
                if( !isDistanceBelowInfluenceDistance(distance) ) {
                    continue;
                }

                double strengthBetweenSpins = calcStrengthByDistance(distance);

                double forceBetweenNeedles = calcForceBetweenNeedles(spatialDots.get(ia).spinAttributes.get(0).direction, spatialDots.get(ib).spinAttributes.get(0).direction);

                double realForceBetweenNeedles = strengthBetweenSpins * forceBetweenNeedles * needleForce;

                ArrayRealVector normalizedDirectionBetweenDots;
                {
                    double length = Math.sqrt(spatialDots.get(ia).spatialPosition.dotProduct(spatialDots.get(ib).spatialPosition));
                    normalizedDirectionBetweenDots = new ArrayRealVector(spatialDots.get(ib).spatialPosition.subtract(spatialDots.get(ia).spatialPosition));
                }

                ArrayRealVector normalizedOrthogonalDirectionBetweenDots = new ArrayRealVector(new double[]{-normalizedDirectionBetweenDots.getDataRef()[1], normalizedDirectionBetweenDots.getDataRef()[0]});

                double realForceBetweenNeedlesInDirection = Math.max(0.0, realForceBetweenNeedles);
                double realForceBetweenNeedlesInOrthogonalDirection =  Math.max(0.0, -realForceBetweenNeedles);

                spatialDots.get(ia).spinAttributes.get(0).addTwoWayToDirection(new ArrayRealVector(normalizedDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInDirection)));
                spatialDots.get(ib).spinAttributes.get(0).addTwoWayToDirection(new ArrayRealVector(normalizedDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInDirection)));

                spatialDots.get(ia).spinAttributes.get(0).addTwoWayToDirection(new ArrayRealVector(normalizedOrthogonalDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInOrthogonalDirection)));
                spatialDots.get(ib).spinAttributes.get(0).addTwoWayToDirection(new ArrayRealVector(normalizedOrthogonalDirectionBetweenDots.mapMultiply(realForceBetweenNeedles*realForceBetweenNeedlesInOrthogonalDirection)));
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
        return random.nextDouble() * 2.0 - 1.0;
    }

    private double calcStrengthBetweenPositions(ArrayRealVector a, ArrayRealVector b) {
        double distance = a.getDistance(b);
        if( isDistanceBelowInfluenceDistance(distance) ) {
            return calcStrengthByDistance(distance);
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
