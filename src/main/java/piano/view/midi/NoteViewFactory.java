package piano.view.midi;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import piano.*;
import piano.note.model.*;
import piano.tool.*;
import piano.view.settings.*;

import java.awt.*;
import java.util.*;

public class NoteViewFactory {
    public static NoteView create(NoteEntry noteEntry, MidiEditorContext context,
            ObjectProperty<Optional<EditorTool>> currentTool
    ) {
        // Create view components with context and model bindings ------------------------------------------------------
        var rectangle = createRectangle(context, noteEntry);
        var text = createLabel(context, noteEntry, rectangle);
        var root = createRoot(noteEntry, rectangle, text, currentTool);

        initPositionAndSize(context, rectangle, text, noteEntry);

        // Configure input handling and control state ------------------------------------------------------------------
        ControlState state = new ControlState();
        NoteViewHandle.Left leftHandle = new NoteViewHandle.Left(root, context, noteEntry, rectangle);
        NoteViewHandle.Right rightHandle = new NoteViewHandle.Right(root, context, noteEntry, rectangle);
        NoteViewHandle.Center centerHandle = new NoteViewHandle.Center(root, context, noteEntry, rectangle);

        // { Rectangle Mouse } selects { NoteMidiHandle }
        rectangle.setOnMouseMoved(event -> {
            double mouseX = event.getX();

            if (leftHandle.isHovered(mouseX)) {
                state.selectedHandle = Optional.of(leftHandle);
            } else if (rightHandle.isHovered(mouseX)) {
                state.selectedHandle = Optional.of(rightHandle);
            } else if (centerHandle.isHovered(mouseX)) {
                state.selectedHandle = Optional.of(centerHandle);
            } else {
                state.selectedHandle = Optional.empty();
            }

            state.selectedHandle.ifPresent(tool -> root.setCursor(tool.getCursor()));
        });

        // { NoteMidiView Press } starts { NoteMidiHandle Drag }
        root.setOnMousePressed(event -> {
            state.lastX = MouseInfo.getPointerInfo().getLocation().getX();
            state.selectedHandle.ifPresent(NoteViewHandle::onDragEntered);
        });

        // { NoteMidiView Drag } moves { NoteMidiHandle }
        root.setOnMouseDragged(event -> {
            var gridInfo = context.getViewSettings().gridInfoProperty();
            double deltaX = MouseInfo.getPointerInfo().getLocation().getX() - state.lastX;
            double cellY = event.getY() / gridInfo.get().getCellHeight();

            state.selectedHandle.ifPresent(handle -> handle.onDragged(deltaX, cellY));
            state.lastX = MouseInfo.getPointerInfo().getLocation().getX();
        });

        // { NoteMidiView Release } stops { NoteMidiHandle Drag }
        root.setOnMouseReleased(event -> {
            state.selectedHandle = Optional.empty();
        });

        // Return the root node ----------------------------------------------------------------------------------------
        return root;
    }

    private static NoteView createRoot(NoteEntry entry, Rectangle rectangle, Text text,
            ObjectProperty<Optional<EditorTool>> currentTool
    ) {
        NoteView root = new NoteView(entry);
        {
            root.setPickOnBounds(false);
            root.getChildren().addAll(rectangle, text);
        }

        // { rectangle.x } -> { Root }
        rectangle.xProperty().addListener(($0, $1, x) -> {
            root.setLayoutX(x.doubleValue());
        });

        // { rectangle.y } -> { Root }
        rectangle.yProperty().addListener(($0, $1, y) -> {
            root.setLayoutY(y.doubleValue());
        });

        // { rectangle.width } -> { Root }
        rectangle.widthProperty().addListener(($0, $1, width) -> {
            root.setPrefWidth(width.doubleValue());
            root.setMinWidth(width.doubleValue());
            root.setMaxWidth(width.doubleValue());
        });

        // { CurrentTool } -> { Root }
        currentTool.addListener(($0, $1, tool) -> {
            if (tool.isEmpty())
                root.setDisable(true);
            else
                root.setDisable(!(tool.get() instanceof PencilTool));
        });

        return root;
    }

