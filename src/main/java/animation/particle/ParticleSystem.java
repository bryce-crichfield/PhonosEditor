package animation.particle;

import animation.*;
import javafx.scene.*;

import java.time.*;
import java.util.*;

public class ParticleSystem implements Animation {
    private final List<Particle> particles;
    private final Group world;

    public ParticleSystem(Group world, int count, ParticleGenerator generator) {
        this.world = world;
        particles = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Particle particle = generator.generate(i);
            particles.add(particle);
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void onBirth() {
        particles.forEach(Particle::onBirth);
        particles.forEach(particle -> {
            world.getChildren().add(particle);
        });
    }

    @Override
    public void onDeath() {
        particles.forEach(Particle::onDeath);
        particles.forEach(particle -> {
            world.getChildren().remove(particle);
        });
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        particles.forEach(particle -> particle.onTick(time, delta));
        particles.forEach(particle -> {
            if (particle.isDead()) {
                world.getChildren().remove(particle);
            }
        });
        particles.removeIf(particle -> particle.isDead());
    }
}
