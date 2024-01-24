package piano.state.playback;

public interface NoteTriggerObserver {
    void accept(String noteName);
}
