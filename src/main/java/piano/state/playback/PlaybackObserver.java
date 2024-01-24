package piano.state.playback;

@FunctionalInterface
public interface PlaybackObserver {
    void accept(PlaybackState oldState, PlaybackState newState);
}
