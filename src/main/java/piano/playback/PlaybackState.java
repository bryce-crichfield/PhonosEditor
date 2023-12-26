package piano.playback;

import lombok.Data;
import lombok.With;

@Data
@With
public class PlaybackState {
    private final double head;
    private final double tail;
    private final int tempo;
    private final boolean isPlaying;

    public PlaybackState(double head, double tail, int tempo, boolean isPlaying) {
        this.head = head;
        this.tail = tail;
        this.tempo = tempo;
        this.isPlaying = isPlaying;
    }
}
