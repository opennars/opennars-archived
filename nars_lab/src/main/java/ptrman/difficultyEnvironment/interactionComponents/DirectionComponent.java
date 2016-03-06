package ptrman.difficultyEnvironment.interactionComponents;

import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.JavascriptDescriptor;

/**
 * Just a small Componenet for one way to store a direction of something
 */
public class DirectionComponent implements IComponent {
    public double angleInRad = 0.0;

    @Override
    public void frameInteraction(JavascriptDescriptor javascriptDescriptor, EntityDescriptor entityDescriptor, float timedelta) {

    }

    @Override
    public String getLongName() {
        return "DirectionComponent";
    }
}
