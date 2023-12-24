package piano.animation;

public class InertialState extends AnimationState {

    public InertialState(double target, double current) {
        super(target, current);
    }

    public void update(float delta) {
        double velocity = (getTarget() - getCurrent());
        velocity *= 0.9;
        setCurrent(getCurrent() + velocity * delta);
    }
}
