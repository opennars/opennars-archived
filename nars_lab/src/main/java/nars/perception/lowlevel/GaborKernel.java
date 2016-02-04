package nars.perception.lowlevel;

import boofcv.struct.convolve.Kernel2D_F32;

// from SquareofTwo/PtrMan old old AI project
public class GaborKernel {


    /**
     *
     *
     *
     * \param phi angle in radiants
     * \param spartialRatioAspect ellipticity of the support of the Gabor function
     */
    public static Kernel2D_F32 generateGaborKernel(int width, float phi, float lambda, float phaseOffset, float spartialRatioAspect) {
        Kernel2D_F32 kernel = new Kernel2D_F32(width);

        float xTick, yTick;
        float sigma;
        int xInt, yInt;

        // constant from http://bmia.bmt.tue.nl/Education/Courses/FEV/course/pdf/Petkov_Gabor_functions2011.pdf
        sigma = 0.56f * lambda;

        for( yInt = 0; yInt < width; yInt++ ) {
            for( xInt = 0; xInt < width; xInt++ ) {
                float x, y;
                float insideExp, insideCos;
                float filterValue;

                x = ((float)(xInt - width / 2)/(float)width) * 2.0f;
                y = ((float)(yInt - width / 2)/(float)width) * 2.0f;

                xTick = x * (float)Math.cos(phi) + y * (float)Math.sin(phi);
                yTick = -x * (float)Math.sin(phi) + y * (float)Math.cos(phi);

                insideExp = - (xTick*xTick + spartialRatioAspect*spartialRatioAspect * yTick*yTick)/(2.0f*sigma*sigma);
                insideCos = 2.0f*(float)Math.PI * (xTick/lambda) + phaseOffset;

                filterValue = (float)Math.exp(insideExp) * (float)Math.cos(insideCos);

                kernel.set(xInt, yInt, filterValue);
            }
        }

        return kernel;
    }
}
