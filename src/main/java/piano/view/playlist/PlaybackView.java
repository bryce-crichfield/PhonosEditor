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
//        group.getChildren().add(playHeadView);

        rectangle = new Rectangle();
        rectangle.setFill(Color.CYAN.deriveColor(1, 1, 1, 0.15));
        rectangle.setWidth(100);
        rectangle.heightProperty().bind(heightProp);
        rectangle.setManaged(false);
        rectangle.setDisable(true);
        group.getChildren().add(rectangle);

        context.getPlayback().observe((($0, playback) -> {
            var grid = context.getViewSettings().gridInfoProperty().get();
            double x = playback.getHead() * grid.getStepDisplayWidth();
            double width = (playback.getTail() - playback.getHead()) * grid.getStepDisplayWidth();
            rectangle.setX(x);
            rectangle.setWidth(width);
        }));

        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            var playback = context.getPlayback().getState();
            double x = playback.getHead() * grid.getStepDisplayWidth();
            double width = (playback.getTail() - playback.getHead()) * grid.getStepDisplayWidth();
            rectangle.setX(x);
            rectangle.setWidth(width);
        });
    }
}
