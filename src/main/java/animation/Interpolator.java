package animation;

@FunctionalInterface
public interface Interpolator {
    double interpolate(double start, double current, double end, double t);

    static Interpolator linear() {
        return (start, current, end, t) -> {
            return start + (end - start) * t;
        };
    }

    static Interpolator easeInOut() {
        return (start, current, end, t) -> {
            double t2 = t * t;
            double t3 = t2 * t;
            return start + (end - start) * (2 * t3 - 3 * t2 + 1);
        };
    }

    static Interpolator easeIn() {
        return (start, current, end, t) -> {
            double t2 = t * t;
            double t3 = t2 * t;
            return start + (end - start) * (t3);
        };
    }

    static Interpolator easeOut() {
        return (start, current, end, t) -> {
            double t2 = t * t;
            double t3 = t2 * t;
            return start + (end - start) * (t3 - 3 * t2 + 3 * t);
        };
    }

}
