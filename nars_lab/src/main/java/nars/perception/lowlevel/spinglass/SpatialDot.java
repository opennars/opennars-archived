package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;

/**
 * is a element of a spin network with a spatial position in the receptive field.
 * The field doesn't have to be of 2 dimensions.
 */
public class SpatialDot {
    public ArrayRealVector spatialPosition;

    public List<SpinAttribute> spinAttributes = new ArrayList<>();
    public boolean wasIgnited = false;

    public List<Integer> neightborIndices = new ArrayList<>(); // get calculated once for speeding up the neightbor search
}
