import atlantafx.base.theme.PrimerDark;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import piano.Editor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("PianoRoll.fxml"));
            Parent root = loader.load();
            Editor editor = loader.getController();

            Scene scene = new Scene(root);

            KeybindingsLoader keybindingsLoader = new KeybindingsLoader(scene, editor);
            keybindingsLoader.load();

            stage.setScene(scene);
            stage.show();

            stage.setHeight(800);
            stage.setWidth(1200);
        }
    }
}
