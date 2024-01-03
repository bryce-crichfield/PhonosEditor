package piano.view.midi;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.model.GridInfo;
import piano.tool.EditorTool;
import piano.view.playlist.PlaybackView;
import piano.view.settings.Theme;

import java.util.Optional;


public class NoteMidiEditor extends AnchorPane {
    private final EditorContext context;
    private final Rectangle background;
    private final Group world;
    private final ObjectProperty<Optional<EditorTool>> currentTool = new SimpleObjectProperty<>(Optional.empty());

    public NoteMidiEditor(EditorContext context) {
        this.context = context;

        // Create the background grid surface --------------------------------------------------------------------------
        var gridInfo = context.getViewSettings().gridInfoProperty();
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
        context.getNotes().onCreate((entry, oldData, newData) -> {
            var noteMidiView = new NoteMidiView(entry, context, currentTool);
            world.getChildren().add(noteMidiView);
        });

        context.getNotes().onDelete((entry, oldData, newData) -> {
            world.getChildren().removeIf(
                    node -> node instanceof NoteMidiView view && view.getNoteEntry().equals(entry));
        });

        // Not a fan of how this just adds itself to the world, but it's the only way I could get it to work
        PlaybackView playbackView = new PlaybackView(context, world, background.heightProperty());
    }

    public ImagePattern createGridLineFill() {
        // TODO: Doesn't support large grid sizes
        // TODO: Please rewrite me before I am lost forever :((((
        GridInfo gridInfo = context.getViewSettings().getGridInfo();
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
                gc.setFill(Theme.BACKGROUND);
            } else {
                gc.setFill(Theme.BACKGROUND.darker());
            }
            gc.fillRect(col * gridWidth, 0, 16 * gridWidth, height);
            dark = !dark;
        }

        // Darken the black keys
        boolean[] keyMask = {true, false, true, false, true, true, false, true, false, true, false, true, true};

        Color darkerFill = Theme.BACKGROUND.deriveColor(1, 1, 1, 0.1);
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
        Color vertLineLight = Theme.BACKGROUND.brighter();
        Color vertLineDark = Theme.BACKGROUND.darker().darker();
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


    public Rectangle getBackgroundSurface() {
        return background;
    }

    public void setTool(EditorTool tool) {
        currentTool.set(Optional.of(tool));
        currentTool.get().ifPresent(EditorTool::onEnter);
    }

    public void scrollByX(double deltaX) {
        double nowX = world.getLayoutX();
        world.setTranslateX(nowX + deltaX);
    }

    public void scrollByY(double deltaY) {
        double nowY = world.getLayoutY();
        world.setTranslateY(nowY + deltaY);
    }

    public void scrollToX(double newX) {
        world.setTranslateX(newX);
    }

    public void scrollToY(double deltaY) {
        world.setTranslateY(deltaY);
    }

    public Group getWorld() {
        return world;
    }
}
