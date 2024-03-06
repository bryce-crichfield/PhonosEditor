package animation.particle.component;

import animation.particle.ParticleComponent;

import java.time.Duration;

public class RotationComponent extends ParticleComponent {
    private final double velocity;
    private double rotation;

    public RotationComponent(double velocity) {
        this.rotation = 0;
        this.velocity = velocity;
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        rotation += velocity * delta.toMillis() / 1000;
        base.setRotate(rotation);
    }
}
