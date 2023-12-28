package piano.animation;

import javafx.util.Pair;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class AnimationState extends AtomicReference<Pair<Double, Double>> {
    public AnimationState(double target, double current) {
        super(new Pair<>(target, current));
    }

    public void modifyTarget(BiFunction<Double, Double, Double> f) {
        set(new Pair<>(f.apply(getTarget(), getCurrent()), getCurrent()));
    }

    public double getTarget() {
        return get().getKey();
    }

    public void setTarget(double target) {
        set(new Pair<>(target, getCurrent()));
    }

    public double getCurrent() {
        return get().getValue();
    }

    public void setCurrent(double current) {
        set(new Pair<>(getTarget(), current));
    }

    public void modifyCurrent(BiFunction<Double, Double, Double> f) {
        set(new Pair<>(getTarget(), f.apply(getTarget(), getCurrent())));
    }

    public void interpolate(Interpolator interpolator, double t) {
        double target = getTarget();
        double current = getCurrent();
        setCurrent(interpolator.interpolate(current, current, target, t));
    }
}
