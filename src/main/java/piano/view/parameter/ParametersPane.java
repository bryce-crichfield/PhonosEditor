package piano.view.parameter;

import config.Configs;
import config.Theme;
import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.state.note.model.NoteData;
import piano.state.note.model.NoteEntry;

import java.util.HashMap;
import java.util.Map;


public class ParametersPane extends AnchorPane {
    private final EditorContext context;
    Rectangle background;
    Camera camera;
    Group world;

    public ParametersPane(EditorContext context) {
        this.context = context;

        // Create a camera to view the 3D shapes
        camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Create the background surface which spans the entire grid area
        setHeight(100);
        var gridInfo = context.getViewSettings().gridInfoProperty();
        background = gridInfo.get().createRectangle();
        background.setFill(Configs.get(Theme.class).defaultColor);
        gridInfo.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getMeasures() * newValue.getBeatDisplayWidth());
            background.setHeight(newValue.getRows() * newValue.getCellHeight());
        });
        background.setTranslateZ(0);

        // Configure the world and the scene
        world = new Group(background, createGrid());


        HBox hbox = new HBox();
        SubScene scene = new SubScene(world, 0, 0, true, SceneAntialiasing.BALANCED);
        PropertiesView propertiesView = new PropertiesView(context);
        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            propertiesView.setPrefHeight(newValue.doubleValue());
        });
        // from piano roll
        propertiesView.setPrefWidth(125);


        scene.layoutXProperty().bind(this.layoutXProperty().add(propertiesView.widthProperty()));
        scene.layoutYProperty().bind(this.layoutYProperty());
        scene.widthProperty().bind(this.widthProperty().subtract(propertiesView.widthProperty()));
        scene.heightProperty().bind(this.heightProperty());

        scene.setCamera(camera);
        scene.setManaged(false);
        scene.setRoot(world);

        scene.setFill(Color.DARKGRAY.darker().darker().darker().darker().darker());

        hbox.getChildren().add(propertiesView);
        hbox.getChildren().add(scene);
        this.getChildren().add(hbox);

        // Spawn a NoteParameterView for each note in the registry
        context.getNoteService().getRegistry().onCreatedListener((entry, oldData, newData) -> {
            var notePropertyView = new ParameterView(this, entry, context);
            world.getChildren().add(notePropertyView);
            zOrderParameterViews();
        });

        // Perform z-ordering by using toFront() and toBack() on the NoteParameterViews
        context.getNoteService().getRegistry().onModifiedListener((entry, oldData, newData) -> {
            zOrderParameterViews();
        });

        // Remove the NoteParameterView from the world when the note is deleted
        context.getNoteService().getRegistry().onDeletedListener((entry, oldData, newData) -> {
            world.getChildren().removeIf(
                    node -> node instanceof ParameterView view && view.getNoteEntry().equals(entry));
            zOrderParameterViews();
        });

    }

    private void zOrderParameterViews() {
        // Collect all the NoteParameterViews
        var views = world.getChildren().stream()
                .filter(node -> node instanceof ParameterView)
                .map(node -> (ParameterView) node)
                .toList();

        // Sort the NoteParameterViews by velocity such that higher velocity notes are rendered on top of lower velocity notes
        Map<Integer, Double> velocityMap = new HashMap<>();

        for (ParameterView view : views) {
            NoteEntry noteEntry = view.getNoteEntry();
            NoteData noteData = noteEntry.get();
            int index = noteData.getStartStep();
            double velocity = noteData.getVelocity();
            double currentLowestVelocity = velocityMap.getOrDefault(index, 100.0);

            if (velocity < currentLowestVelocity) {
                velocityMap.put(index, velocity);
                view.toFront();
            }
        }
    }

    public Group createGrid() {
        Group verticalLines = new Group();
        var gi = context.getViewSettings().gridInfoProperty();
        gi.addListener(($0, $1, newGi) -> {
            verticalLines.getChildren().clear();

            for (int step = 0; step < newGi.getTotalSnaps(); step++) {
                Rectangle rect = new Rectangle();
                rect.setX(newGi.getBeatDisplayWidth() / newGi.getSnapSize() * step);
                rect.setY(0);
                rect.setWidth(1);
                rect.setHeight(newGi.getTotalHeight());

                rect.setDisable(true);

                Color color = Configs.get(Theme.class).defaultColor;
                if (step % newGi.getSnapSize() == 0)
                    color = Configs.get(Theme.class).defaultColor;
                if (step % (newGi.getSnapSize() * newGi.getTime().getNumerator()) == 0)
                    color = Configs.get(Theme.class).defaultColor;
                rect.setFill(color);

                verticalLines.getChildren().add(rect);
            }
        });

        return verticalLines;
    }


    public void scrollX(double v) {
        world.setTranslateX(v);
    }
}
