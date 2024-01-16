package piano.playback;

public interface PlaybackService {
    void play();

    void pause();

    void stop();

    void triggerNote(String noteName);

    void observe(PlaybackObserver observer);

    void observe(NoteTriggerObserver observer);

    void setHead(double head);

    void setTail(double tail);

    PlaybackState getState();
}
