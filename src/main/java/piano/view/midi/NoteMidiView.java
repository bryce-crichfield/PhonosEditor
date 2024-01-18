package piano.view.midi;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import piano.*;
import piano.note.model.*;
import piano.tool.*;
import piano.view.settings.*;

import java.util.*;
import java.util.function.*;

public class NoteMidiView extends StackPane {
    private final MidiEditorContext context;
    private final NoteEntry noteEntry;
    private final Rectangle rectangle;
    private final Text label;
    private final NoteMidiHandle.Left leftHandle;
    private final NoteMidiHandle.Right rightHandle;
    private final NoteMidiHandle.Center centerHandle;
    private Optional<NoteMidiHandle> selectedHandle = Optional.empty();
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
            double largerDimension = Math.min(grid.getBeatDisplayWidth(), grid.getCellHeight());
            label.setFont(label.getFont().font(largerDimension * 0.5));
        }

        rectangle = new Rectangle();
        rectangle.setFill(Theme.ORANGE);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        getChildren().addAll(rectangle, label);

        bindPaneToRectangle();

        // When the grid changes, we need to update the view
        context.getViewSettings().gridInfoProperty().addListener((observable1, oldValue1, newValue1) -> {
            NoteData data = this.noteEntry.get();
            GridInfo grid = newValue1;

            rectangle.setX(data.calcXPosOnGrid(grid));
            rectangle.setY(data.calcYPosOnGrid(grid));
            rectangle.setWidth(data.calculateDisplayWidth(grid));
            rectangle.setHeight(data.calculateDisplayHeight(grid));

            // The font height should be 50% of the limiting cell dimension
            double largerDimension = Math.min(grid.getBeatDisplayWidth(), grid.getCellHeight());
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
        this.setOnMousePressed(event -> controller.ifPresent(c -> c.stackPaneOnMousePressed(event)));
        this.setOnMouseDragged(event -> controller.ifPresent(c -> c.stackPaneOnMouseDragged(event)));
        this.setOnMouseReleased(event -> controller.ifPresent(c -> c.stackPaneOnMouseReleased(event)));

        // Update the view rectangle when the note data model changes
        noteEntry.addListener(($0, $1, data) -> {
            var grid = context.getViewSettings().gridInfoProperty().get();
            rectangle.setX(data.calcXPosOnGrid(grid));
            rectangle.setY(data.calcYPosOnGrid(grid));
            rectangle.setWidth(data.calculateDisplayWidth(grid));
            rectangle.setHeight(data.calculateDisplayHeight(grid));
        });

        // Update the view rectangle when the selected entries change
        context.getNoteService().getSelection().addListener((ListChangeListener<NoteEntry>) c -> {
            if (c.getList().contains(noteEntry)) {
                rectangle.setFill(Theme.BRIGHT_GREEN);
            } else {
                rectangle.setFill(Theme.ORANGE);
            }
        });

        // Initialize the view
        NoteData data = this.noteEntry.get();
        var grid = context.getViewSettings().gridInfoProperty().get();
        rectangle.setX(data.calcXPosOnGrid(grid));
        rectangle.setY(data.calcYPosOnGrid(grid));
        rectangle.setWidth(data.calculateDisplayWidth(grid));
        rectangle.setHeight(data.calculateDisplayHeight(grid));

        // Create handles
        leftHandle = new NoteMidiHandle.Left(this, context, noteEntry, rectangle);
        rightHandle = new NoteMidiHandle.Right(this, context, noteEntry, rectangle);
        centerHandle = new NoteMidiHandle.Center(this, context, noteEntry, rectangle);
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
        double lastX;
        public void stackPaneOnMouseDragged(MouseEvent event) {
            var gridInfo = context.getViewSettings().gridInfoProperty();
            double deltaX = event.getSceneX() - lastX;
            double cellY = event.getY() / gridInfo.get().getCellHeight();

            selectedHandle.ifPresent(handle -> handle.onDragged(deltaX, cellY));
            lastX = event.getSceneX();
        }

        public void stackPaneOnMousePressed(MouseEvent event) {
            lastX = event.getSceneX();
            selectedHandle.ifPresent(handle -> handle.onDragEntered());
        }

        public void stackPaneOnMouseReleased(MouseEvent event) {
            selectedHandle = Optional.empty();
        }

        public void rectangleOnMouseMoved(MouseEvent event) {
            double mouseX = event.getX();

            if (leftHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(leftHandle);
            } else if (rightHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(rightHandle);
            } else if (centerHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(centerHandle);
            } else {
                selectedHandle = Optional.empty();
            }

            // Change the cursor to indicate the handle that will be selected
            selectedHandle.ifPresent(handle -> setCursor(handle.getCursor()));
        }
    }
}
