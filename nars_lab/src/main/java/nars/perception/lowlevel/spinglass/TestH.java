package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;
import sun.security.provider.ConfigFile;

import java.util.List;

/**
 * creates a simple hexagonal field and presents a (transformed) H pattern to it
 */
public class TestH {
    public static void main(String[] args) {
        TestH test = new TestH();

        double spacing = 1.0;
        int countX = 5;
        int countY = 3;
        test.createHexagonGrid(new ArrayRealVector(new double[]{0.0, 0.0}),spacing, countX, countY);

        debugSpatialPointPositions(test.network.spatialDots);
    }

    private static void debugSpatialPointPositions(List<SpatialDot> dots) {
        for(SpatialDot iterationDot : dots) {
            System.out.println(String.format("{%s,%s},", iterationDot.spatialPosition.getDataRef()[0], iterationDot.spatialPosition.getDataRef()[1]));
        }
    }

    private void createHexagonGrid(ArrayRealVector offset, double spacing, int countX, int countY) {
        boolean[] patterArray = {true, true, false};

        ArrayRealVector currentOffset = offset.copy();

        double deltaRotatedX = Math.cos((30.0 / 360.0) * (2.0 * Math.PI)) * spacing;
        double deltaRotatedY = Math.sin((30.0 / 360.0) * (2.0 * Math.PI)) * spacing;

        ArrayRealVector skewOffset = new ArrayRealVector(new double[]{deltaRotatedX, deltaRotatedY});

        for( int cy = 0; cy < countY; cy++) {
            for( int additiveIndex = 0; additiveIndex < 3; additiveIndex++) {
                boolean leftFlag =getPatternFromArray(patterArray, additiveIndex+1);
                boolean rightFlag = getPatternFromArray(patterArray, additiveIndex);

                if(leftFlag) {
                    createLine(currentOffset, new ArrayRealVector(new double[]{1.0, 0.0}), deltaRotatedX*2.0, countX);
                }

                if(rightFlag) {
                    createLine(currentOffset.add(skewOffset), new ArrayRealVector(new double[]{1.0, 0.0}), deltaRotatedX*2.0, countX);
                }

                currentOffset = currentOffset.add(new ArrayRealVector(new double[]{0.0, spacing}));
            }

            /*
            createLine(currentOffset, new ArrayRealVector(new double[]{1.0, 0.0}), deltaRotatedX*2.0, countX);


            ArrayRealVector bottomRowOffset = new ArrayRealVector(skeyOffset.mapMultiply(spacing));
            createLine(currentOffset.add(bottomRowOffset), new ArrayRealVector(new double[]{1.0, 0.0}), deltaRotatedX*2.0, countX);

            createLine(currentOffset.add(bottomRowOffset).add(new ArrayRealVector(new double[]{0.0, spacing})), new ArrayRealVector(new double[]{1.0, 0.0}), deltaRotatedX*2.0, countX);

            currentOffset = currentOffset.add(new ArrayRealVector(new double[]{0.0, spacing*2}));
            */
        }
    }

    private static boolean getPatternFromArray(boolean[] array, int index) {
        return array[index % array.length];
    }

    private void createLine(ArrayRealVector startPosition, ArrayRealVector normalizedDirection, double spacing, int count) {
        ArrayRealVector currentPosition = startPosition.copy();

        for( int ci = 0; ci < count; ci++) {
            SpatialDot createdSpatialDot = new SpatialDot();
            createdSpatialDot.spatialPosition = currentPosition.copy();
            createdSpatialDot.spinAttributes.add(new SpinAttribute());
            createdSpatialDot.spinAttributes.get(0).direction = new ArrayRealVector(new double[]{0.0, 0.0});

            network.spatialDots.add(createdSpatialDot);

            currentPosition = currentPosition.add(normalizedDirection.mapMultiply(spacing));
        }
    }

    public ImplicitNetwork network = new ImplicitNetwork();
}
