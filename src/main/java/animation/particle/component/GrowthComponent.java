package animation.particle.component;

import animation.particle.ParticleComponent;

import java.time.Duration;

public class GrowthComponent extends ParticleComponent {
    private final double rate;

    public GrowthComponent(double rate) {
        this.rate = rate;
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        double width = base.getWidth();
        double height = base.getHeight();

        width += rate * delta.toMillis() / 1000;
        height += rate * delta.toMillis() / 1000;

        base.setWidth(width);
        base.setHeight(height);
    }
}
