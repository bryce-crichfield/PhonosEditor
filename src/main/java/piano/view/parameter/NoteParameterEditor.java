package piano.view.parameter;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import piano.MidiEditor;
import piano.MidiEditorContext;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.view.settings.ViewSettingsController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


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
        background.setFill(createGridLineFill());
        gridInfo.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getColumns() * newValue.getCellWidth());
            background.setHeight(newValue.getRows() * newValue.getCellHeight());
            background.setFill(createGridLineFill());
        });
        background.setTranslateZ(0);

        // Configure the world and the scene
        world = new Group(background);


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

    private Pane makeViewPropsPanel() {
        // 125 is accounting for the width of the piano roll (not dynamic)
        AnchorPane propsPane = new AnchorPane();
        propsPane.setPrefWidth(125);
        propsPane.setPrefHeight(100);

        VBox vboxProps = new VBox();
        vboxProps.setAlignment(Pos.CENTER);

        // Add options to Zoom Level Combo Box -------------------------------------------------------------------------
        ComboBox<String> comboZoomLevel = new ComboBox<>();
        comboZoomLevel.setPrefWidth(100);
        comboZoomLevel.getItems().addAll("50%", "75%", "100%", "125%", "150%", "200%");
        comboZoomLevel.setValue("100%");
        ChangeListener<String> comboZoomLevelListener = (observable, oldValue, newValue) -> {
            String zoomLevel = comboZoomLevel.getSelectionModel().getSelectedItem();
            double percentage = Double.parseDouble(zoomLevel.replaceAll("[^0-9]", "")) / 100;
            double cellWidth = GridInfo.MAX_CELL_WIDTH * percentage;
            double cellHeight = GridInfo.MAX_CELL_HEIGHT * percentage;
            var gi = context.getViewSettings().gridInfoProperty().get();
            var newGi = gi.withCellWidth(cellWidth).withCellHeight(cellHeight);
            context.getViewSettings().setGridInfo(newGi);
            // TODO: We need to make sure we trigger a scroll in the editor so we maintain the relative position
        };
        comboZoomLevel.valueProperty().addListener(comboZoomLevelListener);
        vboxProps.getChildren().add(comboZoomLevel);

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
