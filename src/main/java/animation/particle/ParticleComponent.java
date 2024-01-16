package animation.particle;

import animation.*;

public abstract class ParticleComponent implements Animation {
    protected Particle base;

    final void attach(Particle base) {
        this.base = base;
        onAttach();
    }

    protected void onAttach() {

    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void onBirth() {
    }

    @Override
    public void onDeath() {
    }
}
