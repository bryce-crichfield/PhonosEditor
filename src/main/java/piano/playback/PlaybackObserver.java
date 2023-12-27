package piano.playback;

@FunctionalInterface
public interface PlaybackObserver {
    void accept(PlaybackState oldState, PlaybackState newState);
}
