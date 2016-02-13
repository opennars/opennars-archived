package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 *
 */
public class Test0 {
    public static void main(String[] args) {
        Test0 test0 = new Test0();

        test0.test();

    }

    public void test() {
        Random random = new Random();

        network.maxInfluenceDistance = 1.5;
        network.influenceStrengthFactor = 1.0f;

        Avalanche avalanche = new Avalanche();
        avalanche.neightborSearchRadius = network.maxInfluenceDistance;
        avalanche.minimalDotResultForIgnition = 0.5;
        avalanche.minimalStrengthForIgnition = 0.5;

        // init test network
        buildTestgrid(10, 10, 1.0, 1.0);




        int numberOfSteps = 50;

        System.out.println("ListAnimate[{");

        for( int stepI = 0; stepI < numberOfSteps; stepI++) {
            drawTestshape((double)stepI / (double)numberOfSteps);

            network.step();

            network.resetIgnitions();
            // avalances
            {
                int startIgnitionCandidate = avalanche.searchRandomIgnitionCandidate(network.spatialDots, random);
                if( startIgnitionCandidate != -1 ) {
                    List<SpatialDot> ignitedDots = avalanche.ignite(network.spatialDots, startIgnitionCandidate);
                }
            }


            System.out.println("Graphics[{");

            for( int dotI = 0; dotI < network.spatialDots.size(); dotI++ ) {
                boolean wasIgnited = network.spatialDots.get(dotI).wasIgnited;
                if( wasIgnited ) {
                    System.out.print("Red,");
                }
                else {
                    System.out.print("Black,");
                }

                ArrayRealVector position = network.spatialDots.get(dotI).spatialPosition;
                ArrayRealVector direction = network.spatialDots.get(dotI).spinAttributes.get(0).direction;


                System.out.print("Line[{");

                DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
                otherSymbols.setDecimalSeparator('.');
                //otherSymbols.setGroupingSeparator('.');

                DecimalFormat df = new DecimalFormat("#.###", otherSymbols);


                System.out.print(String.format("{%s,%s},", df.format(position.getDataRef()[0]), df.format(position.getDataRef()[1])));
                ArrayRealVector positionPlusDirection = position.add(direction);
                System.out.print(String.format("{%s,%s}", df.format(positionPlusDirection.getDataRef()[0]), df.format(positionPlusDirection.getDataRef()[1])));
                System.out.print("}]");

                if (dotI != network.spatialDots.size() - 1) {
                    System.out.print(",");
                }

            }

            System.out.println("}, PlotRange -> {{-1, 11}, {-1, 11}}]");

            if( stepI != numberOfSteps-1 ) {
                System.out.println(",");
            }

            System.out.println("");
        }

        System.out.println("}]");
    }

    private void drawTestshape(double relativeTime) {
        double cos = Math.cos(relativeTime * 2.0 * Math.PI);
        double sin = Math.sin(relativeTime * 2.0 * Math.PI);

        double radius = 4.0;
        ArrayRealVector scaledRotation = new ArrayRealVector(new ArrayRealVector(new double[]{cos, sin}).mapMultiply(radius));

        ArrayRealVector centerPosition = new ArrayRealVector(new double[]{5.0, 5.0});

        ArrayRealVector a = centerPosition.add(scaledRotation);
        ArrayRealVector b = centerPosition.subtract(scaledRotation);

        double maxDistance = 0.5;
        TestHelper.additiveLine(network.spatialDots, a, b, maxDistance*maxDistance);

    }

    private void buildTestgrid(int sizeX, int sizeY, double spacingX, double spacingY) {
        for( int iy = 0; iy < sizeY; iy++ ) {
            for( int ix = 0; ix < sizeX; ix++ ) {
                double positionX = (double)ix * spacingX;
                double positionY = (double)iy * spacingY;

                SpatialDot createdSpatialDot = new SpatialDot();
                createdSpatialDot.spatialPosition = new ArrayRealVector(new double[]{positionX, positionY});
                createdSpatialDot.spinAttributes.add(new SpinAttribute());
                createdSpatialDot.spinAttributes.get(0).direction = new ArrayRealVector(new double[]{0.0, 0.0});
                network.spatialDots.add(createdSpatialDot);
            }
        }
    }

    public ImplicitNetwork network = new ImplicitNetwork();
}
