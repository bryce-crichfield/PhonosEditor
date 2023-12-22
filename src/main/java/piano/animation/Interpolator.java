package piano.animation;

import java.util.concurrent.atomic.AtomicReference;

@FunctionalInterface
public interface Interpolator {
    double interpolate(double start, double current, double end, double t);

    public static Interpolator linear() {
        return (start, current, end, t) -> {
            return start + (end - start) * t;
        };
    }

    public static Interpolator easeInOut() {
        return (start, current, end, t) -> {
            double t2 = t * t;
            double t3 = t2 * t;
            return start + (end - start) * (2 * t3 - 3 * t2 + 1);
        };
    }

    public static Interpolator easeIn() {
        return (start, current, end, t) -> {
            double t2 = t * t;
            double t3 = t2 * t;
            return start + (end - start) * (t3);
        };
    }

    public static Interpolator easeOut() {
        return (start, current, end, t) -> {
            double t2 = t * t;
            double t3 = t2 * t;
            return start + (end - start) * (t3 - 3 * t2 + 3 * t);
        };
    }

}
