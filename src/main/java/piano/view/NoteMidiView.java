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
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

public class NoteMidiView extends StackPane {
    private final NoteEntry noteEntry;
    private final Rectangle rectangle;
    private final Text label;
    private final Handle leftHandle = new LeftHandle();
    private final Handle rightHandle = new RightHandle();
    private final Handle bodyHandle = new BodyHandle();
    ObjectProperty<GridInfo> gridInfo;
    private Handle selectedHandle = null;

    public NoteMidiView(NoteEntry noteEntry, NoteRegistry noteRegistry, ObjectProperty<GridInfo> gridInfo) {
        super();
        this.noteEntry = noteEntry;
        this.gridInfo = gridInfo;

        label = new Text("");
        rectangle = new Rectangle();
        rectangle.setFill(Color.DARKGREEN);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        getChildren().addAll(rectangle, label);

        bindPaneToRectangle();

        // When the grid changes, we need to update the view
        gridInfo.addListener((observable1, oldValue1, newValue1) -> {
            // Resize the note to fit the new grid
            double newWidth = newValue1.getCellWidth();
            double newHeight = newValue1.getCellHeight();
            rectangle.setWidth(newWidth);
            rectangle.setHeight(newHeight);

            // Move the note to the new position
            NoteData data1 = this.noteEntry.get();
            double x1 = data1.calcXPosOnGrid(newValue1);
            double y1 = data1.calcYPosOnGrid(newValue1);

            rectangle.setX(x1);
            rectangle.setY(y1);
        });


        // Set handle on mouse hover
        rectangle.setOnMouseMoved(event -> {
            double mouseX = event.getX();

            if (leftHandle.isHovered(mouseX)) {
                selectedHandle = leftHandle;
            } else if (rightHandle.isHovered(mouseX)) {
                selectedHandle = rightHandle;
            } else if (bodyHandle.isHovered(mouseX)) {
                selectedHandle = bodyHandle;
            } else {
                throw new RuntimeException("Mouse is not hovering over any handle");
            }

            // Change the cursor to indicate the handle that will be selected
            setCursor(selectedHandle.getCursor());
        });

        // Delegate the drag event to the selected handle
        this.setOnMouseDragged(event -> {
            double deltaX = event.getX();
            double deltaY = event.getY();

            double cellX = deltaX / gridInfo.get().getCellWidth();
            double cellY = deltaY / gridInfo.get().getCellHeight();

            if (selectedHandle == null) {
                throw new RuntimeException("Mouse is not hovering over any handle");
            }

            selectedHandle.onDragged(cellX, cellY);
        });

        // Update the view rectangle when the note data model changes
        noteEntry.addListener((observable, oldValue, newValue) -> {
            double x = newValue.calcXPosOnGrid(gridInfo.get());
            double y = newValue.calcYPosOnGrid(gridInfo.get());
            rectangle.setX(x);
            rectangle.setY(y);

            double width = (newValue.getEnd() - newValue.getStart()) * gridInfo.get().getCellWidth();
            rectangle.setWidth(width);

            double height = gridInfo.get().getCellHeight();
            rectangle.setHeight(height);
        });

        // Update the view rectangle when the selected entries change
        MemoNoteController.getInstance().getSelectedEntries().addListener((ListChangeListener<NoteEntry>) c -> {
            if (c.getList().contains(noteEntry)) {
                rectangle.setFill(Color.BLUE);
            } else {
                rectangle.setFill(Color.DARKGREEN);
            }
        });

        // Initialize the view
        NoteData data = this.noteEntry.get();
        double x = data.calcXPosOnGrid(gridInfo.get());
        double y = data.calcYPosOnGrid(gridInfo.get());
        rectangle.setX(x);
        rectangle.setY(y);
        double width = (data.getEnd() - data.getStart()) * gridInfo.get().getCellWidth();
        rectangle.setWidth(width);
        double height = gridInfo.get().getCellHeight();
        rectangle.setHeight(height);
    }

    private void bindPaneToRectangle() {
        rectangle.xProperty().addListener((observable, oldValue, newValue) -> {
            label.setX(newValue.doubleValue() + rectangle.getWidth() / 2 - label.getLayoutBounds().getWidth() / 2);
            this.setLayoutX(newValue.doubleValue());
        });

        rectangle.yProperty().addListener((observable, oldValue, newValue) -> {
            label.setY(newValue.doubleValue() + rectangle.getHeight() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setLayoutY(newValue.doubleValue());

            NoteData noteData = this.noteEntry.get();
            String noteString = NoteData.noteToString(88 - noteData.getNote());
            label.setText(noteString);
        });

        rectangle.widthProperty().addListener((observable, oldValue, newValue) -> {
            label.setX(newValue.doubleValue() / 2 - label.getLayoutBounds().getWidth() / 2);
            label.setY(newValue.doubleValue() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setWidth(newValue.doubleValue());
        });
    }

    public NoteEntry getNoteEntry() {
        return noteEntry;
    }

    private interface Handle {
        void onDragged(double cellsX, double cellsY);

        Cursor getCursor();

        boolean isHovered(double mouseX);
    }

    private class LeftHandle implements Handle {
        @Override
        public void onDragged(double cellsX, double cellsY) {
            NoteController controller = MemoNoteController.getInstance();
            controller.modify(noteEntry, noteData -> {
                int x = (int) (noteData.getStart() + cellsX);
                return noteData.withStart(x);
            });
        }

        @Override
        public Cursor getCursor() {
            return Cursor.W_RESIZE;
        }

        @Override
        public boolean isHovered(double mouseX) {
            return mouseX >= rectangle.getX() && mouseX <= rectangle.getX() + 15;
        }
    }

    private class RightHandle implements Handle {
        @Override
        public void onDragged(double cellsX, double cellsY) {
            NoteController controller = MemoNoteController.getInstance();
            controller.modify(noteEntry, noteData -> {
                int x = (int) (noteData.getStart() + cellsX);
                return noteData.withEnd(x);
            });
        }

        @Override
        public Cursor getCursor() {
            return Cursor.E_RESIZE;
        }

        @Override
        public boolean isHovered(double mouseX) {
            return mouseX >= rectangle.getX() + getWidth() - 15 && mouseX <= rectangle.getX() + getWidth();
        }
    }

    private class BodyHandle implements Handle {
        @Override
        public void onDragged(double cellsX, double cellsY) {
            NoteController controller = MemoNoteController.getInstance();
            controller.modify(noteEntry, noteData -> {
                int newStart = (int) (noteData.getStart() + cellsX);
                int newEnd = (int) (noteData.getEnd() + cellsX);
                int newNote = (int) (noteData.getNote() + cellsY);

                return noteData.withStart(newStart).withEnd(newEnd).withNote(newNote);
            });
        }

        @Override
        public Cursor getCursor() {
            return Cursor.MOVE;
        }

        @Override
        public boolean isHovered(double mouseX) {
            return mouseX >= rectangle.getX() + 15 && mouseX <= rectangle.getX() + getWidth() - 15;
        }
    }
}