    private static Text createLabel(MidiEditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
        Text label = new Text();
        {
        }

        // { GridInfo } -> { Label }
        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            // The font height should be 50% of the limiting cell dimension
            double largerDimension = Math.min(grid.getBeatDisplayWidth(), grid.getCellHeight());
            label.setFont(label.getFont().font(largerDimension * 0.5));
        });

        // { NoteEntry } -> { Label }
        noteEntry.addListener(($0, $1, data) -> {
            String noteString = data.getPitch().getNoteName();
            label.setText(noteString);
        });

        // { rectangle.x } -> { Label }
        rectangle.xProperty().addListener(($0, $1, x) -> {
            label.setX(x.doubleValue() + rectangle.getWidth() / 2 - label.getLayoutBounds().getWidth() / 2);
        });

        // { rectangle.y } -> { Label }
        rectangle.yProperty().addListener(($0, $1, y) -> {
            label.setY(y.doubleValue() + rectangle.getHeight() / 2 + label.getLayoutBounds().getHeight() / 2);

            NoteData noteData = noteEntry.get();
            String noteString = noteData.getPitch().getNoteName();
            label.setText(noteString);
        });

        // { rectangle.width } -> { Label }
        rectangle.widthProperty().addListener(($0, $1, width) -> {
            label.setX(width.doubleValue() / 2 - label.getLayoutBounds().getWidth() / 2);
            label.setY(width.doubleValue() / 2 + label.getLayoutBounds().getHeight() / 2);
        });

        return label;
    }

    private static Rectangle createRectangle(MidiEditorContext context, NoteEntry noteEntry) {
        Rectangle rectangle = new Rectangle();
        {
            NoteData data = noteEntry.get();
            var grid = context.getViewSettings().gridInfoProperty().get();
            rectangle.setFill(Theme.ORANGE);
            rectangle.setStroke(Color.BLACK);
            rectangle.setStrokeWidth(1);
        }

        // GridInfo <update> Rectangle
        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            NoteData data = noteEntry.get();
            rectangle.setX(data.calcXPosOnGrid(grid));
            rectangle.setY(data.calcYPosOnGrid(grid));
            rectangle.setWidth(data.calculateDisplayWidth(grid));
            rectangle.setHeight(data.calculateDisplayHeight(grid));
        });

        // NoteEntry <update> Rectangle
        noteEntry.addListener(($0, $1, data) -> {
            var grid = context.getViewSettings().gridInfoProperty().get();
            rectangle.setX(data.calcXPosOnGrid(grid));
            rectangle.setY(data.calcYPosOnGrid(grid));
            rectangle.setWidth(data.calculateDisplayWidth(grid));
            rectangle.setHeight(data.calculateDisplayHeight(grid));
        });

        // NoteSelection <update> Rectangle
        context.getNoteService().getSelection().addListener((ListChangeListener<NoteEntry>) c -> {
            if (c.getList().contains(noteEntry))
                rectangle.setFill(Theme.BRIGHT_GREEN);
            else
                rectangle.setFill(Theme.ORANGE);
        });

        return rectangle;
    }

    public static void initPositionAndSize(MidiEditorContext context, Rectangle rectangle, Text text, NoteEntry entry) {
        NoteData data = entry.get();
        var grid = context.getViewSettings().gridInfoProperty().get();
        rectangle.setX(data.calcXPosOnGrid(grid));
        rectangle.setY(data.calcYPosOnGrid(grid));
        rectangle.setWidth(data.calculateDisplayWidth(grid));
        rectangle.setHeight(data.calculateDisplayHeight(grid));
        double largerDimension = Math.min(grid.getBeatDisplayWidth(), grid.getCellHeight());
        text.setFont(text.getFont().font(largerDimension * 0.5));
    }

    private static class ControlState {
        Optional<NoteViewHandle> selectedHandle = Optional.empty();
        double lastX;
    }
}
