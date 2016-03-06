package ptrman.difficultyEnvironment.TestBed;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageFloat32;
import nars.Memory;
import nars.NAR;
import nars.nar.Default;
import nars.perception.RibDriver;
import nars.perception.lowlevel.spinglass.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.difficultyEnvironment.EntityDescriptor;
import ptrman.difficultyEnvironment.Environment;
import ptrman.difficultyEnvironment.JavascriptDescriptor;
import ptrman.difficultyEnvironment.JavascriptEngine;
import ptrman.difficultyEnvironment.interactionComponents.DirectionComponent;
import ptrman.difficultyEnvironment.interactionComponents.NarsSurface3dControllerComponent;
import ptrman.difficultyEnvironment.interactionComponents.Simple3dPhysicsComponent;
import ptrman.difficultyEnvironment.scriptAccessors.ComponentManipulationScriptingAccessor;
import ptrman.difficultyEnvironment.scriptAccessors.EnvironmentScriptingAccessor;
import ptrman.difficultyEnvironment.scriptAccessors.HelperScriptingAccessor;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class FeedWorld {
    private RibDriver ribDriver = new RibDriver();

    private ImageToSpinglass imageToSpinglass;
    private ImplicitNetwork network = new ImplicitNetwork();
    private Avalanche avalanche = new Avalanche();
    private Random random = new Random();

    private Environment environment = new Environment();
    private JavascriptDescriptor javascriptDescriptor;
    private EntityDescriptor characterEntity;

    private NAR nar;
    private NarsInteractionContainer interactionContainer = new NarsInteractionContainer();

    private List<Vector3D> foodPositions = new ArrayList<>();
    private List<Vector3D> poisonPositions = new ArrayList<>();


    // settings
    // SpatialDot spacing distance of the dots in the image
    double spacingOfDots = 6.0;

    // see nars input/output
    float alpha=0.1f; // how often do we send nars a random action when nars didn't do anything?
    int frameCounter = 0;


    public int fps = 30;

    public static void main(String[] args) {
        FeedWorld feedWorld = new FeedWorld();

        feedWorld.initialize();

        for( int frameI = 0; frameI < feedWorld.fps * 20; frameI++ ) {
            feedWorld.step();
        }
    }

    public void initialize() {
        // network


        network.maxInfluenceDistance = spacingOfDots * 1.5;
        network.influenceStrengthFactor = 1.0f;

        int horizontalResolution = 800;
        int verticalResolution = 600;

        double spacingX = spacingOfDots;
        double spacingY = spacingOfDots;

        int sizeX = (int)( (double)horizontalResolution / spacingX );
        int sizeY = (int)( (double)verticalResolution / spacingY );

        Helper.buildGrid(network, sizeX, sizeY, spacingX, spacingY);

        network.initialize();



        // avalance
        avalanche.neightborSearchRadius = network.maxInfluenceDistance;
        avalanche.minimalDotResultForIgnition = 0.5;
        avalanche.minimalStrengthForIgnition = 0.5;


        // image to spinglass
        int kernelWidth = 32;
        imageToSpinglass = new ImageToSpinglass(kernelWidth);


        // rib
        ribDriver.camera.position = new Vector3D(new double[]{0.0, -1.0, 4.0});


        initializeNars();
        initializeDificultyEnvironment();

        seedFoodAndPoison();
    }

    private void seedFoodAndPoison() {
        // y is up down, so we place 4 just to look at for ealy testing

        foodPositions.add(new Vector3D(4.0, 0.0, 0.0));
        foodPositions.add(new Vector3D(-4.0, 0.0, 0.0));
        foodPositions.add(new Vector3D(0.0, 0.0, 4.0));
        foodPositions.add(new Vector3D(0.0, 0.0, -4.0));

    }

    private void initializeNars() {
        nar = new Default(1000, 1, 1, 3);
        nar.onExec(new Action(interactionContainer, 0, "Left"));
        nar.onExec(new Action(interactionContainer, 1, "Right"));
        Memory m = nar.memory;
        m.conceptForgetDurations.setValue(1.0); //better for declarative reasoning tasks: 2
        m.taskLinkForgetDurations.setValue(1.0); //better for declarative reasoning tasks: 4
        m.termLinkForgetDurations.setValue(1.0); //better for declarative reasoning tasks: 10
    }

    private void initializeDificultyEnvironment() {
        javascriptDescriptor = new JavascriptDescriptor();
        javascriptDescriptor.engine = new JavascriptEngine();
        javascriptDescriptor.environmentScriptingAccessor = new EnvironmentScriptingAccessor(environment);
        javascriptDescriptor.helperScriptingAccessor = new HelperScriptingAccessor();
        javascriptDescriptor.componentManipulationScriptingAccessor = new ComponentManipulationScriptingAccessor();


        characterEntity = new EntityDescriptor();
        NarsSurface3dControllerComponent narsSurface3dControllerComponentForCharacter = new NarsSurface3dControllerComponent();
        Simple3dPhysicsComponent simple3dPhysicsComponentForCharacter = new Simple3dPhysicsComponent();
        DirectionComponent directionComponentForCharacter = new DirectionComponent();
        narsSurface3dControllerComponentForCharacter.directionComponent = directionComponentForCharacter;
        narsSurface3dControllerComponentForCharacter.simple3dPhysicsComponent = simple3dPhysicsComponentForCharacter;

        narsSurface3dControllerComponentForCharacter.maxVelocity = 50.0;

        characterEntity.components.addComponent(narsSurface3dControllerComponentForCharacter);
        characterEntity.components.addComponent(simple3dPhysicsComponentForCharacter);
        characterEntity.components.addComponent(directionComponentForCharacter);

        environment.entities.add(characterEntity);
    }

    public void step() {
        boolean verbose = true;

        if( verbose ) {
            System.out.println(String.format("debug{verbose} frame=%s", frameCounter));
        }


        ribDriver.objects.clear();

        Simple3dPhysicsComponent characterPhysicsComponent = (Simple3dPhysicsComponent)characterEntity.components.getComponentByName("Simple3dPhysicsComponent");
        ribDriver.camera.position = characterPhysicsComponent.position;
        DirectionComponent characterDirectionComponent = (DirectionComponent)characterEntity.components.getComponentByName("DirectionComponent");
        ribDriver.camera.horizontalRotation = characterDirectionComponent.angleInRad;

        // update list with billboards of "food" and "poison"

        for( Vector3D foodPosition : foodPositions ) {
            RibDriver.Billboard foodBillboard = new RibDriver.Billboard();
            foodBillboard.position = foodPosition;
            foodBillboard.radiusHorizontal = 0.05;
            foodBillboard.radiusVertical = 1.0;

            ribDriver.objects.add(foodBillboard);
        }

        for( Vector3D poisionPosition : poisonPositions ) {
            RibDriver.Billboard foodBillboard = new RibDriver.Billboard();
            foodBillboard.position = poisionPosition;
            foodBillboard.radiusHorizontal = 0.05;
            foodBillboard.radiusVertical = 1.0;

            ribDriver.objects.add(foodBillboard);
        }




        // render

        String ribOutput = ribDriver.build(String.format("sceneImage%s", frameCounter));
        PrintWriter out = null;
        try {
            out = new PrintWriter("C:\\users\\r0b3\\temp\\test1.rib");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.print(ribOutput);
        out.close();


        // invoke renderer

        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("\"C:\\Program Files (x86)\\Aqsis\\bin\\aqsis.exe\" test1.rib", new String[]{}, new File("C:\\users\\r0b3\\temp\\"));
            pr.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        BufferedImage readImage = UtilImageIO.loadImage(String.format("C:\\Users\\r0b3\\temp\\sceneImage%s.tif", frameCounter));
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


        // convert the image to spinglass representation
        float imageMultiplier = 1.0f;
        imageToSpinglass.read(convertedInput, network.spatialDots, imageMultiplier);



        // perception with spinglass
        List<SimpleLineDescriptor > simpleLineDescriptorsForNars = new ArrayList<>();

        network.step();

        network.resetIgnitions();
        // avalances
        {
            int avalancesToTry = 20;

            for( int avalanceI = 0; avalanceI < avalancesToTry; avalanceI++ ) {
                int startIgnitionCandidate = avalanche.searchRandomIgnitionCandidate(network.spatialDots, random);
                if( startIgnitionCandidate != -1 ) {
                    List<SpatialDot> ignitedDots = avalanche.ignite(network.spatialDots, startIgnitionCandidate);

                    // convert to format easily digestable by nars
                    SimpleLineDescriptor descriptorForLine = FeedWorld.convertDotsToSimpleLineDescriptor(ignitedDots);
                    simpleLineDescriptorsForNars.add(descriptorForLine);
                }
            }
        }


        boolean doDump = true;

        if( doDump ) {

            // dumping for debugging
            StringBuilder dumpBuilder = new StringBuilder();
            Dump.dumpAsMathematicaGraphic(dumpBuilder, network);

            try {
                PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\r0b3\\temp\\mathematicaDebug.txt", true)));
                out2.println(dumpBuilder.toString());
                out2.close();
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
        }


        // TODO< reward calculation >
        // send it to nars and transfer the actions from nars to the world
        narsInteraction(simpleLineDescriptorsForNars, 0.0f);


        // let the deficultyWorld simulate one step
        environment.stepFrame(1.0f / (float)fps, javascriptDescriptor);


        frameCounter++;
    }

    // input and reward
    private void narsInteraction(List<SimpleLineDescriptor > simpleLineDescriptorsForNars, float reward) {
        // simplistic vision for the poor nars

        for( SimpleLineDescriptor iterationSimpleLineDescriptor : simpleLineDescriptorsForNars) {
            // we need to quanisize the information to make nars it easier to reason about it

            int quantisizedStartPositionX = (int)( (800.0 / iterationSimpleLineDescriptor.startPositionInImageSpace.getDataRef()[0]) * 6.0 );
            int quantisizedStartPositionY = (int)( (600.0 / iterationSimpleLineDescriptor.startPositionInImageSpace.getDataRef()[1]) * 4.0 );

            String s;

            if( iterationSimpleLineDescriptor.type == SimpleLineDescriptor.EnumType.HORIZONTAL ) {
                int quanisizedWidth = (int)( (800.0 / iterationSimpleLineDescriptor.lengthInImageSpace) * 6.0 * 2.0 );

                s = String.format("<(*, %s, %s, %s)-->horizontal>. :|:", quantisizedStartPositionX, quantisizedStartPositionY, quanisizedWidth);
            }
            else {
                int quanisizedHeight = (int)( (800.0 / iterationSimpleLineDescriptor.lengthInImageSpace) * 6.0 * 2.0 );

                s = String.format("<(*, %s, %s, %s)-->vertical>. :|:", quantisizedStartPositionX, quantisizedStartPositionY, quanisizedHeight);
            }

            System.out.println("perception: " + s);
            nar.input(s);

        }

        interactionContainer.triggeredActionIds.clear();


        if((frameCounter % 10) == 0 ) {
            nar.input("<SELF --> [good]>! :|:");
            System.out.println("food urge input");
        }
        if(reward > 0) {
            System.out.println("good mr_nars");
            nar.input("<SELF --> [good]>. :|:");
        }
        if(reward < 0) {
            System.out.println("bad mr_nars");
            nar.input("(--,<SELF --> [good]>). :|:");
        }

        for(int i=0;i<250;i++) { //let NARS decide another action
            nar.frame();
        }

        // if NAR hasn't decided chose a random action
        if(interactionContainer.triggeredActionIds.size()==0 && random.nextFloat()<alpha) {
            int lastAction = random.nextInt(2);
            if(lastAction == 0) {
                System.out.println("random left");
                nar.input("Left(SELF). :|:");
            }
            if(lastAction == 1) {
                System.out.println("random right");
                nar.input("Right(SELF). :|:");
            }
        }


        // interpret the actions and send them to the actor

        for( int iterationActionId : interactionContainer.triggeredActionIds ) {
            double rotationVelocityInDegreePerSecond = 20.0f;
            double rotationVelocityInRadPerSecond = ((2.0 * Math.PI) / 360.0) * rotationVelocityInDegreePerSecond;
            double rotationVelocityInRadPerFrame = rotationVelocityInRadPerSecond / (double)fps;

            if( iterationActionId == 0 ) { // left
                NarsSurface3dControllerComponent characterDirectionComponent = (NarsSurface3dControllerComponent)characterEntity.components.getComponentByName("NarsSurface3dControllerComponent");
                characterDirectionComponent.rotate(rotationVelocityInRadPerFrame);
            }
            else if( iterationActionId == 1 ) { // right
                NarsSurface3dControllerComponent characterDirectionComponent = (NarsSurface3dControllerComponent)characterEntity.components.getComponentByName("NarsSurface3dControllerComponent");
                characterDirectionComponent.rotate(-rotationVelocityInRadPerFrame);
            }
        }

    }



    private static class SimpleLineDescriptor {
        public enum EnumType {
            VERTICAL,
            HORIZONTAL
        }

        public EnumType type;
        public ArrayRealVector startPositionInImageSpace;
        public double lengthInImageSpace;
    }

    // tries to figure out if the dots are more vertical or more horizontal distributed
    private static SimpleLineDescriptor convertDotsToSimpleLineDescriptor(List<SpatialDot> dots) {
        ArrayRealVector boundingBoxMin = new ArrayRealVector(new double[]{Double.MAX_VALUE, Double.MAX_VALUE});
        ArrayRealVector boundingBoxMax = new ArrayRealVector(new double[]{-Double.MAX_VALUE, -Double.MAX_VALUE});

        for( SpatialDot iterationDot : dots) {
            boundingBoxMin.getDataRef()[0] = Math.min(boundingBoxMin.getDataRef()[0], iterationDot.spatialPosition.getDataRef()[0]);
            boundingBoxMin.getDataRef()[1] = Math.min(boundingBoxMin.getDataRef()[1], iterationDot.spatialPosition.getDataRef()[1]);

            boundingBoxMax.getDataRef()[0] = Math.max(boundingBoxMax.getDataRef()[0], iterationDot.spatialPosition.getDataRef()[0]);
            boundingBoxMax.getDataRef()[1] = Math.max(boundingBoxMax.getDataRef()[1], iterationDot.spatialPosition.getDataRef()[1]);
        }

        double diffX = boundingBoxMax.getDataRef()[0] - boundingBoxMin.getDataRef()[0];
        double diffY = boundingBoxMax.getDataRef()[1] - boundingBoxMin.getDataRef()[1];

        SimpleLineDescriptor result = new SimpleLineDescriptor();
        if(diffX > diffY) {
            result.type = SimpleLineDescriptor.EnumType.HORIZONTAL;
            result.startPositionInImageSpace = new ArrayRealVector(new double[]{boundingBoxMin.getDataRef()[0], (boundingBoxMin.getDataRef()[1] + boundingBoxMax.getDataRef()[1]) * 0.5});
            result.lengthInImageSpace = diffX;
        }
        else {
            result.type = SimpleLineDescriptor.EnumType.VERTICAL;
            result.startPositionInImageSpace = new ArrayRealVector(new double[]{(boundingBoxMin.getDataRef()[0] + boundingBoxMax.getDataRef()[0]) * 0.5, boundingBoxMin.getDataRef()[1]});
            result.lengthInImageSpace = diffY;
        }

        return result;
    }
}
