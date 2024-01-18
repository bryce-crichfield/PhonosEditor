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
import piano.note.model.*;
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
        Pane propsPane = makeViewPropsPanel();

        scene.layoutXProperty().bind(this.layoutXProperty().add(propsPane.widthProperty()));
        scene.layoutYProperty().bind(this.layoutYProperty());
        scene.widthProperty().bind(this.widthProperty().subtract(propsPane.widthProperty()));
        scene.heightProperty().bind(this.heightProperty());

        scene.setCamera(camera);
        scene.setManaged(false);
        scene.setRoot(world);

        scene.setFill(Color.DARKGRAY.darker().darker().darker().darker().darker());

        hbox.getChildren().add(propsPane);
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
        Group grid = new Group();

        var gi = context.getViewSettings().gridInfoProperty();

        double rows = gi.get().getRows();
        double columns = gi.get().getMeasures();

        // Draw the vertical lines
        Color vertLineLight = Theme.BACKGROUND.brighter();
        Color vertLineDark = Theme.BACKGROUND.darker().darker();

        for (int col = 0; col < columns; col++) {
            Rectangle rect = new Rectangle();
            int finalCol = col;
            gi.addListener((observable, oldValue, newValue) -> {
                rect.setX(newValue.getBeatDisplayWidth() * finalCol);
                rect.setY(0);
                rect.setWidth(1);
                rect.setHeight(newValue.getRows() * newValue.getCellHeight());
            });
            rect.setDisable(true);
            rect.setFill(col % 4 == 0 ? vertLineLight : vertLineDark);
            grid.getChildren().add(rect);
        }

        return grid;
    }

    private Pane makeViewPropsPanel() {
        // 125 is accounting for the width of the piano roll (not dynamic)
        AnchorPane propsPane = new AnchorPane();
        propsPane.setPrefWidth(125);
        propsPane.setPrefHeight(100);

        VBox vboxProps = new VBox();
        vboxProps.setAlignment(Pos.CENTER);

        // Add options to Zoom Level Combo Box -------------------------------------------------------------------------
        Spinner<Integer> measureCount = new Spinner<>(1, 100, 1);
        measureCount.setPrefWidth(100);
        measureCount.setEditable(true);
        vboxProps.getChildren().add(measureCount);
        measureCount.valueProperty().addListener(($0, $1, value) -> {
            GridInfo gridInfo = context.getViewSettings().gridInfoProperty().get();
            gridInfo = gridInfo.withMeasures(value);
            context.getViewSettings().gridInfoProperty().set(gridInfo);
        });


        ComboBox<String> snapSelect = new ComboBox<>();
        snapSelect.setPrefWidth(100);
        snapSelect.getItems().addAll("4/1", "2/1", "1/1", "1/2", "1/3", "1/4", "1/6", "1/8", "1/12", "1/16", "1/32");
        snapSelect.setValue("1/4");
        vboxProps.getChildren().add(snapSelect);
        snapSelect.valueProperty().addListener(($0, $1, value) -> {
            double parsedFraction = Double.parseDouble(value.split("/")[0]) / Double.parseDouble(value.split("/")[1]);
            double inverse = 1 / parsedFraction;
            GridInfo gridInfo = context.getViewSettings().gridInfoProperty().get();
            gridInfo = gridInfo.withSnapSize(inverse);
            context.getViewSettings().gridInfoProperty().set(gridInfo);
        });


        ComboBox<String> parameterSelect = new ComboBox<>();
        parameterSelect.setPrefWidth(100);
        parameterSelect.getItems().addAll("Velocity", "Pitch", "Duration");
        parameterSelect.setValue("Velocity");
        vboxProps.getChildren().add(parameterSelect);

        FontIcon settingsIcon = new FontIcon("mdi-settings");
        settingsIcon.setIconSize(16);
        Button settingsButton = new Button("", settingsIcon);
        settingsButton.setPrefWidth(100);
        settingsButton.setPrefHeight(30);
        settingsButton.setOnAction(event -> {
            // Load the ViewSettings.fxml file and create a new stage for the popup dialog
            FXMLLoader loader = new FXMLLoader();
            loader.setController(new ViewSettingsController(context));
            loader.setLocation(MidiEditor.class.getResource("/ViewSettings.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        });
        vboxProps.getChildren().add(settingsButton);

        propsPane.getChildren().add(vboxProps);
        AnchorPane.setTopAnchor(vboxProps, 10.0);
        AnchorPane.setLeftAnchor(vboxProps, 10.0);
        AnchorPane.setRightAnchor(vboxProps, 10.0);
        AnchorPane.setBottomAnchor(vboxProps, 10.0);

        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            propsPane.setPrefHeight(newValue.doubleValue());
        });

        return propsPane;
    }

    public void scrollX(double v) {
        world.setTranslateX(v);
    }
}
