package piano.view.midi;

import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.model.NoteEntry;


abstract class NoteMidiHandle {
    protected final Pane pane;
    protected final EditorContext context;
    protected final NoteEntry noteEntry;
    protected final Rectangle rectangle;

    public NoteMidiHandle(Pane pane, EditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
        this.pane = pane;
        this.context = context;
        this.noteEntry = noteEntry;
        this.rectangle = rectangle;
    }

    public abstract void onDragged(double cellsX, double cellsY);

    public abstract Cursor getCursor();

    public abstract boolean isHovered(double mouseX);

    static class Left extends NoteMidiHandle {
        public Left(Pane pane, EditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
            super(pane, context, noteEntry, rectangle);
        }

        @Override
        public void onDragged(double cellsX, double cellsY) {
            context.getNotes().modify(noteEntry, noteData -> {
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

    static class Right extends NoteMidiHandle {
        public Right(Pane pane, EditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
            super(pane, context, noteEntry, rectangle);
        }

        @Override
        public void onDragged(double cellsX, double cellsY) {
            context.getNotes().modify(noteEntry, noteData -> {
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
            return mouseX >= rectangle.getX() + pane.getWidth() - 15 && mouseX <= rectangle.getX() + pane.getWidth();
        }
    }

    static class Body extends NoteMidiHandle {
        public Body(Pane pane, EditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
            super(pane, context, noteEntry, rectangle);
        }

        @Override
        public void onDragged(double cellsX, double cellsY) {
            context.getNotes().modify(noteEntry, noteData -> {
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
            return mouseX >= rectangle.getX() + 15 && mouseX <= rectangle.getX() + pane.getWidth() - 15;
        }
    }
}
