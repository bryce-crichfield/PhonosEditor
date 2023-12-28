package piano.playback;

public interface PlaybackService {
    void play();

    void pause();

    void stop();

    void observe(PlaybackObserver observer);

    void setHead(double head);

    void setTail(double tail);
}
