package piano.view.zoom;

import lombok.Data;
import lombok.With;

@Data
@With
public class TimeSignature {
    // How many beats per minute
    private final int tempo;

    // How many beats per measure
    private final int numerator;

    // What note gets the beat
    private final int denominator;
}
