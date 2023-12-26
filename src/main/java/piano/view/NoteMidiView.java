package piano.view;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piano.control.MemoNoteController;
import piano.control.NoteController;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.tool.EditorTool;
import piano.tool.PencilTool;

import javax.swing.text.html.Option;
import java.util.Optional;

public class NoteMidiView extends StackPane {
    private final NoteEntry noteEntry;
    private final Rectangle rectangle;
    private final Text label;
    private final Handle leftHandle = new LeftHandle();
    private final Handle rightHandle = new RightHandle();
    private final Handle bodyHandle = new BodyHandle();
    ObjectProperty<GridInfo> gridInfo;
    private Handle selectedHandle = null;
    private NoteMidiController selfController = new PencilNoteMidiController();

    ObjectProperty<Optional<EditorTool>> currentTool;

    public NoteMidiView(NoteEntry noteEntry, ObjectProperty<GridInfo> gridInfo, ObjectProperty<Optional<EditorTool>> currentTool) {
        super();
        this.noteEntry = noteEntry;
        this.gridInfo = gridInfo;
        this.currentTool = currentTool;

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
            NoteData data = this.noteEntry.get();
            GridInfo grid = gridInfo.get();

            double x = data.calcXPosOnGrid(grid);
            double y = data.calcYPosOnGrid(grid);
            rectangle.setX(x);
            rectangle.setY(y);

            double width = (data.getEnd() - data.getStart()) * grid.getCellWidth();
            rectangle.setWidth(width);

            double height = grid.getCellHeight();
            rectangle.setHeight(height);
        });

        currentTool.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }

            if (newValue.get() instanceof PencilTool) {
                System.out.println("Pencil tool");
                selfController = new PencilNoteMidiController();
            } else {
                System.out.println("Unknown tool");
                selfController = new NoteMidiController() {};
            }
        });

        // Set handle on mouse hover
        rectangle.setOnMouseMoved(event -> selfController.rectangleOnMouseMoved(event));

        // Delegate the drag event to the selected handle
        this.setOnMouseDragged(event -> selfController.stackPaneOnMouseDragged(event));

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

    interface NoteMidiController {
        default void stackPaneOnMouseDragged(MouseEvent event) {
            event.consume();
        }

        default void rectangleOnMouseMoved(MouseEvent event) {
            event.consume();
        }
    }


    private interface Handle {
        void onDragged(double cellsX, double cellsY);

        Cursor getCursor();

        boolean isHovered(double mouseX);
    }

    private static class NullHandle implements Handle {
        public static final NullHandle instance = new NullHandle();

        @Override
        public void onDragged(double cellsX, double cellsY) {
            // Do nothing
        }

        @Override
        public Cursor getCursor() {
            return Cursor.DEFAULT;
        }

        @Override
        public boolean isHovered(double mouseX) {
            return false;
        }
    }

    private class PencilNoteMidiController implements NoteMidiController {
        @Override
        public void stackPaneOnMouseDragged(MouseEvent event) {
            double deltaX = event.getX();
            double deltaY = event.getY();

            double cellX = deltaX / gridInfo.get().getCellWidth();
            double cellY = deltaY / gridInfo.get().getCellHeight();

            if (selectedHandle == null) {
                return;
            }

            selectedHandle.onDragged(cellX, cellY);
        }

        @Override
        public void rectangleOnMouseMoved(MouseEvent event) {
            double mouseX = event.getX();

            if (leftHandle.isHovered(mouseX)) {
                selectedHandle = leftHandle;
            } else if (rightHandle.isHovered(mouseX)) {
                selectedHandle = rightHandle;
            } else if (bodyHandle.isHovered(mouseX)) {
                selectedHandle = bodyHandle;
            } else {
                selectedHandle = NullHandle.instance;
            }

            // Change the cursor to indicate the handle that will be selected
            setCursor(selectedHandle.getCursor());
        }
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
