import atlantafx.base.theme.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;
import piano.*;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("MidiEditor.fxml"));
            Parent root = loader.load();
            MidiEditor midiEditor = loader.getController();

            Scene scene = new Scene(root);

            KeybindingsLoader keybindingsLoader = new KeybindingsLoader(scene, midiEditor);
            keybindingsLoader.load();

            stage.setScene(scene);
            stage.show();

            stage.setHeight(800);
            stage.setWidth(1200);
        }
    }
}
