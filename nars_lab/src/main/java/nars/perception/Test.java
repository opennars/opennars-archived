package nars.perception;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.convolve.Kernel2D_F32;
import boofcv.struct.image.ImageFloat32;
import nars.op.software.scheme.Util;
import nars.perception.lowlevel.Convolution;
import nars.perception.lowlevel.GaborKernel;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by r0b3 on 04.02.2016.
 */
public class Test {

    public static void main(String[] args) {
        RibDriver ribDriver = new RibDriver();

        RibDriver.Billboard billboard0 = new RibDriver.Billboard();
        billboard0.position = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        billboard0.radius = 1.0f;

        ribDriver.objects.add(billboard0);

        String ribOutput = ribDriver.build();
        PrintWriter out = null;
        try {
            out = new PrintWriter("C:\\users\\r0b3\\temp\\test1.rib");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.print(ribOutput);
        out.close();


        BufferedImage readImage = UtilImageIO.loadImage("C:\\Users\\r0b3\\temp\\scene1.tif");
        // convert to Boofcv float image
        ImageFloat32 convertedInput = ConvertBufferedImage.convertFromSingle(readImage, null, ImageFloat32.class);

        // scale down to range 0.0 .. 1.0
        {
            for( int y = 0; y < convertedInput.height; y++ ) {
                for (int x = 0; x < convertedInput.width; x++) {
                    convertedInput.set(x, y, convertedInput.get(x, y) / 127.0f);
                }
            }
        }

        // TODO< convolution with various directions >

        int kernelWidth = 32;
        float phi = 0.0f; // angle
        float lambda = 0.8f; // scaling 0.9
        float phaseOffset = 0.0f; // fine
        float spatialRatioAspect = 1.0f; // fine
        Kernel2D_F32 kernel = GaborKernel.generateGaborKernel(kernelWidth, phi, lambda, phaseOffset, spatialRatioAspect);

        ImageFloat32 convolutedImage = Convolution.convolution(kernel, convertedInput);

        // dump kernel
        {
            ImageFloat32 kernelImage = new ImageFloat32(kernel.width, kernel.width);

            for( int y = 0; y < kernel.width; y++ ) {
                for( int x = 0; x < kernel.width; x++ ) {
                    float kernelValue = kernel.get(x, y);
                    kernelImage.set(x, y, Math.max(kernelValue * 127.0f, 0.0f));

                    System.out.print(kernelValue);
                    System.out.print(" ");
                }

                System.out.println();
            }

            UtilImageIO.saveImage(kernelImage, "C:\\Users\\r0b3\\temp\\testOpennarsKernel.tif");
        }

        // for correct saving of image
        {
            for( int y = 0; y < convertedInput.height; y++ ) {
                for (int x = 0; x < convertedInput.width; x++) {
                    convolutedImage.set(x, y, Math.max(convolutedImage.get(x, y), 0.0f) );
                }
            }
        }


        UtilImageIO.saveImage(convolutedImage, "C:\\Users\\r0b3\\temp\\testOpennarsConvolution.tif");

        int debug = 0;
    }
}
