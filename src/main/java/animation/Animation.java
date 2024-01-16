package animation;

import javafx.animation.*;

import java.time.*;

public interface Animation {
    default boolean isDead() {
        return !isAlive();
    }

    boolean isAlive();

    default void launchAnimation(Duration duration) {
        AnimationTimer timer = new AnimationTimer() {
            final Duration startTime = Duration.ofNanos(System.nanoTime());
            Duration lastTime = Duration.ofNanos(System.nanoTime());

            @Override
            public void handle(long now) {
                Duration currentTime = Duration.ofNanos(System.nanoTime());
                Duration delta = currentTime.minus(lastTime);
                lastTime = currentTime;

                onTick(currentTime, delta);

                if (!isAlive()) {
                    System.out.println("Animation Over");
                    stop();
                    onDeath();
                }
            }
        };

        onBirth();

        timer.start();
    }

    void onBirth();

    void onDeath();

    void onTick(Duration time, Duration delta);
}
