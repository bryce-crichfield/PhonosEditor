import atlantafx.base.theme.*;
import component.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import piano.*;

import javax.script.*;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("fxml/MidiEditor.fxml"));
            Parent root = loader.load();
            MidiEditor midiEditor = loader.getController();

            Scene scene = new Scene(root);

            KeybindingsLoader keybindingsLoader = new KeybindingsLoader(scene, midiEditor);
            keybindingsLoader.load();

            stage.setScene(scene);

            loadDefault(scene);
            stage.show();

            stage.setHeight(800);
            stage.setWidth(1200);
        }
    }

    public static void loadDefault(Scene root) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        Font.loadFont(Main.class.getResourceAsStream("/fonts/WhiteRabbit.ttf"), 10);
    }
}
