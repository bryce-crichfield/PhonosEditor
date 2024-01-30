package component;

import javafx.fxml.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.net.*;
import java.util.*;

public class Knob extends StackPane {
    private final KnobController controller;
    public Knob() {
        FXMLLoader loader = Fxml.load("/fxml/Knob.fxml");
        loader.setRoot(this);
        loader.setController(new KnobController());

        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        controller = loader.getController();
    }

    public static class KnobController implements Initializable {
        @FXML
        private StackPane self;
        private double angle;
        private double lastX;
        private double lastY;

        public void onMouseDragEntered(MouseDragEvent mouseDragEvent) {
        }

        public void onMouseDragExited(MouseDragEvent mouseDragEvent) {
        }

        public void onMouseDragged(MouseEvent mouseEvent) {
            var width = self.getWidth();
            var centerX = width / 2;
            var height = self.getHeight();
            var centerY = height / 2;

            // rotate about the center of the knob
            var deltaX = mouseEvent.getSceneX() - centerX;
            var deltaY = mouseEvent.getSceneY() - centerY;
            var newAngle = Math.atan2(deltaY, deltaX);
            var deltaAngle = newAngle - angle;
            angle = newAngle;

            // rotate the knob
            var rotation = self.getRotate();
            self.setRotate(rotation + Math.toDegrees(deltaAngle));

        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            self.setOnMouseDragEntered(this::onMouseDragEntered);
            self.setOnMouseDragExited(this::onMouseDragExited);
            self.setOnMouseDragged(this::onMouseDragged);
        }
    }
}
