package piano.view.playlist;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.MidiEditorContext;
import piano.tool.EditorTool;

import java.util.Optional;

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

        context.getPlayback().observe(((oldState, newState) -> {
            var tail = newState.getTail();
            var head = newState.getHead();
            var gi = context.getViewSettings().gridInfoProperty().get();
            double x = head * gi.getCellWidth();
            double width = newState.getDuration() * gi.getCellWidth();
            rectangle.setX(x);
            rectangle.setWidth(width);
        }));

        context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
            var gi = newValue;
            var playback = context.getPlayback().getState();
            double x = playback.getHead() * gi.getCellWidth();
            double width = (playback.getTail() - playback.getHead()) * gi.getCellWidth();
            rectangle.setX(x);
            rectangle.setWidth(width);
        });
    }
}
