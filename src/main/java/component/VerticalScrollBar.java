package component;

import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import util.*;

public class VerticalScrollBar extends ScrollBar {


    public VerticalScrollBar() {
        super();

        container = new VBox();
        AnchorPane.setTopAnchor(container, 0.0);
        AnchorPane.setBottomAnchor(container, 0.0);
        AnchorPane.setRightAnchor(container, 0.0);
        AnchorPane.setLeftAnchor(container, 0.0);

        // Create the scroll bar buttons
        {
            Polygon updwardFacingTriangle = new Polygon(0, SIZE, SIZE, SIZE, SIZE / 2, 0);
            negativeButton = createButton(updwardFacingTriangle, SIZE);

            Polygon downwardFacingTriangle = new Polygon(0, 0, SIZE, 0, SIZE / 2, SIZE);
            positiveButton = createButton(downwardFacingTriangle, SIZE);
        }

        // Create the scroll bar track and handle
        {
            track = new AnchorPane();
            track.setPrefWidth(SIZE);
            track.setBackground(new Background(new BackgroundFill(TRACK_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
            VBox.setVgrow(track, Priority.ALWAYS);
            handle = new Handle(SIZE, SIZE * 5);

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
            newY = track.getHeight() - handle.getHeight() - 1;
        }

        newY = MathUtil.clamp(newY, 0, track.getHeight() - handle.getHeight());
        handle.setY(newY);
        onScroll.accept(this);
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
        // We have to consider the total scrollable length (which considers handle's width)
        double totalScrollableLength = track.getHeight() - handle.getHeight();
        double relativeScrollableLength = totalScrollableLength / track.getHeight();
        double relativePosition = handle.getY() / track.getHeight();
        return MathUtil.map(relativePosition, 0, relativeScrollableLength, 0, 1);
    }
}

