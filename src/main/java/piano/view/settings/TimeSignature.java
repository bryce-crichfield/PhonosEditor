package piano.view.settings;

import lombok.*;

@Data
public class TimeSignature {
    // How many beats per minute
    private final int tempo;

    // How many beats per measure
    private final int numerator;

    // What note gets the beat
    private final int denominator;
}
