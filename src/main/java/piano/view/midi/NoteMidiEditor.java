package piano.view.midi;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import piano.*;
import piano.tool.*;
import piano.view.playlist.*;
import piano.view.settings.*;

import java.util.*;


public class NoteMidiEditor extends AnchorPane {
    private final MidiEditorContext context;
    private final Rectangle background;
    private final Group world;
    private final ObjectProperty<Optional<EditorTool>> currentTool;

    public NoteMidiEditor(MidiEditorContext context, ObjectProperty<Optional<EditorTool>> currentTool) {
        this.context = context;
        this.currentTool = currentTool;

        // Create the background grid surface --------------------------------------------------------------------------
        var gridInfo = context.getViewSettings().gridInfoProperty();
        background = gridInfo.get().createRectangle();

        gridInfo.addListener((observable, oldValue, newValue) -> {
            var newRect = newValue.createRectangle();
            background.setWidth(newRect.getWidth());
            background.setHeight(newRect.getHeight());
        });
        background.setTranslateZ(0);

        // Add the surface to the world and configure the scene --------------------------------------------------------
        world = new Group(background, createGrid());

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
        subScene.setOnMouseReleased(mouseEvent -> {
            Optional<EditorTool> nextTool = currentTool.get().map(tool -> tool.onMouseEvent(mouseEvent));
            currentTool.set(nextTool);
        });


        // Bind view to model ------------------------------------------------------------------------------------------
        context.getNoteService().getRegistry().onCreatedListener((entry, oldData, newData) -> {
            var noteMidiView = new NoteMidiView(entry, context, currentTool);
            world.getChildren().add(noteMidiView);
        });

        context.getNoteService().getRegistry().onDeletedListener((entry, oldData, newData) -> {
            world.getChildren().removeIf(
                    node -> node instanceof NoteMidiView view && view.getNoteEntry().equals(entry));
        });

        // Not a fan of how this just adds itself to the world, but it's the only way I could get it to work
        PlaybackView playbackView = new PlaybackView(context, world, background.heightProperty(), currentTool);
    }

    public Group createGrid() {
        Group grid = new Group();
        var gi = context.getViewSettings().gridInfoProperty();
        double rows = gi.get().getRows();
        double columns = gi.get().getColumns();

        // Draw the background using rectangles
        boolean dark = false;
        for (int col = 0; col < columns; col += 16, dark = !dark) {
            Rectangle rect = new Rectangle();
            int finalCol = col;
            gi.addListener((observable, oldValue, newValue) -> {
                rect.setX(newValue.getCellWidth() * finalCol);
                rect.setY(0);
                rect.setWidth(newValue.getColumns() * newValue.getCellWidth());
                rect.setHeight(newValue.getRows() * newValue.getCellHeight());
            });
            rect.setDisable(true);
            rect.setFill(dark ? Theme.BACKGROUND : Theme.BACKGROUND.darker());
            grid.getChildren().add(rect);
        }

        // Draw the vertical lines
        Color vertLineLight = Theme.BACKGROUND.brighter();
        Color vertLineDark = Theme.BACKGROUND.darker().darker();
        for (int col = 0; col < columns; col++) {
            Rectangle rect = new Rectangle();
            int finalCol = col;
            gi.addListener((observable, oldValue, newValue) -> {
                rect.setX(newValue.getCellWidth() * finalCol);
                rect.setY(0);
                rect.setWidth(1);
                rect.setHeight(newValue.getRows() * newValue.getCellHeight());
            });
            rect.setDisable(true);
            rect.setFill(col % 4 == 0 ? vertLineLight : vertLineDark);
            grid.getChildren().add(rect);
        }

        // Draw the horizontal lines
        for (int row = 0; row < rows; row++) {
            Rectangle rect = new Rectangle();
            int finalRow = row;
            gi.addListener((observable, oldValue, newValue) -> {
                rect.setX(0);
                rect.setY(newValue.getCellHeight() * finalRow);
                rect.setWidth(newValue.getColumns() * newValue.getCellWidth());
                rect.setHeight(1);
            });
            rect.setDisable(true);
            rect.setFill(Theme.BACKGROUND.darker().darker());
            grid.getChildren().add(rect);
        }
        return grid;
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
