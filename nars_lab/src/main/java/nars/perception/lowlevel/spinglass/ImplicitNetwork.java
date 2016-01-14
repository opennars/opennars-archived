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
        spreadSpin();
        weakenSpin();
    }

    public void spreadSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            for( int ib = ia+1; ib < spatialDots.size(); ib++) {
                double strengthBetweenSpins = calcStrengthBetweenPositions(spatialDots.get(ia).spatialPosition, spatialDots.get(ib).spatialPosition);

                // add spin

                spatialDots.get(ia).spinAttributes.get(0).direction = spatialDots.get(ia).spinAttributes.get(0).direction.add(spatialDots.get(ib).spinAttributes.get(0).direction.mapMultiply(strengthBetweenSpins));
            }
        }
    }

    // adds noise to all spins
    public void pertubeSpin() {
        double scale = 0.1f;

        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).direction = spatialDots.get(ia).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{getRandomDirection()*scale, getRandomDirection()*scale}));
        }
    }

    public void addPerceptionToSpin() {
        // for testing we just add some vectors

        spatialDots.get(0).spinAttributes.get(0).direction = spatialDots.get(0).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{1.0, 0.0}));
        spatialDots.get(1).spinAttributes.get(0).direction = spatialDots.get(1).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{1.0, 0.0}));
        spatialDots.get(2).spinAttributes.get(0).direction = spatialDots.get(2).spinAttributes.get(0).direction.add(new ArrayRealVector(new double[]{0.0, 1.0}));
    }

    public void weakenSpin() {
        for( int ia = 0; ia < spatialDots.size(); ia++ ) {
            spatialDots.get(ia).spinAttributes.get(0).direction = new ArrayRealVector(spatialDots.get(ia).spinAttributes.get(0).direction.mapMultiply(weakenScale));
        }
    }

    private double getRandomDirection() {
        return random.nextDouble() * 2.0 - 1.0;
    }

    private static double calcStrengthBetweenPositions(ArrayRealVector a, ArrayRealVector b) {
        double distance = a.getDistance(b);
        return calcStrengthByDistance(distance);
    }

    private static double calcStrengthByDistance(double distance) {
        // TODO< limit max distance >

        // we use strength reduction as if it were a signal in 3d space

        return 1.0 / (distance*distance);
    }

    private Random random = new Random();
}
