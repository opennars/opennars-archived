package nars.perception;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for creating rib files for renderman compatible renderers
 */
public class RibDriver {
    private static double angleDegreeToRadiants(double degree) {
        return (degree / 360.0) * (2.0 * Math.PI);
    }

    public StringBuilder string = new StringBuilder();

    public Camera camera = new Camera();

    public static abstract class SpatialObject {
        public Vector3D position = new Vector3D(new double[]{0.0, 0.0, 0.0});
    }

    public static abstract class Object extends SpatialObject {
        public ArrayRealVector color;

        abstract public void build(StringBuilder string, Vector3D cameraPosition);
    }

    public static class Camera extends SpatialObject {
        public double horizontalRotation = 0.0; // in radiants
    }

    public static class Billboard extends Object {
        public double radiusHorizontal = 0.0;
        public double radiusVertical = 0.0;

        public void build(StringBuilder string, Vector3D cameraPosition) {
            Vector3D diffToCamera = cameraPosition.subtract(position);
            Vector3D diffToCameraNormalize = diffToCamera.normalize();

            Vector3D worldUpVector = new Vector3D(new double[]{0.0, 1.0, 0.0});

            Vector3D sideVector = diffToCameraNormalize.crossProduct(worldUpVector);
            Vector3D upVector = diffToCameraNormalize.crossProduct(sideVector);
            Vector3D sideVectorNormalized = sideVector.normalize();
            Vector3D upVectorNormalized = upVector.normalize();

            Vector3D sideVectorScaled = sideVectorNormalized.scalarMultiply(radiusHorizontal);
            Vector3D upVectorScaled = upVectorNormalized.scalarMultiply(radiusVertical);

            Vector3D edgePosition0 = position.subtract(sideVectorScaled).add(upVectorScaled);
            Vector3D edgePosition1 = position.add(sideVectorScaled).add(upVectorScaled);
            Vector3D edgePosition2 = position.add(sideVectorScaled).subtract(upVectorScaled);
            Vector3D edgePosition3 = position.subtract(sideVectorScaled).subtract(upVectorScaled);

            string.append("TransformBegin\n");

            //string.append(String.format("Translate %s %s %s\n", position.getX(), position.getY(), position.getZ()));

            // TODO< color >
            string.append("Color [1 0 0]\n");
            //string.append("Polygon \"P\" [ -1 -1 0 1 -1 0 1 1 0 -1 1 0 ] \n\n");
            string.append(String.format("Polygon \"P\" [ %s %s %s %s %s %s %s %s %s %s %s %s ] \n\n", edgePosition0.getX(), edgePosition0.getY(), edgePosition0.getZ(), edgePosition1.getX(), edgePosition1.getY(), edgePosition1.getZ(), edgePosition2.getX(), edgePosition2.getY(), edgePosition2.getZ(), edgePosition3.getX(), edgePosition3.getY(), edgePosition3.getZ()));

            string.append("TransformEnd\n");
            string.append("\n");
        }
    }

    public List<Object> objects = new ArrayList<>();

    public String build(String imageName) {
        string.append(String.format("Display \"%s.tif\" \"file\" \"rgb\"\n", imageName));
        string.append("Format 800 600 1\n");
        string.append("Projection \"perspective\" \"fov\" 60\n");

        string.append(String.format("Translate %s %s %s\n", camera.position.getX(), camera.position.getY(), camera.position.getZ()));
        string.append("\n");

        string.append("WorldBegin\n");


        // no idea why we do this
        string.append("LightSource \"ambientlight\" 0 \"intensity\" .5\n");
        string.append(String.format("Rotate %s 1 0 0\n", angleDegreeToRadiants(camera.horizontalRotation)));

        for( Object iterationobject : objects ) {
            iterationobject.build(string, camera.position);
        }

        string.append("WorldEnd\n");

        return string.toString();
    }
}
