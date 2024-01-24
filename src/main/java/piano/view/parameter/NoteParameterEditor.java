package piano.view.parameter;

import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import org.kordamp.ikonli.javafx.*;
import piano.*;
import piano.state.note.model.*;
import piano.view.settings.*;

import java.io.*;
import java.util.*;


public class NoteParameterEditor extends AnchorPane {
    private final MidiEditorContext context;
    Rectangle background;
    Camera camera;
    Group world;

    public NoteParameterEditor(MidiEditorContext context) {
        this.context = context;

        // Create a camera to view the 3D shapes
        camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Create the background surface which spans the entire grid area
        setHeight(100);
        var gridInfo = context.getViewSettings().gridInfoProperty();
        background = gridInfo.get().createRectangle();
        background.setFill(Theme.BACKGROUND);
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
            var notePropertyView = new NoteParameterView(this, entry, context);
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
                    node -> node instanceof NoteParameterView view && view.getNoteEntry().equals(entry));
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

                Color color = Theme.GRAY_3;
                if (step % newGi.getSnapSize() == 0)
                    color = Theme.GRAY_4;
                if (step % (newGi.getSnapSize() * newGi.getTime().getNumerator()) == 0)
                    color = Theme.GRAY_5;
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
