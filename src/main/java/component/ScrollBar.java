package component;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;

import java.util.function.*;

public abstract class ScrollBar extends AnchorPane {
    public static final int SIZE = 15;
    public static final Color DEFAULT_COLOR = Color.DARKGRAY.darker().darker().darker();
    public static final Color HOVER_COLOR = Color.DARKGRAY;
    public static final Color TRACK_COLOR = DEFAULT_COLOR.darker().darker().darker();

    protected Pane container;
    protected Node negativeButton;
    protected AnchorPane track;
    protected Node positiveButton;
    protected Handle handle;

    protected Consumer<ScrollBar> onScroll = (scrollBar) -> {
    };

    public void setOnHandleScroll(Consumer<ScrollBar> callback) {
        onScroll = callback;
    }

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
        button.setGraphic(icon);

        Background defaultBackground = new Background(
                new BackgroundFill(TRACK_COLOR, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY));
        Background hoverBackground = new Background(new BackgroundFill(HOVER_COLOR, CornerRadii.EMPTY, Insets.EMPTY));

        button.setBackground(defaultBackground);

        button.setOnMouseEntered(event -> {
            button.setBackground(hoverBackground);
        });

        button.setOnMouseExited(event -> {
            button.setBackground(defaultBackground);
        });

        return button;
    }

    protected static class Handle extends Rectangle {
        boolean isPressed = false;

        public Handle(double width, double height) {
            super(width, height, DEFAULT_COLOR);

            setOnMouseEntered(event -> {
                setFill(HOVER_COLOR);

            });

            setOnMousePressed(event -> {
                isPressed = true;
                setFill(HOVER_COLOR);

            });

            setOnMouseReleased(event -> {
                isPressed = false;
                setFill(DEFAULT_COLOR);
            });

            setOnMouseExited(event -> {
                if (isPressed) {
                    return;
                }
                setFill(DEFAULT_COLOR);
            });

            setStroke(DEFAULT_COLOR);
            setArcWidth(SIZE);
            setArcHeight(SIZE);
            setStrokeWidth(1);
        }
    }
}
