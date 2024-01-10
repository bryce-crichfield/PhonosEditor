package component;

import javafx.css.Size;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import piano.Util;

import java.util.function.Consumer;

public class VerticalScrollBar extends ScrollBar {
    public static final int SIZE = 35;
    private final Pane container;
    private final Button negativeButton;
    private final AnchorPane track;
    private final Button positiveButton;
    private final Handle handle;
    private Consumer<ScrollBar> onScroll = (scrollBar) -> {
    };

    public VerticalScrollBar() {
        super();

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
            handle = new Handle(SIZE, SIZE*3, Color.DARKGRAY.darker().darker().darker().darker().darker());

            var centerVertically = container.widthProperty().subtract(handle.widthProperty()).divide(2);
            handle.xProperty().bind(centerVertically);

            track.getChildren().add(handle);

            // When the track's size is changed, the handle's position needs to be updated
            track.heightProperty().addListener((observable, oldValue, newValue) -> {
                double oldHeight = oldValue.doubleValue();
                double newHeight = newValue.doubleValue();

                double percentage = handle.getY() / (oldHeight - handle.getHeight());
                double newY = percentage * (newHeight - handle.getHeight());

                handle.setY(newY);
            });
        }

        // Add the listeners to the scroll bar buttons and handle
        {
            negativeButton.setOnMouseClicked(event -> {
                double newY = handle.getY() - (track.getHeight() * 0.05);
                updateY(newY);
            });

            handle.setOnMouseDragged(event -> {
                double newY = event.getY();
                // center the handle on the mouse's position relative to the handle itself
                newY -= handle.getHeight() / 2;
                updateY(newY);
            });

            positiveButton.setOnMouseClicked(event -> {
                double newY = handle.getY() + (track.getHeight() * 0.05);
                updateY(newY);
            });
        }
        container.getChildren().addAll(negativeButton, track, positiveButton);
        getChildren().add(container);
    }

    private void updateY(double newY) {
        // NOTE: The GRE proved to be important to solve a bug where the handle would go past the track
        if (newY >= track.getHeight() - handle.getHeight()) {
            return;
        }

        newY = Util.clamp(newY, 0, track.getHeight() - handle.getHeight());
        handle.setY(newY);
        onScroll.accept(this);
    }

    @Override
    public void onScroll(Consumer<ScrollBar> callback) {
        onScroll = callback;
    }

    @Override
    public void scrollBy(double delta) {
        double newY = handle.getY() + delta;
        updateY(newY);
    }

    @Override
    public void scrollTo(double position) {
        double newY = position * (track.getHeight() - handle.getHeight());
        updateY(newY);
    }

    @Override
    public double getScrollableLength() {
        return track.getHeight();
    }

    @Override
    public double getAbsolutePosition() {

        return handle.getY();
    }

    @Override
    public double getRelativePosition() {
        return handle.getY() / track.getHeight();
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

            setStroke(color.brighter().brighter().brighter().brighter().brighter());
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

