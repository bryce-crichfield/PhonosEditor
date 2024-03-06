import config.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import piano.Editor;
import util.FxUtil;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            var loaders = Configs.createLoaders();
            loaders.put(Theme.class, Theme.getFactory());
            loaders.put(Keybindings.class, Keybindings.getFactory());
            Configs.load(loaders);
            System.out.println(Configs.get(Theme.class).background);

            FXMLLoader loader = FxUtil.load("/fxml/Editor.fxml");
            Parent root = loader.load();
            Editor editor = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setHeight(1200);
            stage.setWidth(800);
            editor.initializeKeyBindings();
            stage.show();
        }
    }
}
