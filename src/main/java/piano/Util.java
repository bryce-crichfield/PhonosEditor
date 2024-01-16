package piano;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class Util {

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

    public static AnchorPane createAnchorPane(Node child) {
        AnchorPane pane = new AnchorPane();
        AnchorPane.setTopAnchor(child, 0.0);
        AnchorPane.setBottomAnchor(child, 0.0);
        AnchorPane.setLeftAnchor(child, 0.0);
        AnchorPane.setRightAnchor(child, 0.0);
        pane.getChildren().add(child);
        return pane;
    }

    public class Easing {
        public static double easeInSin(double t) {
            return 1 - Math.cos((t * Math.PI) / 2);
        }

        public static double easeInCubic(double t) {
            return t * t * t;
        }

        public static double easeOutCubic(double t) {
            return 1 - Math.pow(1 - t, 3);
        }

        public static double easeInOutCubic(double t) {
            return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
        }
    }
}
