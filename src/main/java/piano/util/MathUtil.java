package piano.util;

public class MathUtil {
    public static double floorToNearest(double value, double nearest) {
        return java.lang.Math.floor(value / nearest) * nearest;
    }
}
