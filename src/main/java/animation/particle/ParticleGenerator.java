package animation.particle;

@FunctionalInterface
public interface ParticleGenerator {
    Particle generate(int index);
}
