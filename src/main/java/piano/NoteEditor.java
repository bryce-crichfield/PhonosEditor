package piano;

import javafx.beans.property.ObjectProperty;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;


public class NoteEditor {
    private final ObjectProperty<GridInfo> gridInfo;
    private final Rectangle background;
    private final Group world;
    private final List<Note> notes;
    private EditorTool currentTool = new SelectTool();

    public NoteEditor(Pane parent, ObjectProperty<GridInfo> gridInfo) {
        this.gridInfo = gridInfo;

        notes = new ArrayList<>();

        // Create a camera to view the 3D shapes
        Camera camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Configure the background of the editor
        var gi = gridInfo.get();
        background = new Rectangle(gi.getColumns() * gi.getCellWidth(),
                                   gi.getRows() * gi.getCellHeight()
        );
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
        scene.widthProperty().bind(parent.widthProperty());
        scene.heightProperty().bind(parent.heightProperty());

        scene.setFill(Color.BLACK.brighter());
        scene.setCamera(camera);

        scene.setManaged(false);
        scene.setRoot(world);

        // Delegate mouse events to the current tool
        scene.setOnMousePressed(mouseEvent -> currentTool.onMouseEvent(mouseEvent));
        scene.setOnMouseReleased(mouseEvent -> currentTool.onMouseEvent(mouseEvent));
        scene.setOnMouseDragged(mouseEvent -> currentTool.onMouseEvent(mouseEvent));

        // Add the subscene to the parent
        parent.getChildren().add(scene);
    }

    public ImagePattern createGridLineFill() {
        // TODO: Doesn't support large grid sizes
        GridInfo gridInfo = this.gridInfo.get();
        double width = gridInfo.getColumns() * gridInfo.getCellWidth();
        double height = gridInfo.getRows() * gridInfo.getCellHeight();
        double gridWidth = gridInfo.getCellWidth();
        double gridHeight = gridInfo.getCellHeight();
        double rows = gridInfo.getRows();
        double columns = gridInfo.getColumns();


        Canvas canvas = new Canvas(width, height);
        var gc = canvas.getGraphicsContext2D();

        // Draw the background every 16 columns alternating between dark and light
        boolean dark = false;
        for (int col = 0; col < columns; col += 16) {
            if (dark) {
                gc.setFill(Color.DARKGRAY.darker().darker().darker().darker().darker());
            } else {
                gc.setFill(Color.DARKGRAY.darker().darker().darker());
            }
            gc.fillRect(col * gridWidth, 0, 16 * gridWidth, height);
            dark = !dark;
        }

        // Darken the black keys
        boolean[] keyMask = {true, false, true, false, true, true, false, true, false, true, false, true, true};

        Color darkerFill = Color.DARKGRAY.deriveColor(1, 1, 1, 0.1);
        gc.setFill(darkerFill);
        int keyIndex = 12;
        for (int row = (int) (rows - 1); row >= 0; row--) {
            int y = (int) (row * gridHeight);
            if (keyMask[keyIndex]) {
                gc.fillRect(0, y, width, gridHeight);
            }

            if (keyIndex == 0) {
                keyIndex = 11;
            } else {
                keyIndex--;
            }
        }

        // Draw the vertical lines
        Color vertLineLight = Color.DARKGRAY.darker();
        Color vertLineDark = Color.DARKGRAY.darker().darker().darker().darker().darker();
        for (int col = 0; col < columns; col++) {
            if (col % 4 == 0) {
                gc.setStroke(vertLineLight);
            } else {
                gc.setStroke(vertLineDark);
            }
            gc.strokeLine(col * gridWidth, 0, col * gridWidth, height);
        }

        // Draw the horizontal lines
        for (int row = 0; row < rows; row++) {
            gc.strokeLine(0, row * gridHeight, width, row * gridHeight);
        }


        Image image = canvas.snapshot(new SnapshotParameters(), null);
        return new ImagePattern(image, 0, 0, width, height, false);
    }

    public Rectangle getBackground() {
        return background;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setTool(EditorTool tool) {
        currentTool = tool;
    }

    public void scrollX(double deltaX) {
        world.setTranslateX(deltaX);
    }

    public void scrollY(double deltaY) {
        world.setTranslateY(deltaY);
    }

    public ObjectProperty<GridInfo> getGridInfo() {
        return gridInfo;
    }

    public void addNote(Note note) {
        notes.add(note);
        world.getChildren().add(note);
    }
}
