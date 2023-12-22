package piano;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Util {

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double map(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

    public static class DebugListener implements ChangeListener<Number> {
        private final String name;
        public DebugListener(String name) {
            this.name = name;
        }
        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
            System.out.println(name + " changed from " + number + " to " + t1);
        }
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
