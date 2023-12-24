package piano.model;

import lombok.Getter;
import lombok.With;

@With
@Getter
public class NoteData {
    private int note;
    private int start;
    private int end;
    private int velocity;

    public NoteData() {
        this.note = 0;
        this.start = 0;
        this.end = 0;
        this.velocity = 0;
    }

    public NoteData(int note, int start, int end, int velocity) {
        this.note = note;
        this.start = start;
        this.end = end;
        this.velocity = velocity;
    }

    public static NoteData from(double x, double y, double width, double height, GridInfo gridInfo) {
        NoteData noteData = new NoteData();

        int colStart = (int) Math.floor(x / gridInfo.getCellWidth());
        int colEnd = (int) Math.floor((x + width) / gridInfo.getCellWidth());
        int rowStart = (int) Math.floor(y / gridInfo.getCellHeight());

        noteData.start = colStart;
        noteData.end = colEnd;
        noteData.note = rowStart;
        noteData.velocity = 100;

        return noteData;
    }

    public int calculateX(GridInfo gridInfo) {
        return (int) (start * gridInfo.getCellWidth());
    }

    public int calculateY(GridInfo gridInfo) {
        return (int) (note * gridInfo.getCellHeight());
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
