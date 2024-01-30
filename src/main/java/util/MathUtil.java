package util;

public class MathUtil {
    public static double floorToNearest(double value, double nearest) {
        return java.lang.Math.floor(value / nearest) * nearest;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double map(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

    // Takes a value in the range [min, max] and returns a value in the range [max, min]
    public static double reverse(double value, double min, double max) {
        return max - (value - min);
    }

    public static double lerp(double a, double b, double t) {
        double result = a + (b - a) * t;
        // Clamp if close to b
        if (Math.abs(result - b) < 0.0001) {
            result = b;
        }

        return result;
    }
}
