package util;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public interface DialogUtil {
    static boolean warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        return !alert.getResult().getButtonData().isCancelButton();
    }

    static Optional<Path> file() {
        return file(null);
    }

    static Optional<Path> file(String extension) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileChooser.setInitialFileName("Untitled");
        if (extension != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File", extension));
        }

        File file = fileChooser.showOpenDialog(stage);
        return Optional.ofNullable(file != null ? file.toPath() : null);
    }
}
