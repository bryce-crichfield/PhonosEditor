package animation.particle;

import animation.Animation;
import javafx.scene.shape.Rectangle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Particle extends Rectangle implements Animation {
    private final List<ParticleComponent> components;

    public Particle() {
        components = new ArrayList<>();
        setDisable(true);
    }

    public void addComponent(ParticleComponent component) {
        components.add(component);
        component.attach(this);
    }

    @Override
    public boolean isAlive() {
        return components.stream().allMatch(ParticleComponent::isAlive);
    }

    @Override
    public void onBirth() {
        components.forEach(ParticleComponent::onBirth);
    }

    @Override
    public void onDeath() {
        components.forEach(ParticleComponent::onDeath);
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        components.forEach(component -> component.onTick(time, delta));
    }
}
