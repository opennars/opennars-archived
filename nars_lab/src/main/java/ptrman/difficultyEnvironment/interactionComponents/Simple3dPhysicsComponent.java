package ptrman.difficultyEnvironment.interactionComponents;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 * Provides a simple dummy "physics" object with a position
 */
public class Simple3dPhysicsComponent implements IComponent {
    public Vector3D position = new Vector3D(0.0, 0.0, 0.0);
    public Vector3D velocity = new Vector3D(0.0, 0.0, 0.0);

    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {
        position = position.add(velocity.scalarMultiply(timedelta));
    }

    @Override
    public String getLongName() {
        return "Simple3dPhysicsComponent";
    }
}
