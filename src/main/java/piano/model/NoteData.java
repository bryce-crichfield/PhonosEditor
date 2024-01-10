package piano.model;

import lombok.Getter;
import lombok.With;
import piano.Util;
import piano.util.GridMath;

@With
@Getter
public class NoteData {
    private NotePitch pitch;
    private int start;
    private int end;
    private int velocity;

    public NoteData(NotePitch pitch, int start, int end, int velocity) {
        // Enforce invariants
        start = Math.max(start, 0);
        end = Math.max(end, 0);
        velocity = (int) Util.clamp(velocity, 0, 100);

        // start must be less than end
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // end must be greater than start
        if (end < start) {
            int temp = end;
            end = start;
            start = temp;
        }

        // duration must be at least 1
        if (end - start < 1) {
            end = start + 1;
        }

        this.pitch = pitch;
        this.start = start;
        this.end = end;
        this.velocity = velocity;
    }

    public double calcXPosOnGrid(GridInfo gridInfo) {
        double x = start * gridInfo.getCellWidth();
        return GridMath.snapToGridX(gridInfo, x);
    }

    public double calcYPosOnGrid(GridInfo gridInfo) {
        double mappedIndex = Util.reverse(pitch.getNoteIndex(), 1, 88);
        double y = (mappedIndex - 1) * gridInfo.getCellHeight();
        return GridMath.snapToGridY(gridInfo, y);
    }

    public double getVelocityAsPercentage() {
        return velocity / 100.0d;
    }

    public NoteData setVelocityAsPercentage(double velocity) {
        return this.withVelocity((int) (velocity * 100));
    }

    public int getLength() {
        return end - start;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NoteData other) {
            return pitch.equals(other.pitch) && start == other.start && end == other.end && velocity == other.velocity;
        }
        return false;
    }

    public static NoteData from(double x, double y, double width, double height, GridInfo gridInfo) {
        int colStart = (int) Math.floor(x / gridInfo.getCellWidth());
        int colEnd = (int) Math.floor((x + width) / gridInfo.getCellWidth());
        int rowStart = (int) Math.floor(y / gridInfo.getCellHeight());
        NotePitch pitch = NotePitch.from(rowStart);
        return new NoteData(pitch, colStart, colEnd, 100);
    }
}
