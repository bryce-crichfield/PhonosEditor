package piano.view;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piano.control.MemoNoteController;
import piano.control.NoteController;
import piano.model.NoteEntry;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteRegistry;
import piano.util.GridMath;

public class NoteMidiView extends StackPane {
    private final NoteEntry noteEntry;
    private final NoteRegistry noteRegistry;

    // The dimensions of the note are controlled by the rectangle, which is the background of the note
    private final Rectangle rectangle;
    ObjectProperty<GridInfo> gridInfo;
    private Handle selectedHandle = null;

    public NoteMidiView(NoteEntry noteEntry, NoteRegistry noteRegistry, ObjectProperty<GridInfo> gridInfo) {
        super();
        this.noteEntry = noteEntry;
        this.noteRegistry = noteRegistry;
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

        Text label = new Text("");

        getChildren().add(rectangle);
        getChildren().add(label);



        // DELEGATES ===================================================================================================

        // Whenever the mouse hover over the note, we need to change the cursor to indicate that the note can be moved
        rectangle.setOnMouseMoved(event -> {
            double mouseX = event.getX();

            // Select the handle based on the mouse position
            if (mouseX >= rectangle.getX() && mouseX <= rectangle.getX() + 15) {
                selectedHandle = Handle.Left;
            } else if (mouseX >= rectangle.getX() + getWidth() - 15 && mouseX <= rectangle.getX() + getWidth()) {
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
        });

        // When the mouse is pressed, we need to start moving the note based on the selected handle of the note
        rectangle.setOnMouseDragged(event -> {
            // Grow the note from the Left
            if (selectedHandle == Handle.Left) {
                // Snap the mouse position to the grid
                double gridWidth1 = this.gridInfo.get().getCellWidth();
                double newX = GridMath.snapToGridX(this.gridInfo.get(), event.getX());
                double newWidth = GridMath.snapToGridX(this.gridInfo.get(), getWidth() + rectangle.getX() - newX);

                // Ensure that the note is not colliding with any other notes or that the note is not too small
                if (!testCollision(newX, rectangle.getY(), newWidth, getHeight())) {
                    if (!(newWidth < gridWidth1)) {// Move the note to the new position and update the note property
                        rectangle.setX(newX);
                        rectangle.setWidth(newWidth);
                        updateNoteProperty();
                    }
                }
            }

            // Grow the note from the Right
            if (selectedHandle == Handle.Right) {
                // Snap the mouse position to the grid
                double gridWidth1 = this.gridInfo.get().getCellWidth();
                double newWidth = GridMath.snapToGridX(this.gridInfo.get(),
                                                       event.getX()
                ) - rectangle.getX() + gridWidth1;

                // Ensure that the note is not colliding with any other notes or that the note is not too small
                if (!testCollision(rectangle.getX(), rectangle.getY(), newWidth, getHeight())) {
                    if (!(newWidth < gridWidth1)) {
                        rectangle.setWidth(newWidth);
                        updateNoteProperty();
                    }
                }

            }

            // Move the note around
            if (selectedHandle == Handle.Body) {
                moveNote(event.getX(), event.getY());
            }
        });

        // Bind label and stack pane x-position to the rectangle
        rectangle.xProperty().addListener((observable, oldValue, newValue) -> {
            label.setX(newValue.doubleValue() + rectangle.getWidth() / 2 - label.getLayoutBounds().getWidth() / 2);
            this.setLayoutX(newValue.doubleValue());
        });

        // Bind label and stack pane y-position to the rectangle
        // Bind the text to the note's y-position
        rectangle.yProperty().addListener((observable, oldValue, newValue) -> {
            label.setY(newValue.doubleValue() + rectangle.getHeight() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setLayoutY(newValue.doubleValue());

            NoteData noteData = this.noteEntry.get();
            String noteString = NoteData.noteToString(88 - noteData.getNote());
            label.setText(noteString);
        });

        // Bind label and stack pane width to the rectangle
        rectangle.widthProperty().addListener((observable, oldValue, newValue) -> {
            label.setX(newValue.doubleValue() / 2 - label.getLayoutBounds().getWidth() / 2);
            label.setY(newValue.doubleValue() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setWidth(newValue.doubleValue());
        });

        gridInfo.addListener((observable, oldValue, newValue) -> {
            // Resize the note to fit the new grid
            double newWidth = newValue.getCellWidth();
            double newHeight = newValue.getCellHeight();
            rectangle.setWidth(newWidth);
            rectangle.setHeight(newHeight);

            // Move the note to the new position
            NoteData data = this.noteEntry.get();
            double x = data.calcXPosOnGrid(newValue);
            double y = data.calcYPosOnGrid(newValue);
            moveNote(x, y);

        });

        // Initially position the note on the grid =====================================================================
        NoteData data = this.noteEntry.get();
        double x = data.calcXPosOnGrid(gridInfo.get());
        double y = data.calcYPosOnGrid(gridInfo.get());
        moveNote(x, y);


        NoteController controller = MemoNoteController.getInstance();
        controller.getSelectedEntries().addListener((ListChangeListener<? super NoteEntry>) c -> {
            if (controller.getSelectedEntries().contains(noteEntry)) {
                rectangle.setFill(Color.BLUE);
            } else {
                rectangle.setFill(Color.DARKGREEN);
            }
        });
    }

    private void updateNoteProperty() {
        NoteData newData = NoteData.from(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),
                                         rectangle.getHeight(), gridInfo.get()
        );
        noteEntry.set(newData);
    }

    private void moveNote(double x, double y) {
        // Find the snapped x and y positions
        double newX = GridMath.snapToGridX(gridInfo.get(), x);
        double newY = GridMath.snapToGridY(gridInfo.get(), y);

        // Ensure that the note is not colliding with any other notes
        if (testCollision(newX, newY, getWidth(), getHeight())) {
            return;
        }

        // Move the note to the new position and update the note property
        rectangle.setX(newX);
        rectangle.setY(newY);
        updateNoteProperty();
    }

    private boolean testCollision(double x, double y, double width, double height) {
        // TODO: Reimplement this
        double gridWidth = gridInfo.get().getCellWidth();
        double gridHeight = gridInfo.get().getCellHeight();

        return false;
    }

    public NoteEntry getNoteEntry() {
        return noteEntry;
    }

    private enum Handle {
        Left, Right, Body
    }
}
