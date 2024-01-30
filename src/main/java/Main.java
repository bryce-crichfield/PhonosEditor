import config.Theme;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;
import piano.*;
import util.*;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Theme.load();

            FXMLLoader loader = FxUtil.load("/fxml/Editor.fxml");
            Parent root = loader.load();
            Editor editor = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setHeight(1200);
            stage.setWidth(800);
            stage.show();
        }
    }
}
