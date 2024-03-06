package piano.state.playback;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BasePlaybackService implements PlaybackService {
    private final ObjectProperty<PlaybackState> playbackState;
    private final List<NoteTriggerObserver> noteTriggerObservers = new ArrayList<>();

    public BasePlaybackService(ObjectProperty<PlaybackState> playbackState) {
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

    public void advance(Duration delta) {
        PlaybackState state = playbackState.get();
        double head = state.getHead();
        double tail = state.getTail();
        double value = state.getValue();
        int tempo = state.getTempo();
        boolean isPlaying = state.isPlaying();

        if (isPlaying) {
            double deltaBpm = (delta.toMillis() * tempo / 60000.0);
            double newValue = value + deltaBpm;
            if (newValue > tail) {
                newValue = head;
            }

            playbackState.set(state.withValue(newValue));
        }
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
        playbackState.set(state.withPlaying(false).withValue(state.getHead()));
    }

    @Override
    public void triggerNote(String noteName) {
        for (NoteTriggerObserver observer : noteTriggerObservers) {
            observer.accept(noteName);
        }
    }

    @Override
    public void observe(PlaybackObserver observer) {
        playbackState.addListener((observable, oldValue, newValue) -> {
            observer.accept(oldValue, newValue);
        });
    }

    @Override
    public void observe(NoteTriggerObserver observer) {
        noteTriggerObservers.add(observer);
    }

    @Override
    public void setHead(double head) {
        PlaybackState state = playbackState.get();
        playbackState.set(state.withHead(head).withValue(head));
    }

    @Override
    public void setTail(double tail) {
        PlaybackState state = playbackState.get();
        playbackState.set(state.withTail(tail));
    }

    @Override
    public PlaybackState getState() {
        return playbackState.get();
    }
}
