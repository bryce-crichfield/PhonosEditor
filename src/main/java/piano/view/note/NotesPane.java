package piano.view.note;

import config.Configs;
import config.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.state.note.model.NotePitch;
import piano.state.tool.EditorTool;
import piano.view.playlist.PlaylistView;

import java.util.Optional;


public class NotesPane extends AnchorPane {
    private final EditorContext context;
    private final Rectangle background;
    private final Group world;
    private final ObjectProperty<Optional<EditorTool>> currentTool;

    public NotesPane(EditorContext context, ObjectProperty<Optional<EditorTool>> currentTool) {
        this.context = context;
        this.currentTool = currentTool;

        // Create the background grid surface --------------------------------------------------------------------------
        var gridInfo = context.getViewSettings().gridInfoProperty();
        background = gridInfo.get().createRectangle();
        gridInfo.addListener(($0, $1, newGi) -> {
            var newRect = newGi.createRectangle();
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
            var noteMidiView = NoteViewFactory.create(entry, context, currentTool);
            world.getChildren().add(noteMidiView);
        });

        context.getNoteService().getRegistry().onDeletedListener((entry, oldData, newData) -> {
            world.getChildren()
                    .removeIf(node -> node instanceof NoteView view && view.getNoteEntry().equals(entry));
        });

        // Not a fan of how this just adds itself to the world, but I haven't changed it yet
        PlaylistView playbackView = new PlaylistView(context, world, background.heightProperty(), currentTool);
    }

    public Group createGrid() {
        Group grid = new Group();
        var gi = context.getViewSettings().gridInfoProperty();
        double rows = gi.get().getRows();
        double columns = gi.get().getMeasures();

        // Draw the background grid ------------------------------------------------------------------------------------
        for (int key = 0; key < rows; key++) {
            NotePitch pitch = NotePitch.from(key + 1);
//
            ObjectProperty<Color> colorProp = pitch.getNoteName().contains("#") ?
                    Configs.get(Theme.class).textLight :
                    Configs.get(Theme.class).textDark;

            Rectangle rect = new Rectangle();
            grid.getChildren().add(rect);

            rect.setDisable(true);
            colorProp.addListener((observable, oldValue, newValue) -> {
                System.out.println("Color changed to " + newValue);
                rect.setFill(newValue);
            });
//            rect.fillProperty().bind(colorProp);

            int finalKey = key;
            gi.addListener((observable, oldValue, newGi) -> {
                rect.setY(newGi.getCellHeight() * finalKey);
                rect.setWidth(newGi.createRectangle().getWidth());
                rect.setHeight(newGi.createRectangle().getHeight());
            });

            // Add a border to each rectangle
            rect.setStroke(Configs.get(Theme.class).defaultColor);
            rect.setStrokeWidth(1);
        }

        // Draw the vertical lines -------------------------------------------------------------------------------------
        Group verticalLines = new Group();
        grid.getChildren().add(verticalLines);

        gi.addListener(($0, $1, newGi) -> {
            verticalLines.getChildren().clear();

            for (int step = 0; step < newGi.getTotalSnaps(); step++) {
                Rectangle rect = new Rectangle();
                rect.setX(newGi.getBeatDisplayWidth() / newGi.getSnapSize() * step);
                rect.setY(0);
                rect.setWidth(1);
                rect.setHeight(newGi.getTotalHeight());

                rect.setDisable(true);

                ObjectProperty<Color> colorProp = Theme.defaultColorProperty();
                if (step % newGi.getSnapSize() == 0)
                    colorProp = Configs.get(Theme.class).backgroundGridDark;
                if (step % (newGi.getSnapSize() * newGi.getTime().getNumerator()) == 0)
                    colorProp = Configs.get(Theme.class).backgroundGridLight;
                rect.fillProperty().bind(colorProp);
                verticalLines.getChildren().add(rect);
            }
        });

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
