package piano.state.tool;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.state.note.model.NoteData;
import piano.state.note.model.NoteEntry;
import piano.view.note.NotesPane;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class SelectTool implements EditorTool {
    // Two Modes:
    // 1. Window Selection - Only those notes that are completely inside the selection box are selected
    //      (Drag to the right, blue box)
    // 2. Cross Selection - All notes that intersect the selection box are selected
    //      (Drag to the left, green box)

    private final EditorContext context;
    private final Group world;
    private final NotesPane editor;
    private Optional<SelectionBox> currentBox;

    public SelectTool(NotesPane editor, Group world, EditorContext context) {
        this.world = world;
        this.editor = editor;
        this.context = context;
    }

    @Override
    public void onEnter() {
        context.getNoteService().getSelection().clear();
    }

    @Override
    public EditorTool onMouseEvent(MouseEvent event) {
        // Selection Press
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isPrimaryButtonDown()) {
            PickResult pickResult = event.getPickResult();
            Node node = pickResult.getIntersectedNode();
            if (node.equals(editor.getBackgroundSurface())) {
                Point3D point = pickResult.getIntersectedPoint();
                currentBox = Optional.of(new CrossSelectionBox(point));
                world.getChildren().add(currentBox.get());
            }
        }

        // Selection Drag
        if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && event.isPrimaryButtonDown()) {
            PickResult pickResult = event.getPickResult();
            Node node = pickResult.getIntersectedNode();
            if (node.equals(editor.getBackgroundSurface())) {
                Point3D point = pickResult.getIntersectedPoint();

                if (currentBox.isPresent()) {
                    SelectionBox box = currentBox.get();
                    boolean leftOfAnchor = point.getX() < box.anchorPoint.getX();
                    boolean rightOfAnchor = point.getX() > box.anchorPoint.getX();
                    if (box instanceof WindowSelectionBox && leftOfAnchor) {
                        Point3D anchorPoint = box.anchorPoint;
                        world.getChildren().remove(box);
                        currentBox = Optional.of(new CrossSelectionBox(anchorPoint));
                        world.getChildren().add(currentBox.get());
                    }

                    if (box instanceof CrossSelectionBox && rightOfAnchor) {
                        Point3D anchorPoint = box.anchorPoint;
                        world.getChildren().remove(box);
                        currentBox = Optional.of(new WindowSelectionBox(anchorPoint));
                        world.getChildren().add(currentBox.get());
                    }
                }

                currentBox.ifPresent(box -> box.onDragged(point));
            }
        }

        // Selection Release
        if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            currentBox.ifPresent(box -> world.getChildren().remove(box));

            // Return to pencil if selection is non-empty
            if (!context.getNoteService().getSelection().isEmpty()) {
                return new PencilTool(editor, context);
            }
        }


        return this;
    }

    abstract class SelectionBox extends Rectangle {
        protected final Point3D anchorPoint;

        public SelectionBox(Point3D anchorPoint, Color color) {
            this.anchorPoint = anchorPoint;

            setFill(color.deriveColor(0, 1, 1, 0.5));
            setStroke(color);
            setStrokeWidth(1);

            // Don't allow the selection box to receive mouse events
            setDisable(true);
        }


        public void onDragged(Point3D point) {
            // Update the selection box
            double x = Math.min(anchorPoint.getX(), point.getX());
            double y = Math.min(anchorPoint.getY(), point.getY());
            double width = Math.abs(anchorPoint.getX() - point.getX());
            double height = Math.abs(anchorPoint.getY() - point.getY());
            setX(x);
            setY(y);
            setWidth(width);
            setHeight(height);


            context.getNoteService().getSelection().clear();
            Stream<NoteEntry> registry = context.getNoteService().getRegistry().getEntries().stream();
            Collection<NoteEntry> noteEntries = registry.filter(noteEntry -> isNoteSelected(noteEntry.get())).toList();
            noteEntries.forEach(note -> context.getNoteService().getSelection().add(note));
        }

        protected abstract boolean isNoteSelected(NoteData data);
    }

    private class WindowSelectionBox extends SelectionBox {

        public WindowSelectionBox(Point3D anchorPoint) {
            super(anchorPoint, Color.BLUE);
        }

        @Override
        protected boolean isNoteSelected(NoteData data) {
            var gi = context.getViewSettings().getGridInfo();
            double noteX = data.calcXPosOnGrid(gi);
            double noteY = data.calcYPosOnGrid(gi);
            double noteWidth = gi.getStepDisplayWidth() * data.getDurationInSteps();
            double noteHeight = gi.getCellHeight();

            double boxX = getX();
            double boxY = getY();
            double boxWidth = getWidth();
            double boxHeight = getHeight();

            // The note needs to be completely inside the box
            return noteX >= boxX && noteX + noteWidth <= boxX + boxWidth
                    && noteY >= boxY && noteY + noteHeight <= boxY + boxHeight;
        }
    }

    private class CrossSelectionBox extends SelectionBox {

        public CrossSelectionBox(Point3D anchorPoint) {
            super(anchorPoint, Color.GREEN);
        }

        @Override
        protected boolean isNoteSelected(NoteData data) {
            var gi = context.getViewSettings().getGridInfo();
            double noteX = data.calcXPosOnGrid(gi);
            double noteY = data.calcYPosOnGrid(gi);
            double noteWidth = gi.getStepDisplayWidth() * data.getDurationInSteps();
            double noteHeight = gi.getCellHeight();

            double boxX = getX();
            double boxY = getY();
            double boxWidth = getWidth();
            double boxHeight = getHeight();

            // The note just needs to intersect the box not be completely inside it
            return noteX + noteWidth >= boxX && noteX <= boxX + boxWidth
                    && noteY + noteHeight >= boxY && noteY <= boxY + boxHeight;
        }
    }
}
