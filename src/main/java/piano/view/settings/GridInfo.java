package piano.view.settings;

import javafx.scene.shape.*;
import lombok.*;
import piano.*;
import piano.util.*;


@With
@Getter
public class GridInfo {
    // 1/32nd notes
    public static final int STEPS_PER_BEAT = 16;
    public static final double MIN_CELL_WIDTH = 32;
    public static final double MIN_CELL_HEIGHT = 32;
    public static final double MAX_CELL_WIDTH = 512;
    public static final double MAX_CELL_HEIGHT = 512;
    private int rows;
    private int measures;
    // the display width of a single beat
    private double beatDisplayWidth;
    private double cellHeight;
    private double snapSize;
    private TimeSignature time;

    public GridInfo(int rows, int measures, double beatDisplayWidth, double cellHeight, double snapSize, TimeSignature time) {
        beatDisplayWidth = Util.clamp(beatDisplayWidth, MIN_CELL_WIDTH, MAX_CELL_WIDTH);
        cellHeight = Util.clamp(cellHeight, MIN_CELL_HEIGHT, MAX_CELL_HEIGHT);

        this.rows = rows;
        this.measures = measures;
        this.beatDisplayWidth = beatDisplayWidth;
        this.cellHeight = cellHeight;
        this.snapSize = snapSize;
        this.time = time;
    }

    public GridInfo(int rows, int measures, double beatDisplayWidth, double cellHeight, double snapSize) {
        this(rows, measures, beatDisplayWidth, cellHeight, snapSize, new TimeSignature(120, 4, 4));
    }

    public Rectangle createRectangle() {
        return new Rectangle(0, 0, getTotalWidth(), getTotalHeight());
    }

    public double getTotalWidth() {
        double totalBeats = measures * time.getNumerator();
        return totalBeats * beatDisplayWidth;
    }

    public double getTotalHeight() {
        return rows * cellHeight;
    }

    public double getTotalSteps() {
        return measures * time.getNumerator() * snapSize;
    }

    public double getStepDisplayWidth() {
        double totalSteps = measures * time.getNumerator() * STEPS_PER_BEAT;
        double totalBeatWidth = getTotalWidth();
        double stepWidth = totalBeatWidth / totalSteps;
        return stepWidth;
    }

    public double getStepsPerSnap() {
        return STEPS_PER_BEAT / snapSize;
    }

    public double snapStepXToNearestStep(double x) {
        return MathUtil.floorToNearest(x, getStepsPerSnap());
    }

    public double snapWorldXToNearestStep(double x) {
        double stepWidth = getStepDisplayWidth();
        double step = x / stepWidth;
        return MathUtil.floorToNearest(step, getStepsPerSnap());
    }

    public double snapToGridY(double y) {
        return MathUtil.floorToNearest(y, cellHeight);
    }

    public double snapInSteps() {
        return (1 / snapSize) * STEPS_PER_BEAT;
    }
}
