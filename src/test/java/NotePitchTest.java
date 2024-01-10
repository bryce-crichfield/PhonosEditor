
import org.junit.jupiter.api.Test;
import piano.model.NotePitch;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotePitchTest {
    List<String> generateKeys() {
        String[] keyMask = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                "F#", "G", "G#"};
        List<String> keys = new ArrayList<>();
        int noteIndex = 0;
        int octave = -3;
        for (int i = 0; i < 88; i++) {
            if (keyMask[noteIndex] == ("C")) {
                if (octave < 0) {
                    octave = 1;
                } else {
                    octave++;
                }
            }

            String strOct = octave < 0 ? "0" : String.valueOf(octave);
            keys.add(keyMask[noteIndex] + strOct);

            noteIndex++;
            if (noteIndex == 12) {
                noteIndex = 0;
            }

        }

        return keys;
    }
    @Test
    void testIndexToString() {
        List<String> keys = generateKeys();
        for (int i = 1; i <= 88; i++) {
            String actual = NotePitch.indexToString(i);
            String expected = keys.get(i - 1);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testStringToIndex() {
        List<String> keys = generateKeys();
        for (int i = 1; i <= 88; i++) {
            String key = keys.get(i - 1);
            int actual = NotePitch.stringToNote(key);
            assertEquals(i, actual);
        }
    }

    @Test
    void testBidirectional() {
        for (int i = 1; i <= 88; i++) {
            String key = NotePitch.indexToString(i);
            int actual = NotePitch.stringToNote(key);
            assertEquals(i, actual);
        }
    }
}