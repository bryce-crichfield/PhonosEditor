package component;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import piano.Util;

import java.util.function.Consumer;

public class HorizontalScrollBar extends ScrollBar {
    public static final int SIZE = 15;
    private final Pane container;
    private final Label negativeButton;
    private final AnchorPane track;
    private final Label positiveButton;
    private final Handle handle;
    private Consumer<ScrollBar> onScroll = (scrollBar) -> {
    };

    public HorizontalScrollBar() {
        super();

        container = new HBox();
        AnchorPane.setTopAnchor(container, 0.0);
        AnchorPane.setBottomAnchor(container, 0.0);
        AnchorPane.setRightAnchor(container, 0.0);
        AnchorPane.setLeftAnchor(container, 0.0);

        // Initialize the scroll bar buttons
        {
            Polygon leftFacingTriangle = new Polygon(SIZE, 0, SIZE, SIZE, 0, SIZE / 2);
            negativeButton = (Label) createButton(leftFacingTriangle, SIZE);

            Polygon rightFacingTriangle = new Polygon(0, 0, 0, SIZE, SIZE, SIZE / 2);
            positiveButton = (Label) createButton(rightFacingTriangle, SIZE);
        }

        // Initialize the scroll bar track and handle
        {
            track = new AnchorPane();
            track.setPrefHeight(SIZE);
            HBox.setHgrow(track, Priority.ALWAYS);

            handle = new Handle(SIZE * 7, SIZE, Color.DARKGRAY.darker().darker().darker().darker().darker());

            track.getChildren().add(handle);

            var centerHorizontally = container.heightProperty().subtract(handle.heightProperty()).divide(2);
            handle.yProperty().bind(centerHorizontally);

            // When the track's size is changed, the handle's position needs to be updated
            // This is so the percentage of the handle's position is the same before and after the track's size is changed
            track.widthProperty().addListener((observable, oldValue, newValue) -> {
                double oldWidth = oldValue.doubleValue();
                double newWidth = newValue.doubleValue();

                double percentage = handle.getX() / (oldWidth - handle.getWidth());
                double newX = percentage * (newWidth - handle.getWidth());

                updateX(newX);
            });
        }

        // Add the listeners to the scroll bar buttons and handle
        {
            handle.setOnMouseDragged(event -> {
                double newX = event.getX();
                newX -= handle.getWidth() / 2;
                updateX(newX);
            });

            negativeButton.setOnMouseClicked(event -> {
                double newX = handle.getX() - (track.getWidth() * 0.05);
                updateX(newX);
            });

            positiveButton.setOnMouseClicked(event -> {
                double newX = handle.getX() + (track.getWidth() * 0.05);
                updateX(newX);
            });
        }
        container.getChildren().addAll(negativeButton, track, positiveButton);
        getChildren().add(container);
    }

    private void updateX(double newX) {
        // NOTE: The GRE proved to be important to solve a bug where the handle would go past the track
        if (newX >= track.getWidth() - handle.getWidth()) {
            newX = track.getWidth() - handle.getWidth() - 1;
        }

        newX = Util.clamp(newX, 0, track.getWidth() - handle.getWidth());
        handle.setX(newX);
        onScroll.accept(this);
    }

    @Override
    public void onScroll(Consumer<ScrollBar> callback) {
        onScroll = callback;
    }

    @Override
    public void scrollBy(double delta) {
        double newX = handle.getX() + delta;
        updateX(newX);
    }

    @Override
    public void scrollTo(double position) {
        double newX = position * (track.getWidth() - handle.getWidth());
        updateX(newX);
    }

    @Override
    public double getScrollableLength() {
        return track.getWidth();
    }

    @Override
    public double getAbsolutePosition() {
        return handle.getX();
    }

    @Override
    public double getRelativePosition() {
        // We have to consider the total scrollable length (which considers handle's width)
        double totalScrollableLength = track.getWidth() - handle.getWidth();
        double totalRelativeScrollableLength = totalScrollableLength / track.getWidth();
        double relativePosition = handle.getX() / track.getWidth();
        return Util.map(relativePosition, 0, totalRelativeScrollableLength, 0, 1);
    }

    private static class Handle extends Rectangle {
        private final Color color;
        boolean isPressed = false;

        public Handle(double width, double height, Color color) {
            super(width, height, getFill(color));
            this.color = color;

            setOnMouseEntered(event -> {
                setFill(color.brighter().brighter());

            });

            setOnMousePressed(event -> {
                isPressed = true;
                setFill(getFill(color.brighter().brighter()));

            });

            setOnMouseReleased(event -> {
                isPressed = false;
                setFill(getFill(color));
            });

            setOnMouseExited(event -> {
                if (isPressed) {
                    return;
                }
                setFill(getFill(color));
            });

            setStroke(color.brighter().brighter().brighter());
            setArcWidth(SIZE);
            setArcHeight(SIZE);
            setStrokeWidth(1);
        }

        private static Paint getFill(Color color) {
            return color;
        }
    }
}

