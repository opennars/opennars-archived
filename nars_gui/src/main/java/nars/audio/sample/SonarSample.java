package nars.audio.sample;

import com.gs.collections.api.block.function.primitive.FloatToFloatFunction;

public class SonarSample
{
    public final float[] buf;
    public final float rate;
    
    public SonarSample(float[] buf, float rate)
    {
        this.buf = buf;
        this.rate = rate;
        //System.out.println("SonarSample: " + buf.length + " " + rate);
        //System.out.println(Arrays.toString(buf));
    }

    /** digitize provided function at sample rate (ex: 44.1kh) */
    public static SonarSample digitize(FloatToFloatFunction f,  int sampleRate, float duration) {

        int samples = (int)(duration * sampleRate);
        final SonarSample ss = new SonarSample(new float[samples], sampleRate);
        final float[] b = ss.buf;
        float t = 0, dt = 1.0f / sampleRate;
        for (int i = 0; i < samples; i++) {
            b[i] = f.valueOf(t);
            t+=dt;
        }

        return ss;
    }
}