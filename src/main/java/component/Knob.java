package component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import util.FxUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class Knob extends StackPane {
    private final KnobController controller;

    public Knob() {
        FXMLLoader loader = FxUtil.load("/fxml/Knob.fxml");
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
