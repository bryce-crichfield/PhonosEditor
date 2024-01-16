package piano.view.midi;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piano.MidiEditorContext;
import piano.model.GridInfo;
import piano.model.note.NoteData;
import piano.model.note.NoteEntry;
import piano.tool.EditorTool;
import piano.tool.PencilTool;

import java.util.Optional;
import java.util.function.Consumer;

public class NoteMidiView extends StackPane {
    private final MidiEditorContext context;
    private final NoteEntry noteEntry;
    private final Rectangle rectangle;
    private final Text label;
    private final NoteMidiHandle.Left leftHandle;
    private final NoteMidiHandle.Right rightHandle;
    private final NoteMidiHandle.Body bodyHandle;
    private Optional<NoteMidiHandle> selectedHandle;
    private Optional<NoteMidiController> controller = Optional.empty();

    public NoteMidiView(NoteEntry noteEntry, MidiEditorContext context,
            ObjectProperty<Optional<EditorTool>> currentTool
    ) {
        super();
        this.context = context;
        this.noteEntry = noteEntry;

        label = new Text("");
        {
            // The font height should be 50% of the limiting cell dimension
            var grid = context.getViewSettings().gridInfoProperty().get();
            double largerDimension = Math.min(grid.getCellWidth(), grid.getCellHeight());
            label.setFont(label.getFont().font(largerDimension * 0.5));
        }

        rectangle = new Rectangle();
        rectangle.setFill(Color.DARKGREEN);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        getChildren().addAll(rectangle, label);

        bindPaneToRectangle();

        // When the grid changes, we need to update the view
        context.getViewSettings().gridInfoProperty().addListener((observable1, oldValue1, newValue1) -> {
            NoteData data = this.noteEntry.get();
            GridInfo grid = newValue1;

            double x = data.calcXPosOnGrid(grid);
            double y = data.calcYPosOnGrid(grid);
            rectangle.setX(x);
            rectangle.setY(y);

            double width = (data.getEnd() - data.getStart()) * grid.getCellWidth();
            rectangle.setWidth(width);

            double height = grid.getCellHeight();
            rectangle.setHeight(height);

            // The font height should be 50% of the limiting cell dimension
            double largerDimension = Math.min(grid.getCellWidth(), grid.getCellHeight());
            label.setFont(label.getFont().font(largerDimension * 0.5));
        });

        // When the tool changes, we need to update the controller
        {
            Consumer<Optional<EditorTool>> bindTool = tool -> {
                if (tool.isEmpty()) {
                    return;
                }

                if (tool.get() instanceof PencilTool) {
                    controller = Optional.of(new NoteMidiController());
                    this.setDisable(false);
                } else {
                    controller = Optional.empty();
                    this.setDisable(true);
                }
            };

            bindTool.accept(currentTool.get());

            currentTool.addListener((observable, oldValue, newValue) -> {
                bindTool.accept(newValue);
            });
        }

        // Set handle on mouse hover
        rectangle.setOnMouseMoved(event -> controller.ifPresent(c -> c.rectangleOnMouseMoved(event)));

        // Delegate the drag event to the selected handle
        this.setOnMouseDragged(event -> controller.ifPresent(c -> c.stackPaneOnMouseDragged(event)));

        // Update the view rectangle when the note data model changes
        noteEntry.addListener((observable, oldValue, newValue) -> {
            var gridInfo = context.getViewSettings().gridInfoProperty();
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
        context.getNotes().getSelectedEntries().addListener((ListChangeListener<NoteEntry>) c -> {
            if (c.getList().contains(noteEntry)) {
                rectangle.setFill(Color.BLUE);
            } else {
                rectangle.setFill(Color.DARKGREEN);
            }
        });

        // Initialize the view
        NoteData data = this.noteEntry.get();
        var gridInfo = context.getViewSettings().gridInfoProperty();
        double x = data.calcXPosOnGrid(gridInfo.get());
        double y = data.calcYPosOnGrid(gridInfo.get());
        rectangle.setX(x);
        rectangle.setY(y);
        double width = (data.getEnd() - data.getStart()) * gridInfo.get().getCellWidth();
        rectangle.setWidth(width);
        double height = gridInfo.get().getCellHeight();
        rectangle.setHeight(height);

        // Create handles
        leftHandle = new NoteMidiHandle.Left(this, context, noteEntry, rectangle);
        rightHandle = new NoteMidiHandle.Right(this, context, noteEntry, rectangle);
        bodyHandle = new NoteMidiHandle.Body(this, context, noteEntry, rectangle);
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
            String noteString = noteData.getPitch().getNoteName();
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

    private class NoteMidiController {
        public void stackPaneOnMouseDragged(MouseEvent event) {
            double deltaX = event.getX();
            double deltaY = event.getY();

            var gridInfo = context.getViewSettings().gridInfoProperty();

            double cellX = deltaX / gridInfo.get().getCellWidth();
            double cellY = deltaY / gridInfo.get().getCellHeight();

            if (selectedHandle != null) {
                selectedHandle.ifPresent(handle -> handle.onDragged(cellX, cellY));
            }

            event.consume();
        }

        public void rectangleOnMouseMoved(MouseEvent event) {
            double mouseX = event.getX();

            if (leftHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(leftHandle);
            } else if (rightHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(rightHandle);
            } else if (bodyHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(bodyHandle);
            } else {
                selectedHandle = Optional.empty();
            }

            // Change the cursor to indicate the handle that will be selected
            selectedHandle.ifPresent(handle -> setCursor(handle.getCursor()));

            event.consume();
        }
    }
}
