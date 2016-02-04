package nars.perception.lowlevel;

import boofcv.alg.filter.convolve.GConvolveImageOps;
import boofcv.core.image.border.BorderType;
import boofcv.core.image.border.FactoryImageBorder;
import boofcv.core.image.border.ImageBorder;
import boofcv.struct.convolve.Kernel2D_F32;
import boofcv.struct.image.ImageFloat32;

public class Convolution {
    public ImageFloat32 convolution(Kernel2D_F32 kernel, ImageFloat32 input) {
        ImageFloat32 output = new ImageFloat32(input.width,input.height);

        ImageBorder<ImageFloat32> border = FactoryImageBorder.single(input, BorderType.EXTENDED);
        GConvolveImageOps.convolve(kernel, input, output, border);

        return output;
    }
}
