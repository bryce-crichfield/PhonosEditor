package piano.view;

import component.*;
import javafx.animation.*;
import piano.*;
import piano.view.settings.*;

import java.time.*;

public class SmoothZoomController {
    private final static double FRICTION = 0.15;
    private final static double ZOOM = 2;
    private final MidiEditorContext context;
    private double horizontalVelocity = 0;
    private double verticalVelocity = 0;
    private double horizontalActual = GridInfo.MIN_CELL_WIDTH;
    private double verticalActual = GridInfo.MIN_CELL_HEIGHT;

    public SmoothZoomController(MidiEditorContext context) {
        this.context = context;
    }

    public void tick(Duration delta) {
        double deltaSeconds = delta.toMillis() / 1000.0;

        horizontalActual += horizontalVelocity * deltaSeconds;
        verticalActual += verticalVelocity * deltaSeconds;

        horizontalVelocity *= Math.pow(FRICTION, deltaSeconds);
        horizontalVelocity = Util.clamp(horizontalVelocity, -50, 50);
        if (Math.abs(horizontalVelocity) < 10) {
            horizontalVelocity = 0;
        }


        verticalVelocity *= Math.pow(FRICTION, deltaSeconds);
        verticalVelocity = Util.clamp(verticalVelocity, -50, 50);
        if (Math.abs(verticalVelocity) < 10) {
            verticalVelocity = 0;
        }

        GridInfo gridInfo = context.getViewSettings().gridInfoProperty().get();
        double oldCellWidth = gridInfo.getBeatDisplayWidth();
        double oldCellHeight = gridInfo.getCellHeight();
        double newCellWidth = Util.clamp(horizontalActual, GridInfo.MIN_CELL_WIDTH, GridInfo.MAX_CELL_WIDTH);
        double newCellHeight = Util.clamp(verticalActual, GridInfo.MIN_CELL_HEIGHT, GridInfo.MAX_CELL_HEIGHT);

        GridInfo newGridInfo = gridInfo.withBeatDisplayWidth(newCellWidth).withCellHeight(newCellHeight);
        context.getViewSettings().setGridInfo(newGridInfo);
    }

    public AnimationTimer spawn() {
        var timer = new AnimationTimer() {
            private Instant last = Instant.now();

            @Override
            public void handle(long now) {
                Instant nowInstant = Instant.now();
                Duration delta = Duration.between(last, nowInstant);
                last = nowInstant;
                tick(delta);
            }
        };

        timer.start();

        return timer;
    }

    public void shoveHorizontal(double direction) {
        if (direction == Double.NaN) {
            return;
        }


        if (!context.getViewSettings().smoothZoomEnabled.get()) {
            horizontalActual += direction * ZOOM;
            return;
        }

        horizontalVelocity += direction * ZOOM;
    }

    public void shoveVertical(double direction) {
        if (direction == Double.NaN) {
            return;
        }

        if (!context.getViewSettings().smoothZoomEnabled.get()) {
            verticalActual += direction * ZOOM;
            return;
        }

        verticalVelocity += direction * ZOOM;
    }
}
