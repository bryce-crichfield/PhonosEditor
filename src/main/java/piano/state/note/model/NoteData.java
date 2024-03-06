package piano.state.note.model;

import lombok.Getter;
import lombok.With;
import piano.view.zoom.GridInfo;
import util.MathUtil;

@With
@Getter
public class NoteData {
    private NotePitch pitch;
    private int startStep;
    private int endStep;
    private int velocity;

    public NoteData(NotePitch pitch, int startStep, int endStep, int velocity) {
        // Enforce invariants
//        startStep = Math.max(startStep, 0);
//        endStep = Math.max(endStep, 0);
//        velocity = (int) Util.clamp(velocity, 0, 100);

        // start must be less than end
        if (startStep > endStep) {
            int temp = startStep;
            startStep = endStep;
            endStep = temp;
        }

        // end must be greater than start
        if (endStep < startStep) {
            int temp = endStep;
            endStep = startStep;
            startStep = temp;
        }

        // duration must be at least 1
        if (endStep - startStep < 1) {
            endStep = startStep + 1;
        }

        this.pitch = pitch;
        this.startStep = startStep;
        this.endStep = endStep;
        this.velocity = velocity;
    }

    public static NoteData from(double x, double y, double width, double height, GridInfo gridInfo) {
        int colStart = (int) Math.floor(x / gridInfo.getBeatDisplayWidth());
        int colEnd = (int) Math.floor((x + width) / gridInfo.getBeatDisplayWidth());
        int rowStart = (int) Math.floor(y / gridInfo.getCellHeight());
        NotePitch pitch = NotePitch.from(rowStart);
        return new NoteData(pitch, colStart, colEnd, 100);
    }

    public double calcXPosOnGrid(GridInfo gridInfo) {
        return startStep * gridInfo.getStepDisplayWidth();
    }

    public double calcYPosOnGrid(GridInfo gridInfo) {
        double mappedIndex = MathUtil.reverse(pitch.getNoteIndex(), 1, 88);
        double y = (mappedIndex - 1) * gridInfo.getCellHeight();
        return gridInfo.snapToGridY(y);
    }

    public double getVelocityAsPercentage() {
        return velocity / 100.0d;
    }

    public NoteData setVelocityAsPercentage(double velocity) {
        return this.withVelocity((int) (velocity * 100));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NoteData other) {
            return pitch.equals(other.pitch) && startStep == other.startStep && endStep == other.endStep && velocity == other.velocity;
        }
        return false;
    }

    public double getDurationInSteps() {
        return endStep - startStep;
    }

    public double calculateDisplayWidth(GridInfo gridInfo) {
        return getDurationInSteps() * gridInfo.getStepDisplayWidth();
    }

    public double calculateDisplayHeight(GridInfo gridInfo) {
        return gridInfo.getCellHeight();
    }
}
