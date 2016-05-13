package ptrman.difficultyEnvironment.interactionComponents;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 *
 */
public class NarsSurface3dControllerComponent implements IComponent {
    public Simple3dPhysicsComponent simple3dPhysicsComponent;
    public DirectionComponent directionComponent;

    public double maxVelocity = 0.0;

    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {

    }

    public void thrust() {
        double x = Math.cos(directionComponent.angleInRad) * maxVelocity;
        double z = Math.sin(directionComponent.angleInRad) * maxVelocity;

        simple3dPhysicsComponent.velocity = new Vector3D(x, 0.0, z);
    }

    public void stop() {
        simple3dPhysicsComponent.velocity = new Vector3D(0.0, 0.0, 0.0);
    }

    public void rotate(double deltaInRad) {
        directionComponent.angleInRad += deltaInRad;
    }

    @Override
    public String getLongName() {
        return "NarsSurface3dControllerComponent";
    }
}
