package piano.view.playlist;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import piano.*;
import piano.tool.*;

import java.util.*;

public class PlaybackView {
    private final Rectangle rectangle;
    private final PlayHeadView playHeadView;
    private final MidiEditorContext context;

    public PlaybackView(MidiEditorContext context, Group group, DoubleProperty heightProp,
            ObjectProperty<Optional<EditorTool>> currentTool
    ) {
        this.context = context;

        playHeadView = new PlayHeadView(heightProp, context);
        playHeadView.setManaged(false);
        group.getChildren().add(playHeadView);

        rectangle = new Rectangle();
        rectangle.setFill(Color.CYAN.deriveColor(1, 1, 1, 0.15));
        rectangle.setWidth(100);
        rectangle.heightProperty().bind(heightProp);
        rectangle.setManaged(false);
        rectangle.setDisable(true);
        group.getChildren().add(rectangle);

        context.getPlayback().observe(((oldState, newState) -> {
            var tail = newState.getTail();
            var head = newState.getHead();
            var gi = context.getViewSettings().gridInfoProperty().get();
            double x = head * gi.getBeatDisplayWidth();
            double width = newState.getDuration() * gi.getBeatDisplayWidth();
            rectangle.setX(x);
            rectangle.setWidth(width);
        }));

        context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
            var gi = newValue;
            var playback = context.getPlayback().getState();
            double x = playback.getHead() * gi.getBeatDisplayWidth();
            double width = (playback.getTail() - playback.getHead()) * gi.getBeatDisplayWidth();
            rectangle.setX(x);
            rectangle.setWidth(width);
        });
    }
}
