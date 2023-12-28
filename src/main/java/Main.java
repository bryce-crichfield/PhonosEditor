import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main {
    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("PianoRoll.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            // maximize the window
//            stage.setMaximized(true);
            setStageOnSecondMonitor(stage);
        }
    }

    private static void setStageOnSecondMonitor(Stage stage) {
        // Get the list of screens (monitors)
        Screen secondScreen = null;
        for (Screen screen : Screen.getScreens()) {
            if (!screen.equals(Screen.getPrimary())) {
                secondScreen = screen;
                break;
            }
        }

        if (secondScreen != null) {
            // Get the bounds of the second screen
            Rectangle2D bounds = secondScreen.getBounds();

            // Set the stage position to be on the second screen
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
        }
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
