import config.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import piano.Editor;
import util.FxUtil;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            var builder = Configs.builder()
                            .addFactory(Theme.class, Theme.getFactory())
                            .addFactory(Keybindings.class, Keybindings.getFactory())
                    ;
            Configs.load(builder);


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
