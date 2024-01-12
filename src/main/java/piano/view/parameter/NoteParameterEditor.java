package piano.view.parameter;

import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.model.NoteData;
import piano.model.NoteEntry;

import java.util.HashMap;
import java.util.Map;


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
        setHeight(100);
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

        // 125 is accounting for the width of the piano roll (not dynamic)
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
            zOrderParameterViews();
        });

        context.getNotes().onDelete((entry, oldData, newData) -> {
            world.getChildren().removeIf(
                    node -> node instanceof NoteParameterView view && view.getNoteEntry().equals(entry));
            zOrderParameterViews();
        });

        // Perform z-ordering by using toFront() and toBack() on the NoteParameterViews
        context.getNotes().onModify((entry, oldData, newData) -> {
            zOrderParameterViews();
        });
    }

    private void zOrderParameterViews() {
        // Collect all the NoteParameterViews
        var views = world.getChildren().stream()
                .filter(node -> node instanceof NoteParameterView)
                .map(node -> (NoteParameterView) node)
                .toList();

        // Sort the NoteParameterViews by velocity such that higher velocity notes are rendered on top of lower velocity notes
        Map<Integer, Double> velocityMap = new HashMap<>();

        for (NoteParameterView view : views) {
            NoteEntry noteEntry = view.getNoteEntry();
            NoteData noteData = noteEntry.get();
            int index = noteData.getStart();
            double velocity = noteData.getVelocity();
            double currentLowestVelocity = velocityMap.getOrDefault(index, 100.0);

            if (velocity < currentLowestVelocity) {
                velocityMap.put(index, velocity);
                view.toFront();
            }
        }
    }

    public Paint createGridLineFill() {
        double cellWidth = context.getViewSettings().getGridInfo().getCellWidth();
        double backgroundWidth = context.getViewSettings().getGridInfo().getColumns() * cellWidth;
        double backgroundHeight = this.getHeight();

        var canvas = new javafx.scene.canvas.Canvas(backgroundWidth, backgroundHeight);
        var gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.DARKGRAY.darker().darker().darker().darker().darker());
        gc.fillRect(0, 0, backgroundWidth, backgroundHeight);
        gc.setStroke(Color.DARKGRAY.darker().darker().darker().darker());

        for (int i = 0; i < backgroundWidth / cellWidth; i++) {
            gc.strokeLine(i * cellWidth, 0, i * cellWidth, backgroundHeight);
        }

        var image = canvas.snapshot(null, null);
        return new javafx.scene.paint.ImagePattern(image);
    }

    public void scrollX(double v) {
        world.setTranslateX(v);
    }
}
