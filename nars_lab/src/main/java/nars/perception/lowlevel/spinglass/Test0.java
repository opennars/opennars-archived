package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Created by r0b3 on 14.01.2016.
 */
public class Test0 {
    public static void main(String[] args) {
        ImplicitNetwork network = new ImplicitNetwork();

        // init test network
        network.spatialDots.add(new SpatialDot());
        network.spatialDots.add(new SpatialDot());
        network.spatialDots.add(new SpatialDot());

        network.spatialDots.get(0).spatialPosition = new ArrayRealVector(new double[]{0.0, 0.0});
        network.spatialDots.get(1).spatialPosition = new ArrayRealVector(new double[]{1.0, 0.0});
        network.spatialDots.get(2).spatialPosition = new ArrayRealVector(new double[]{2.0, 0.0});

        network.spatialDots.get(0).spinAttributes.add(new SpinAttribute());
        network.spatialDots.get(1).spinAttributes.add(new SpinAttribute());
        network.spatialDots.get(2).spinAttributes.add(new SpinAttribute());

        network.spatialDots.get(0).spinAttributes.get(0).direction = new ArrayRealVector(new double[]{0.0, 0.0});
        network.spatialDots.get(1).spinAttributes.get(0).direction = new ArrayRealVector(new double[]{0.0, 0.0});
        network.spatialDots.get(2).spinAttributes.get(0).direction = new ArrayRealVector(new double[]{0.0, 0.0});


        for( int stepI = 0; stepI < 10; stepI++) {
            network.step();

            for( int dotI = 0; dotI < network.spatialDots.size(); dotI++ ) {
                ArrayRealVector direction = network.spatialDots.get(dotI).spinAttributes.get(0).direction;

                System.out.print(String.format("{%s,%s}", direction.getDataRef()[0], direction.getDataRef()[1]));
            }

            System.out.println("");
        }
    }
}
