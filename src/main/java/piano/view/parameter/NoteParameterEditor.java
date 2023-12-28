package piano.view.parameter;

import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;


public class NoteParameterEditor extends AnchorPane {
    private final EditorContext context;
    Rectangle background;
    Camera camera;
    Group world;
    public NoteParameterEditor(EditorContext context) {
        this.context = context;

        // Create a camera to view the 3D shapes
        camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Create the background surface which spans the entire grid area
        var gridInfo = context.getViewSettings().gridInfoProperty();
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

        scene.layoutXProperty().bind(this.layoutXProperty().add(125));
        scene.layoutYProperty().bind(this.layoutYProperty());
        scene.widthProperty().bind(this.widthProperty().subtract(125));
        scene.heightProperty().bind(this.heightProperty());

        scene.setCamera(camera);
        scene.setManaged(false);
        scene.setRoot(world);

        scene.setFill(Color.DARKGRAY.darker().darker().darker().darker().darker());

        this.getChildren().add(scene);

        // Spawn a NoteParameterView for each note in the registry
        context.getNotes().onCreate((entry, oldData, newData) -> {
            var notePropertyView = new NoteParameterView(this, entry, context);
            world.getChildren().add(notePropertyView);
        });

        context.getNotes().onDelete((entry, oldData, newData) -> {
            world.getChildren().removeIf(
                    node -> node instanceof NoteParameterView view && view.getNoteEntry().equals(entry));
        });
    }

    public static Paint createGridLineFill() {
        return Color.TRANSPARENT;
    }

    public void scrollX(double v) {
        world.setTranslateX(v);
    }
}
