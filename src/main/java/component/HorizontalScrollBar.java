package component;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import piano.Util;

import java.util.function.Consumer;

public class HorizontalScrollBar extends ScrollBar {
    public static final int SIZE = 25;
    private final Pane container;
    private final Button negativeButton;
    private final AnchorPane track;
    private final Button positiveButton;
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
            negativeButton = new Button();
            negativeButton.setPrefWidth(SIZE);
            negativeButton.setPrefHeight(SIZE);
            negativeButton.setGraphic(new FontIcon("mdi-arrow-left"));
            negativeButton.setFocusTraversable(false);
            negativeButton.setStyle("-fx-background-radius: 0;");

            positiveButton = new Button();
            positiveButton.setPrefWidth(SIZE);
            positiveButton.setPrefHeight(SIZE);
            positiveButton.setGraphic(new FontIcon("mdi-arrow-right"));
            positiveButton.setFocusTraversable(false);
            positiveButton.setStyle("-fx-background-radius: 0;");
        }

        // Initialize the scroll bar track and handle
        {
            track = new AnchorPane();
            track.setPrefHeight(25);
            HBox.setHgrow(track, Priority.ALWAYS);

            handle = new Handle(SIZE * 2, SIZE, Color.GRAY);

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
            return;
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
        return handle.getX() / track.getWidth();
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

            setArcHeight(10);
            setArcWidth(10);
            setStroke(Color.BLACK);
            setStrokeWidth(1);
        }

        private static Paint getFill(Color color) {
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                                                         new Stop(0, color),
                                                         new Stop(0.5, color.brighter()),
                                                         new Stop(1, color)
            );

            return gradient;
        }
    }
}

