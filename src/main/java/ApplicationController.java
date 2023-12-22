import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;

public class ApplicationController {
    public ScrollPane Mixer;
    public AnchorPane PlaylistAnchor;

    public void initialize() {
        HBox mixerTracks = new HBox();
        Mixer.setContent(mixerTracks);
        for (int i = 0; i < 25; i++) {
            try {
                URL mixerTrackTemplate = getClass().getResource("PhonoMixerTrack.fxml");
                FXMLLoader loader = new FXMLLoader(mixerTrackTemplate);
                PhonoMixerTrackController mixerTrack = new PhonoMixerTrackController();
                loader.setController(mixerTrack);

                mixerTracks.getChildren().add(loader.load());
                mixerTrack.trackName.setText("Track " + (i + 1));
            } catch (Exception cause) {
                cause.printStackTrace();
            }
        }

    }
}
