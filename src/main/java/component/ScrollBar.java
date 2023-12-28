package component;

import javafx.animation.AnimationTimer;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import piano.Util;

import java.util.function.Consumer;

public class ScrollBar extends AnchorPane {
    public static final int SIZE = 25;
    float handleVelocity = 0;
    private Pane container;
    private Button negativeButton;
    private AnchorPane track;
    private Button positiveButton;
    private Handle handle;
    private Consumer<Double> onScrollIn;
    private Consumer<Double> onScroll = (percentage) -> {
    };

    public ScrollBar(Orientation orientation) {
        super();

        if (orientation == Orientation.HORIZONTAL) {
            initHorizontal();
        } else {
            initVertical();
        }

        container.getChildren().addAll(negativeButton, track, positiveButton);

        getChildren().add(container);
    }

    private void initHorizontal() {
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

                handleVelocity = (float) (newX - handle.getX());
            });
        }

        // Add the listeners to the scroll bar buttons and handle
        {
            Consumer<Double> applyHorizontalScroll = (newX) -> {
                // NOTE: The GRE proved to be important to solve a bug where the handle would go past the track
                if (newX >= track.getWidth() - handle.getWidth()) {
                    return;
                }

                newX = Util.clamp(newX, 0, track.getWidth() - handle.getWidth());
                handleVelocity = (float) (newX - handle.getX());
            };

            onScrollIn = (delta) -> {
                double newX = handle.getX() + delta;
                applyHorizontalScroll.accept(newX);
            };

            handle.setOnMouseDragged(event -> {
                double newX = event.getX();
                newX -= handle.getWidth() / 2;
                applyHorizontalScroll.accept(newX);
            });

            negativeButton.setOnMouseClicked(event -> {
                double newX = handle.getX() - (track.getWidth() * 0.05);
                applyHorizontalScroll.accept(newX);
            });

            positiveButton.setOnMouseClicked(event -> {
                double newX = handle.getX() + (track.getWidth() * 0.05);
                applyHorizontalScroll.accept(newX);
            });

            final Long[] lastTime = {System.nanoTime()};
            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    double deltaTime = (now - lastTime[0]) / 1e9;
                    lastTime[0] = now;

                    handle.setX(handle.getX() + handleVelocity * deltaTime * 5);
                    handle.setX(Util.clamp(handle.getX(), 0, track.getWidth() - handle.getWidth() - 1));
                    handleVelocity *= 0.9;

                    double newX = handle.getX();
                    double percentage = newX / (track.getWidth() - handle.getWidth());
                    onScroll.accept(percentage);
                }
            };

            timer.start();
        }
    }

    private void initVertical() {
        container = new VBox();
        AnchorPane.setTopAnchor(container, 0.0);
        AnchorPane.setBottomAnchor(container, 0.0);
        AnchorPane.setRightAnchor(container, 0.0);
        AnchorPane.setLeftAnchor(container, 0.0);

        // Create the scroll bar buttons
        {
            negativeButton = new Button();
            negativeButton.setGraphic(new FontIcon("mdi-arrow-up"));
            negativeButton.setFocusTraversable(false);
            negativeButton.setStyle("-fx-background-radius: 0;");

            positiveButton = new Button();
            positiveButton.setGraphic(new FontIcon("mdi-arrow-down"));
            positiveButton.setFocusTraversable(false);
            positiveButton.setStyle("-fx-background-radius: 0;");
        }

        // Create the scroll bar track and handle
        {
            track = new AnchorPane();
            track.setPrefWidth(25);
            VBox.setVgrow(track, Priority.ALWAYS);
            handle = new Handle(25, 50, Color.GRAY);

            var centerVertically = container.widthProperty().subtract(handle.widthProperty()).divide(2);
            handle.xProperty().bind(centerVertically);

            track.getChildren().add(handle);

            // When the track's size is changed, the handle's position needs to be updated
            track.heightProperty().addListener((observable, oldValue, newValue) -> {
                double oldHeight = oldValue.doubleValue();
                double newHeight = newValue.doubleValue();

                double percentage = handle.getY() / (oldHeight - handle.getHeight());
                double newY = percentage * (newHeight - handle.getHeight());
                handleVelocity = (float) (newY - handle.getY());
            });
        }

        // Add the listeners to the scroll bar buttons and handle
        {

            Consumer<Double> applyVerticalScroll = (newY) -> {
                // NOTE: The GRE proved to be important to solve a bug where the handle would go past the track
                if (newY >= track.getHeight() - handle.getHeight()) {
                    return;
                }


                newY = Util.clamp(newY, 0, track.getHeight() - handle.getHeight());
                handle.setTargetVal(newY.floatValue());
                handleVelocity = (float) (newY - handle.getY());
            };

            onScrollIn = (delta) -> {
                double newY = handle.getY() + delta;
                applyVerticalScroll.accept(newY);
            };

            negativeButton.setOnMouseClicked(event -> {
                double newY = handle.getY() - (track.getHeight() * 0.05);
                applyVerticalScroll.accept(newY);
            });

            handle.setOnMouseDragged(event -> {
                double newY = event.getY();
                // center the handle on the mouse's position relative to the handle itself
                newY -= handle.getHeight() / 2;
                applyVerticalScroll.accept(newY);
            });

            positiveButton.setOnMouseClicked(event -> {
                double newY = handle.getY() + (track.getHeight() * 0.05);
                applyVerticalScroll.accept(newY);
            });

            final Long[] lastTime = {System.nanoTime()};

            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    double deltaTime = (now - lastTime[0]) / 1e9;
                    lastTime[0] = now;

                    handle.setY(handle.getY() + handleVelocity * deltaTime * 10);
                    handle.setY(Util.clamp(handle.getY(), 0, track.getHeight() - handle.getHeight() - 1));
                    handleVelocity *= 0.9;

                    double newY = handle.getY();
                    double percentage = newY / (track.getHeight() - handle.getHeight());
                    onScroll.accept(percentage);
                }
            };

            timer.start();
        }
    }

    public void scroll(double delta) {
        onScrollIn.accept(delta);
    }

    public void setOnScroll(Consumer<Double> onScroll) {
        this.onScroll = onScroll;
    }

    private static class Handle extends Rectangle {
        private final Color color;
        boolean isPressed = false;
        float currentVal = 0;
        float targetVal = 0;

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

        public void setTargetVal(float targetVal) {
            this.targetVal = targetVal;
        }
    }
}

