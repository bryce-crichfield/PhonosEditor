package component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

import java.util.function.Consumer;

public abstract class ScrollBar extends AnchorPane {
    public abstract void onScroll(Consumer<ScrollBar> callback);

    public abstract void scrollBy(double delta);

    public abstract void scrollTo(double position);

    public abstract double getScrollableLength();

    public abstract double getAbsolutePosition();

    public abstract double getRelativePosition();

    static Node createButton(Polygon icon, double size) {
        Transform scale = new Scale(0.5, 0.5);
        Transform translate = Transform.translate(size / 2, size / 2);

        icon.getTransforms().addAll(scale, translate);
        icon.setFill(javafx.scene.paint.Color.WHITE);

        Label button = new Label();
        button.setPrefWidth(size);
        button.setPrefHeight(size);
        button.setFocusTraversable(false);
        button.setStyle("-fx-background-radius: 0;");
        button.setGraphic(icon);


        javafx.scene.paint.Color defaultColor = Color.DARKGRAY.darker().darker().darker().darker().darker();
        Background defaultBackground = new Background(new BackgroundFill(defaultColor, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY));
        Background hoverBackground = new Background(new BackgroundFill(defaultColor.brighter().brighter(), CornerRadii.EMPTY, Insets.EMPTY));

        button.setBackground(defaultBackground);

        button.setOnMouseEntered(event -> {
            button.setBackground(hoverBackground);
        });

        button.setOnMouseExited(event -> {
            button.setBackground(defaultBackground);
        });

        return button;
    }
}
