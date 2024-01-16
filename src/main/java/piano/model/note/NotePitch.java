package piano.model.note;

import lombok.Data;

@Data
public class NotePitch {
    private final String noteName;
    private final int noteIndex;

    public NotePitch(String noteName, int noteIndex) {
        this.noteName = noteName;
        this.noteIndex = noteIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NotePitch other) {
            return noteIndex == other.noteIndex;
        }
        return false;
    }

    public static NotePitch from(String noteName) {
        return new NotePitch(noteName, stringToNote(noteName));
    }

    public static int stringToNote(String note) {
        String[] notes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                "F#", "G", "G#"};

        boolean[] octaveInc = {false, false, false, true, true, true, true, true, true, true, true, true};

        // Extract the octave (any number of digits)
        int octave = Integer.parseInt(note.replaceAll("[^0-9]", ""));
        // Extract the note name (any number of letters)
        String noteName = note.replaceAll("[^(A-Z|#)]", "");
        // Find the index of the note name in the notes array
        int noteIndex = 0;
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].equals(noteName)) {
                noteIndex = i;
                break;
            }
        }
        boolean octInc = octaveInc[noteIndex];
        if (octave == 1 && octInc) {
            octave = 0;
        } else if (octInc) {
            octave -= 1;
        }

        int index = (octave * 12) + noteIndex + 1;
        // Clamp the index to the range 1 to 88
        index = Math.max(1, Math.min(88, index));
        return index;
    }

    public static NotePitch from(int noteIndex) {
        return new NotePitch(indexToString(noteIndex), noteIndex);
    }

    public static String indexToString(int note) {
        // piano notes are indexed from 1 to 88
        note -= 1;
        // Clamp the note to the range 0 to 88
        note = Math.max(0, Math.min(88, note));
        // index 0 represents A0 on the piano
        // index 88 represents C8 on the piano

        // note is the index of the note on the piano
        String[] notes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                "F#", "G", "G#"};

        // octave is the octave of the note
        int noteIndex = note % 12;
        int octave = note <= 2 ? 0 : (int) Math.ceil((note - 2) / 12.0);
        return notes[noteIndex] + octave;
    }
}
