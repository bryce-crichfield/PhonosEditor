package util;

import javafx.scene.paint.*;

public class ColorUtil {
    public static double luminance(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    public static double contrast(Color self, Color other) {
        double l1 = luminance(self);
        double l2 = luminance(other);
        if (l1 > l2) {
            return (l1 + 0.05) / (l2 + 0.05);
        } else {
            return (l2 + 0.05) / (l1 + 0.05);
        }
    }
}
