package animation.particle.component;

import animation.particle.*;

import java.time.*;

public class VelocityComponent extends ParticleComponent {
    private final double velocityX;
    private final double velocityY;

    public VelocityComponent(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        double vx = velocityX * delta.toMillis() / 1000;
        double vy = velocityY * delta.toMillis() / 1000;
        base.setX(base.getX() + vx);
        base.setY(base.getY() + vy);
    }
}
