package piano.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import piano.control.MemoNoteController;
import piano.model.GridInfo;
import piano.model.NoteRegistry;
import piano.tool.EditorTool;

import java.util.Optional;


public class NoteMidiEditor extends AnchorPane {
    private final ObjectProperty<GridInfo> gridInfo;
    private final Rectangle background;
    private final Group world;
    private final NoteRegistry notes;
    private final ObjectProperty<Optional<EditorTool>> currentTool = new SimpleObjectProperty<>(Optional.empty());
    private final MemoNoteController controller;

    public NoteMidiEditor(ObjectProperty<GridInfo> gridInfo, NoteRegistry noteRegistry) {
        this.gridInfo = gridInfo;
        this.notes = noteRegistry;
        this.controller = MemoNoteController.createInstance(noteRegistry);


        // Create the background grid surface --------------------------------------------------------------------------
        background = gridInfo.get().createRectangle();
        background.setFill(createGridLineFill());
        gridInfo.addListener((observable, oldValue, newValue) -> {
            var newRect = newValue.createRectangle();
            background.setWidth(newRect.getWidth());
            background.setHeight(newRect.getHeight());
            background.setFill(createGridLineFill());
        });
        background.setTranslateZ(0);

        // Add the surface to the world and configure the scene --------------------------------------------------------
        world = new Group(background);

        Camera camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        SubScene subScene = new SubScene(world, 0, 0, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());

        subScene.setCamera(camera);
        subScene.setManaged(false);
        subScene.setRoot(world);

        getChildren().add(subScene);

        // Delegate mouse events to EditorTool -------------------------------------------------------------------------
        subScene.setOnMousePressed(mouseEvent -> currentTool.get().ifPresent(tool -> tool.onMouseEvent(mouseEvent)));
        subScene.setOnMouseMoved(mouseEvent -> currentTool.get().ifPresent(tool -> tool.onMouseEvent(mouseEvent)));
        subScene.setOnMouseDragged(mouseEvent -> currentTool.get().ifPresent(tool -> tool.onMouseEvent(mouseEvent)));
        subScene.setOnMouseReleased(mouseEvent -> currentTool.get().ifPresent(tool -> tool.onMouseEvent(mouseEvent)));

        // Bind view to model ------------------------------------------------------------------------------------------
        notes.onAdded(entry -> {
            var noteMidiView = new NoteMidiView(entry, gridInfo, currentTool);
            world.getChildren().add(noteMidiView);
        });

        notes.onRemoved(entry -> {
            world.getChildren().removeIf(
                node -> node instanceof NoteMidiView view && view.getNoteEntry().equals(entry));
        });
    }

    public ImagePattern createGridLineFill() {
        // TODO: Doesn't support large grid sizes
        // TODO: Please rewrite me before I am lost forever :((((
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

    public MemoNoteController getController() {
        return controller;
    }

    public Rectangle getBackgroundSurface() {
        return background;
    }

    public NoteRegistry getNoteRegistry() {
        return notes;
    }

    public void setTool(EditorTool tool) {
        currentTool.set(Optional.of(tool));
        currentTool.get().ifPresent(EditorTool::onEnter);
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

    public Group getWorld() {
        return world;
    }
}
