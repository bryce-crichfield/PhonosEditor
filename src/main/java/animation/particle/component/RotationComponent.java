package animation.particle.component;

import animation.particle.ParticleComponent;

import java.time.Duration;

public class RotationComponent extends ParticleComponent {
    private double rotation;
    private final double velocity;

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
