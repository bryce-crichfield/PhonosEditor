package piano.view.playlist;

import component.Handle;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import piano.EditorContext;
import piano.util.GridMath;

public class PlaybackView {
    private final Handle handle;
    private final PlayHeadView playHeadView;
    private final EditorContext context;

    public PlaybackView(EditorContext context, Group group, DoubleProperty heightProp) {
        this.context = context;

        playHeadView = new PlayHeadView(heightProp, context);
        playHeadView.setManaged(false);
        group.getChildren().add(playHeadView);

        handle = Handle.builder()
                .makeCenterMovableHorizontally()
                .makeLeftHandle()
                .makeRightHandle()
                .build(0, 0, 100, 100, group);

        handle.setFill(Color.CYAN.deriveColor(1, 1, 1, 0.25));

        handle.heightProperty().bind(heightProp);

        handle.setOnCenterHandleDragged(() -> {
            var gi = context.getViewSettings().getGridInfo();
            double newX = GridMath.snapToGridX(gi, handle.getX());
            handle.setX(newX);

            var playback = context.getPlayback();
            // The playback wants units in terms of cells
            playback.setHead(newX / gi.getCellWidth());
            playback.setTail((newX + handle.getWidth()) / gi.getCellWidth());
        });

        handle.setOnRightHandleDragged(() -> {
            var gi = context.getViewSettings().getGridInfo();
            double newWidth = GridMath.snapToGridX(gi, handle.getWidth());
            handle.setWidth(newWidth);

            System.out.println("newWidth = " + newWidth);

            var playback = context.getPlayback();
            // The playback wants units in terms of cells
            playback.setTail((handle.getX() + newWidth) / gi.getCellWidth());
        });

        handle.setOnLeftHandleDragged(() -> {
            var gi = context.getViewSettings().getGridInfo();
            double newX = GridMath.snapToGridX(gi, handle.getX());
            double newWidth = GridMath.snapToGridX(gi, handle.getWidth());
        });
    }


}
