package piano;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;

class Note extends StackPane {
    // The dimensions of the note are controlled by the rectangle, which is the background of the note
    private final Rectangle rectangle;
    private final List<Note> notes;
    ObjectProperty<GridInfo> gridInfo;
//    private final double gridWidth;
//    private final double gridHeight;
    private Handle selectedHandle = null;

    public Note(List<Note> notes, double x, double y, ObjectProperty<GridInfo> gridInfo) {
        super();

        this.notes = notes;

//        this.gridWidth = gridWidth;
//        this.gridHeight = gridHeight;

        this.gridInfo = gridInfo;

        double gridWidth = gridInfo.get().getCellWidth();
        double gridHeight = gridInfo.get().getCellHeight();

        setWidth(gridWidth);
        setHeight(gridHeight);

        rectangle = new Rectangle(gridWidth, gridHeight);
        rectangle.setFill(Color.DARKGREEN);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);

        Text label = new Text("C");

        getChildren().add(rectangle);
        getChildren().add(label);

        rectangle.setOnMouseMoved(this::updateHandle);
        rectangle.setOnMouseDragged(this::onMouseDrag);

        rectangle.xProperty().addListener((observable, oldValue, newValue) -> {
            // Whenever the rectangle's x position changes, update the label and stack-pane's position, and the label's text
            label.setX(newValue.doubleValue() + rectangle.getWidth() / 2 - label.getLayoutBounds().getWidth() / 2);
            this.setLayoutX(newValue.doubleValue());
        });

        rectangle.yProperty().addListener((observable, oldValue, newValue) -> {
            // Whenever the rectangle's y position changes, update the label and stack-pane's position, and the label's text
            label.setY(newValue.doubleValue() + rectangle.getHeight() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setLayoutY(newValue.doubleValue());
        });

        rectangle.widthProperty().addListener((observable, oldValue, newValue) -> {
            // Whenever the rectangle's width changes, update the label's position and text, and the stack-pane's width
            label.setX(newValue.doubleValue() / 2 - label.getLayoutBounds().getWidth() / 2);
            label.setY(newValue.doubleValue() / 2 + label.getLayoutBounds().getHeight() / 2);
            label.setText(String.valueOf((int) (newValue.doubleValue() / gridWidth)));
            this.setWidth(newValue.doubleValue());
        });

        moveNote(x, y);


        // Whenever the grid info changes, we need to find our column and row in the old system and convert it to the new system
        gridInfo.addListener((observable, oldValue, newValue) -> {
            double oldGridWidth = oldValue.getCellWidth();
            double oldGridHeight = oldValue.getCellHeight();
            double newGridWidth = newValue.getCellWidth();
            double newGridHeight = newValue.getCellHeight();

            double oldX = rectangle.getX();
            double oldY = rectangle.getY();
            double oldWidth = rectangle.getWidth();
            double oldHeight = rectangle.getHeight();

            double newX = Math.floor(oldX / oldGridWidth) * newGridWidth;
            double newY = Math.floor(oldY / oldGridHeight) * newGridHeight;
            double newWidth = Math.floor(oldWidth / oldGridWidth) * newGridWidth;
            double newHeight = Math.floor(oldHeight / oldGridHeight) * newGridHeight;

            rectangle.setX(newX);
            rectangle.setY(newY);
            rectangle.setWidth(newWidth);
            rectangle.setHeight(newHeight);
        });
    }

    public void onMouseDrag(MouseEvent event) {
        if (selectedHandle == Handle.Left) {
            resizeNoteFromLeft(event.getX());
        }

        if (selectedHandle == Handle.Right) {
            resizeNoteFromRight(event.getX());
        }

        if (selectedHandle == Handle.Body) {
            moveNote(event.getX(), event.getY());
        }
    }

    private void moveNote(double x, double y) {
        double gridWidth = gridInfo.get().getCellWidth();
        double gridHeight = gridInfo.get().getCellHeight();

        double newX = Math.floor(x / gridWidth) * gridWidth;
        double newY = Math.floor(y / gridHeight) * gridHeight;

        if (testCollision(newX, newY, getWidth(), getHeight())) {
            return;
        }

        rectangle.setX(newX);
        rectangle.setY(newY);
    }

    private void resizeNoteFromLeft(double mouseX) {
        double gridWidth = gridInfo.get().getCellWidth();
        double gridHeight = gridInfo.get().getCellHeight();

        double newX = Math.floor(mouseX / gridWidth) * gridWidth;
        double newWidth = getWidth() + getRectangleX() - newX;
        newWidth = Math.floor(newWidth / gridWidth) * gridWidth;

        if (testCollision(newX, getRectangleY(), newWidth, getHeight())) {
            return;
        }

        if (newWidth < gridWidth) {
            return;
        }

        rectangle.setX(newX);
        rectangle.setWidth(newWidth);
    }

    private boolean testCollision(double x, double y, double width, double height) {
        double gridWidth = gridInfo.get().getCellWidth();
        double gridHeight = gridInfo.get().getCellHeight();

        // Get this note's rectangle in row-column coordinates
        float row = (float) Math.floor(y / gridHeight);
        float column = (float) Math.floor(x / gridWidth);
        float rows = (float) Math.floor(height / gridHeight);
        float columns = (float) Math.floor(width / gridWidth);

        for (Note note : notes) {
            if (note == this) {
                continue;
            }

            // Get the other note's rectangle in row-column coordinates
            float otherRow = (float) Math.floor(note.getRectangleY() / gridHeight);
            float otherColumn = (float) Math.floor(note.getRectangleX() / gridWidth);
            float otherRows = (float) Math.floor(note.getHeight() / gridHeight);
            float otherColumns = (float) Math.floor(note.getWidth() / gridWidth);

            // If the rectangles intersect, return true
            if (row + rows > otherRow && row < otherRow + otherRows && column + columns > otherColumn && column < otherColumn + otherColumns) {
                return true;
            }
        }

        return false;
    }

    private void resizeNoteFromRight(double mouseX) {
        double gridWidth = gridInfo.get().getCellWidth();

        double newWidth = Math.floor(mouseX / gridWidth) * gridWidth - getRectangleX();
        newWidth += gridWidth;

        if (testCollision(getRectangleX(), getRectangleY(), newWidth, getHeight())) {
            return;
        }

        if (newWidth < gridWidth) {
            return;
        }

        rectangle.setWidth(newWidth);
    }

    private void updateHandle(MouseEvent event) {
        double mouseX = event.getX();

        // Select the handle based on the mouse position
        if (mouseX >= getRectangleX() && mouseX <= getRectangleX() + 15) {
            selectedHandle = Handle.Left;
        } else if (mouseX >= getRectangleX() + getWidth() - 15 && mouseX <= getRectangleX() + getWidth()) {
            selectedHandle = Handle.Right;
        } else {
            selectedHandle = Handle.Body;
        }

        // Change the cursor to indicate the handle that will be selected
        if (selectedHandle == Handle.Left) {
            setCursor(Cursor.W_RESIZE);
        } else if (selectedHandle == Handle.Right) {
            setCursor(Cursor.E_RESIZE);
        } else {
            setCursor(Cursor.MOVE);
        }
    }

    enum Handle {
        Left, Right, Body
    }

    private double getRectangleX() {
        return rectangle.getX();
    }

    private double getRectangleY() {
        return rectangle.getY();
    }
}
