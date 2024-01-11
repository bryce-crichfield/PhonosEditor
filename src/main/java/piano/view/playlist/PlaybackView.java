package piano.view.playlist;

import component.ResizableRectangle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import piano.EditorContext;
import piano.tool.EditorTool;
import piano.tool.PlayheadTool;
import piano.util.GridMath;

import java.util.Optional;

public class PlaybackView {
    private final ResizableRectangle resizableRectangle;
    private final PlayHeadView playHeadView;
    private final EditorContext context;

    public PlaybackView(EditorContext context, Group group, DoubleProperty heightProp,
            ObjectProperty<Optional<EditorTool>> currentTool
    ) {
        this.context = context;

        playHeadView = new PlayHeadView(heightProp, context);
        playHeadView.setManaged(false);
        group.getChildren().add(playHeadView);

        resizableRectangle = ResizableRectangle.builder().makeCenterMovableHorizontally().makeRightHandle().build(0, 0, 100,
                                                                                                                                   100, group
        );

        resizableRectangle.setFill(Color.CYAN.deriveColor(1, 1, 1, 0.25));

        resizableRectangle.heightProperty().bind(heightProp);

        resizableRectangle.setOnCenterHandleDragged(() -> {
            var gi = context.getViewSettings().getGridInfo();
            double newX = GridMath.snapToGridX(gi, resizableRectangle.getX());
            resizableRectangle.setX(newX);

            var playback = context.getPlayback();
            // The playback wants units in terms of cells
            playback.setHead(newX / gi.getCellWidth());
            playback.setTail((newX + resizableRectangle.getWidth()) / gi.getCellWidth());
        });

        resizableRectangle.setOnRightHandleDragged(() -> {
            var gi = context.getViewSettings().getGridInfo();
            double newWidth = GridMath.snapToGridX(gi, resizableRectangle.getWidth());
            resizableRectangle.setWidth(newWidth);

            var playback = context.getPlayback();
            // The playback wants units in terms of cells
            playback.setTail((resizableRectangle.getX() + newWidth) / gi.getCellWidth());
        });

        currentTool.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }

            resizableRectangle.setInteractionEnabled(newValue.get() instanceof PlayheadTool);
            playHeadView.setVisible(false);
        });

        context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
            var gi = newValue;
            var playback = context.getPlayback().getState();
            double x = playback.getHead() * gi.getCellWidth();
            double width = (playback.getTail() - playback.getHead()) * gi.getCellWidth();
            resizableRectangle.setX(x);
            resizableRectangle.setWidth(width);
        });
    }


}
