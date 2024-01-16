package animation.particle.component;

import animation.particle.*;

import java.time.*;

public class LifespanComponent extends ParticleComponent {
    private final Duration duration;
    private Duration elapsed;

    public LifespanComponent(Duration duration) {
        this.duration = duration;
        this.elapsed = Duration.ZERO;
    }

    @Override
    public boolean isAlive() {
        return elapsed.compareTo(duration) < 0;
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        elapsed = elapsed.plus(delta);
    }
}
