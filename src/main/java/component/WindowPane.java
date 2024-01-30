package component;

import javafx.beans.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.net.*;
import java.util.*;

public class WindowPane extends TitledPane {
    @FXML
    private BorderPane header;
    @FXML
    private Button closeButton;
    @FXML
    private Label titleLabel;
    Optional<String> titleText;

    private double lastX;
    private double lastY;

    public WindowPane(@NamedArg("titleText") String title) {
        URL fxml = getClass().getResource("/fxml/WindowPane.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setRoot(this);
        loader.setController(this);


        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        closeButton.setOnAction(event -> {
            var stage = (Stage) getScene().getWindow();
            stage.close();
        });

        titleText = Optional.ofNullable(title);
        titleText.ifPresent(titleLabel::setText);

        this.widthProperty().addListener(($0, $1, newWidth) -> {
            header.setPrefWidth(newWidth.doubleValue());
        });

        // whenever the header is dragged, the window should move
        header.setOnMousePressed(event -> {
            lastX = event.getSceneX();
            lastY = event.getSceneY();
        });

        header.setOnMouseDragged(event -> {
            var stage = (Stage) getScene().getWindow();
            stage.setX(event.getScreenX() - lastX);
            stage.setY(event.getScreenY() - lastY);
        });
    }
}
