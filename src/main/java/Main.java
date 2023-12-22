import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class Main {
    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("Playlist.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
