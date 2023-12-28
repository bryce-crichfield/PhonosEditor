package piano;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.util.function.Function;

public class Util {

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double map(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
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

    public static <T, R> ObjectProperty<R> mapToObj(ObjectProperty<T> prop, Function<T, R> mapper) {
        ObjectProperty<R> binding = new SimpleObjectProperty<>();

        prop.addListener((observable, oldValue, newValue) -> {
            binding.set(mapper.apply(newValue));
        });

        return binding;
    }

    public static <T> DoubleProperty mapToDouble(ObjectProperty<T> prop, Function<T, Double> mapper) {
        DoubleProperty binding = new SimpleDoubleProperty();

        prop.addListener((observable, oldValue, newValue) -> {
            binding.set(mapper.apply(newValue));
        });

        return binding;
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
}
