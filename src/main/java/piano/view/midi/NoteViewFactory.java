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
import piano.util.*;
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

    public static final int MIN_LABEL_WIDTH = 32;

    private static Text createLabel(MidiEditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
        Text label = new Text();
        {
            label.setVisible(context.getViewSettings().showNoteLettersProperty().get());
            Color backgroundColor = context.getViewSettings().getPatternColor();
            double whiteContrast = ColorUtils.contrast(backgroundColor, Color.WHITE);
            double blackContrast = ColorUtils.contrast(backgroundColor, Color.BLACK);
            Color fontColor = whiteContrast > blackContrast ? Color.WHITE : Color.BLACK;
            label.setFill(fontColor);
        }

        // { ViewSettings } -> { Label }
        context.getViewSettings().showNoteLettersProperty().addListener(($0, $1, show) -> {
            label.setVisible(show);
        });

        // { ViewSettings } -> { Label }
        context.getViewSettings().patternColorProperty().addListener(($0, $1, color) -> {
            double whiteContrast = ColorUtils.contrast(color, Color.WHITE);
            double blackContrast = ColorUtils.contrast(color, Color.BLACK);
            Color fontColor = whiteContrast > blackContrast ? Color.WHITE : Color.BLACK;
            label.setFill(fontColor);
        });

        // { GridInfo } -> { Label }
        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            // The font height should be 50% of the limiting cell dimension
            double width = noteEntry.get().getDuration() * grid.getStepDisplayWidth();
            if (width < MIN_LABEL_WIDTH) {
                label.setVisible(false);
                return;
            }

            double largerDimension = Math.min(width, grid.getCellHeight());
            label.setVisible(context.getViewSettings().showNoteLettersProperty().get());
            label.setFont(label.getFont().font(largerDimension * 0.5));
        });

        // { NoteEntry } -> { Label }

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

            var grid = context.getViewSettings().gridInfoProperty().get();
            double w = noteEntry.get().getDuration() * grid.getStepDisplayWidth();
            if (w < MIN_LABEL_WIDTH) {
                label.setVisible(false);
                return;
            }

            double largerDimension = Math.min(w, grid.getCellHeight());
            label.setVisible(context.getViewSettings().showNoteLettersProperty().get());
            label.setFont(label.getFont().font(largerDimension * 0.5));
        });

        return label;
    }

    private static Rectangle createRectangle(MidiEditorContext context, NoteEntry noteEntry) {
        Rectangle rectangle = new Rectangle();
        {
            NoteData data = noteEntry.get();
            var grid = context.getViewSettings().gridInfoProperty().get();
            rectangle.setFill(context.getViewSettings().getPatternColor());
            rectangle.setStroke(Color.BLACK);
            rectangle.setStrokeWidth(1);
        }

        // PatternColor <update> Rectangle
        context.getViewSettings().patternColorProperty().addListener(($0, $1, color) -> {
            rectangle.setFill(color);
        });

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
            if (c.getList().contains(noteEntry)) {
                rectangle.setStroke(Color.CYAN);
                rectangle.setStrokeWidth(2);
            }
            else {
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(1);
            }
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

        double width = entry.get().getDuration() * grid.getStepDisplayWidth();
        if (width < MIN_LABEL_WIDTH) {
            text.setVisible(false);
            return;
        }
        double largerDimension = Math.min(grid.getBeatDisplayWidth(), grid.getCellHeight());
        text.setVisible(context.getViewSettings().showNoteLettersProperty().get());
        text.setFont(text.getFont().font(largerDimension * 0.5));
    }

    private static class ControlState {
        Optional<NoteViewHandle> selectedHandle = Optional.empty();
        double lastX;
    }
}
