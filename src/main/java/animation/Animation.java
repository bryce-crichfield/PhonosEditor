package animation;

import javafx.animation.AnimationTimer;

import java.time.Duration;

public interface Animation {
     boolean isAlive();
     default boolean isDead() {
         return !isAlive();
     }
     void onBirth();
     void onDeath();
     void onTick(Duration time, Duration delta);

     default void launchAnimation(Duration duration) {
         AnimationTimer timer = new AnimationTimer() {
                Duration startTime = Duration.ofNanos(System.nanoTime());
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
}
