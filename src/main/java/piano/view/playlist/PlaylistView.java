package piano.view.playlist;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.state.tool.EditorTool;
import piano.view.zoom.GridInfo;

import java.util.Optional;

public class PlaylistView {
    private final Rectangle rectangle;
    private final Rectangle playHeadView;
    private final EditorContext context;

    public PlaylistView(EditorContext context, Group group, DoubleProperty heightProp,
                        ObjectProperty<Optional<EditorTool>> currentTool
    ) {
        this.context = context;

        playHeadView = new Rectangle();
        playHeadView.setFill(javafx.scene.paint.Color.CYAN);
        playHeadView.setStroke(javafx.scene.paint.Color.WHITE);
        playHeadView.setOpacity(0.5);
        playHeadView.setWidth(3);
        playHeadView.heightProperty().bind(heightProp);

        context.getPlayback().observe((old, now) -> {
            GridInfo gi = context.getViewSettings().getGridInfo();
            double x = now.getValue() * gi.getBeatDisplayWidth();
            playHeadView.setTranslateX(x);
            playHeadView.toFront();
        });
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
