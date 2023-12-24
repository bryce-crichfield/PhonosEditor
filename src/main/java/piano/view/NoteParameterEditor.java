package piano.view;

import javafx.beans.property.ObjectProperty;
import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.model.GridInfo;
import piano.model.NoteRegistry;


public class NoteParameterEditor extends AnchorPane {
    public void scrollX(double v) {
        world.setTranslateX(v);
    }

    private final ObjectProperty<GridInfo> gridInfo;
    private final NoteRegistry notes;
    Rectangle background;
    Camera camera;
    Group world;

    public NoteParameterEditor(ObjectProperty<GridInfo> gridInfo, NoteRegistry notes) {
        this.gridInfo = gridInfo;
        this.notes = notes;

        // Create a camera to view the 3D shapes
        camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Create the background surface which spans the entire grid area
        background = gridInfo.get().createRectangle();
        background.setFill(createGridLineFill());
        gridInfo.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getColumns() * newValue.getCellWidth());
            background.setHeight(newValue.getRows() * newValue.getCellHeight());
            background.setFill(createGridLineFill());
        });
        background.setTranslateZ(0);

        // Configure the world and the scene
        world = new Group(background);

        SubScene scene = new SubScene(world, 0, 0, true, SceneAntialiasing.BALANCED);

        scene.layoutXProperty().bind(this.layoutXProperty().add(PianoRoll.DEFAULT_WIDTH));
        scene.layoutYProperty().bind(this.layoutYProperty());
        scene.widthProperty().bind(this.widthProperty().subtract(PianoRoll.DEFAULT_WIDTH));
        scene.heightProperty().bind(this.heightProperty());

        scene.setCamera(camera);
        scene.setManaged(false);
        scene.setRoot(world);

        scene.setFill(Color.DARKGRAY.darker().darker().darker().darker().darker());

        this.getChildren().add(scene);

        // Spawn a NoteParameterView for each note in the registry
        notes.onAdded(entry -> {
            var notePropertyView = new NoteParameterView(this, entry, gridInfo);
            world.getChildren().add(notePropertyView);
        });

        notes.onRemoved(entry -> {
            world.getChildren().removeIf(
                    node -> node instanceof NoteParameterView view && view.getNoteEntry().equals(entry));
        });

    }

    public static javafx.scene.paint.Paint createGridLineFill() {
        return Color.TRANSPARENT;
    }
}
