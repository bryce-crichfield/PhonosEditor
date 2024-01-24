package piano.state.playback;

import lombok.*;

@Data
@With
public class PlaybackState {
    private final double head;
    private final double value;
    private final double tail;
    private final int tempo;
    private final boolean isPlaying;

    public PlaybackState(double head, double tail, int tempo, boolean isPlaying) {
        this.head = head;
        this.tail = tail;
        this.value = head;
        this.tempo = tempo;
        this.isPlaying = isPlaying;
    }

    public PlaybackState(double head, double value, double tail, int tempo, boolean isPlaying) {
        this.head = head;
        this.value = value;
        this.tail = tail;
        this.tempo = tempo;
        this.isPlaying = isPlaying;
    }

    public double getDuration() {
        return tail - head;
    }
}
