package piano.view.parameter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import piano.Editor;
import piano.EditorContext;
import piano.view.GraphicsPaneController;
import piano.view.zoom.GridInfo;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class PropertiesView extends AnchorPane {
    private final EditorContext context;
    @FXML
    private ComboBox<String> snapSize;

    @FXML
    private ComboBox<String> parameterType;

    @FXML
    private Button viewSettings;

    public PropertiesView(EditorContext context) {
        URL fxml = getClass().getResource("/fxml/DisplaysPane.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.context = context;

        snapSize.valueProperty().addListener(($0, $1, newSnap) -> changeSnapSize(newSnap));
        parameterType.valueProperty().addListener(($0, $1, newType) -> changeParameterType(newType));
        viewSettings.setOnAction(event -> openViewSettings());
    }

    private void changeSnapSize(String value) {
        Consumer<Double> setSnapSize = (snapSize) -> {
            GridInfo gridInfo = context.getViewSettings().gridInfoProperty().get();
            gridInfo = gridInfo.withSnapSize(snapSize);
            context.getViewSettings().gridInfoProperty().set(gridInfo);
        };

        switch (value) {
            case "Whole" -> setSnapSize.accept(0.25);
            case "Half" -> setSnapSize.accept(0.5);
            case "Quarter" -> setSnapSize.accept(1.0);
            case "Eighth" -> setSnapSize.accept(2.0);
            case "Sixteenth" -> setSnapSize.accept(4.0);
            case "Thirty-second" -> setSnapSize.accept(8.0);
            case "Triplet" -> setSnapSize.accept(3.0);
            case "Quintuplet" -> setSnapSize.accept(5.0);
        }
    }

    private void changeParameterType(String value) {
        switch (value) {
            case "Velocity" -> System.out.println("Velocity");
            case "Pitch" -> System.out.println("Pitch");
            case "Duration" -> System.out.println("Duration");
        }
    }

    private void openViewSettings() {
        // Load the GraphicsPane.fxml file and create a new stage for the popup dialog
        FXMLLoader loader = new FXMLLoader();
        Stage stage = new Stage();
        loader.setController(new GraphicsPaneController(context));
        loader.setLocation(Editor.class.getResource("/fxml/GraphicsPane.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // hide window buttons
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
