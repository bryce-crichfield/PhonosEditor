package piano.playback;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;

import java.time.Duration;
import java.util.Random;

public class PlaybackService {
    private final ObjectProperty<PlaybackState> playbackState;

    public PlaybackService(ObjectProperty<PlaybackState> playbackState) {
        this.playbackState = playbackState;

        new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                Duration delta = Duration.ofNanos(now - lastTime);
                lastTime = now;

                advance(delta);
            }
        }.start();
    }

    public ObjectProperty<PlaybackState> getPlaybackState() {
        return playbackState;
    }

    public void play() {
        PlaybackState state = playbackState.get();
        playbackState.set(state.withPlaying(true));
    }

    public void pause() {
        PlaybackState state = playbackState.get();
        playbackState.set(state.withPlaying(false));
    }

    public void stop() {
        PlaybackState state = playbackState.get();
        playbackState.set(state.withPlaying(false).withHead(0));
    }

    public void advance(Duration delta) {
        PlaybackState state = playbackState.get();
        double head = state.getHead();
        double tail = state.getTail();
        int tempo = state.getTempo();
        boolean isPlaying = state.isPlaying();

        if (isPlaying) {
            double offset = (delta.toMillis() * tempo / 60000.0);
            double newHead = head + offset;
            if (newHead > tail) {
                newHead = tail;
            }
            playbackState.set(state.withHead(newHead));
        }
    }
}
