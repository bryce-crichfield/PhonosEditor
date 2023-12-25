package piano.model;

import lombok.Getter;
import lombok.With;
import piano.Util;
import piano.util.GridMath;

@With
@Getter
public class NoteData {
    private int note;
    private int start;
    private int end;
    private int velocity;

    public NoteData(int note, int start, int end, int velocity) {
        // Enforce invariants
        note = (int) Util.clamp(note, 0, 127);
        start = (int) Util.clamp(start, 0, 127);
        end = (int) Util.clamp(end, 0, 127);
        velocity = (int) Util.clamp(velocity, 0, 100);

        // start must be less than end
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        this.note = note;
        this.start = start;
        this.end = end;
        this.velocity = velocity;
    }

    public static NoteData from(double x, double y, double width, double height, GridInfo gridInfo) {
        int colStart = (int) Math.floor(x / gridInfo.getCellWidth());
        int colEnd = (int) Math.floor((x + width) / gridInfo.getCellWidth());
        int rowStart = (int) Math.floor(y / gridInfo.getCellHeight());

        return new NoteData(rowStart, colStart, colEnd, 100);
    }

    public double calcXPosOnGrid(GridInfo gridInfo) {
        double x = start * gridInfo.getCellWidth();
        return GridMath.snapToGridX(gridInfo, x);
    }

    public double calcYPosOnGrid(GridInfo gridInfo) {
        double y = note * gridInfo.getCellHeight();
        return GridMath.snapToGridY(gridInfo, y);
    }

    public double getVelocityAsPercentage() {
        return velocity / 100.0d;
    }

    public NoteData setVelocityAsPercentage(double velocity) {
        return this.withVelocity((int) (velocity * 100));
    }

    public static String noteToString(int note) {
        String[] notes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                          "F#", "G", "G#"};
        int octave = note / 12;
        int noteIndex = note % 12;
        return notes[noteIndex] + octave;
    }

    public static int stringToNote(String note) {
        String[] notes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                          "F#", "G", "G#"};
        int octave = Integer.parseInt(note.substring(note.length() - 1));
        int noteIndex = 0;
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].equals(note.substring(0, note.length() - 1))) {
                noteIndex = i;
                break;
            }
        }
        return octave * 12 + noteIndex;
    }
}
