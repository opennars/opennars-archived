package nars.perception.lowlevel.spinglass;

import boofcv.struct.convolve.Kernel2D_F32;
import boofcv.struct.image.ImageFloat32;
import nars.perception.lowlevel.Convolution;
import nars.perception.lowlevel.GaborKernel;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.List;

/**
 *
 */
public class ImageToSpinglass {
    private Kernel2D_F32 kernelVertical;
    private Kernel2D_F32 kernelHorizontal;

    // kernelWidth = 32
    public ImageToSpinglass(int kernelWidth) {
        float phi = 0.0f; // angle
        float lambda = 0.8f;
        float phaseOffset = 0.0f;
        float spatialRatioAspect = 1.0f;
        kernelVertical = GaborKernel.generateGaborKernel(kernelWidth, phi, lambda, phaseOffset, spatialRatioAspect);

        phi = (float)(Math.PI / 2.0); // angle
        kernelHorizontal = GaborKernel.generateGaborKernel(kernelWidth, phi, lambda, phaseOffset, spatialRatioAspect);
    }

    public void read(ImageFloat32 input, List<SpatialDot> spatialDots, float strengthMultiplier) {
        ImageFloat32 convolutedImageVertical = Convolution.convolution(kernelVertical, input);
        ImageFloat32 convolutedImageHorizontal = Convolution.convolution(kernelHorizontal, input);

        // readout the strength for the spatialDots

        for( SpatialDot iterationSpatialDot : spatialDots ) {
            int x = (int) iterationSpatialDot.spatialPosition.getDataRef()[0];
            int y = (int) iterationSpatialDot.spatialPosition.getDataRef()[1];

            float horizontalStrength = strengthMultiplier * convolutedImageHorizontal.get(x, y);
            float verticalStrength = strengthMultiplier * convolutedImageVertical.get(x, y);

            iterationSpatialDot.spinAttributes.get(0).direction = new ArrayRealVector(new ArrayRealVector(new double[]{1.0, 0.0}).mapMultiply(horizontalStrength).add(new ArrayRealVector(new double[]{0.0, 1.0}).mapMultiply(verticalStrength)));
        }
    }
}
