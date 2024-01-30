package piano.view.piano;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import piano.*;

public class PianoRoll extends AnchorPane {
    private final MidiEditorContext context;
    private final Camera camera;
    private final SubScene subScene;
    private final Group world;

    public PianoRoll(MidiEditorContext context) {
        this.context = context;

        // Create a camera to view the 3D shapes
        camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Configure the world and the scene
        world = new Group();
        subScene = new SubScene(world, 0, 0, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

        subScene.setCamera(camera);
        subScene.setManaged(false);
        subScene.setRoot(world);

        subScene.setFill(Color.BLACK);

        this.getChildren().add(subScene);

        for (int i = 0; i < 88; i++) {
            var key = new PianoKey(i, context);
            world.getChildren().add(key);
        }

        setMinWidth(125);
        setMaxWidth(125);
    }

    public void scrollY(double deltaY) {
        world.setTranslateY(deltaY);
    }
}
