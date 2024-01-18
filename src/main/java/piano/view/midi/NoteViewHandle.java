package piano.view.midi;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import piano.*;
import piano.note.model.*;


abstract class NoteViewHandle {
    protected final Pane pane;
    protected final MidiEditorContext context;
    protected final NoteEntry noteEntry;
    protected final Rectangle rectangle;

    public NoteViewHandle(Pane pane, MidiEditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
        this.pane = pane;
        this.context = context;
        this.noteEntry = noteEntry;
        this.rectangle = rectangle;

        var grid = context.getViewSettings().gridInfoProperty().get();
//        double unsnappedX = noteEntry.get().getStartStep() * grid.getStepDisplayWidth();
//        noteEntry.setUnsnappedX(unsnappedX);
    }

    public abstract void onDragEntered();
    public abstract void onDragged(double deltaX, double cellsY);

    public abstract Cursor getCursor();

    public abstract boolean isHovered(double mouseX);

    static class Left extends NoteViewHandle {
        public Left(Pane pane, MidiEditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
            super(pane, context, noteEntry, rectangle);
        }

        @Override
        public void onDragged(double deltaX, double cellsY) {
            context.getNoteService().modify(noteEntry, entry -> {
                var noteData = entry.get();
                var grid = context.getViewSettings().gridInfoProperty().get();
                double unsnappedX = entry.getUnsnappedX() + deltaX;
                entry.setUnsnappedX(unsnappedX);
                double startStep = grid.snapWorldXToNearestStep(unsnappedX);
                return noteData.withStartStep((int) startStep);
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

        @Override
        public void onDragEntered() {
            context.getNoteService().update(noteEntry, entry -> {
                var grid = context.getViewSettings().gridInfoProperty().get();
                double unsnappedX = entry.get().getStartStep() * grid.getStepDisplayWidth();
                entry.setUnsnappedX(unsnappedX);
            });
        }
    }

    static class Right extends NoteViewHandle {
        public Right(Pane pane, MidiEditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
            super(pane, context, noteEntry, rectangle);
        }

        @Override
        public void onDragged(double deltaX, double cellsY) {
            context.getNoteService().modify(noteEntry, entry -> {
                var grid = context.getViewSettings().gridInfoProperty().get();
                var noteData = entry.get();

                entry.setUnsnappedX(entry.getUnsnappedX() + deltaX);
                double endStep = grid.snapWorldXToNearestStep(entry.getUnsnappedX());
                return noteData.withEndStep((int) endStep);
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

        @Override
        public void onDragEntered() {
            context.getNoteService().update(noteEntry, entry -> {
                var grid = context.getViewSettings().gridInfoProperty().get();
                double unsnappedX = entry.get().getEndStep() * grid.getStepDisplayWidth();
                entry.setUnsnappedX(unsnappedX);
            });
        }
    }

    static class Center extends NoteViewHandle {
        public Center(Pane pane, MidiEditorContext context, NoteEntry noteEntry, Rectangle rectangle) {
            super(pane, context, noteEntry, rectangle);
        }

        @Override
        public void onDragEntered() {
            context.getNoteService().update(noteEntry, entry -> {
                var grid = context.getViewSettings().gridInfoProperty().get();
                double unsnappedX = entry.get().getStartStep() * grid.getStepDisplayWidth();
                entry.setUnsnappedX(unsnappedX);
            });
        }

        @Override
        public void onDragged(double deltaX, double cellsY) {
            context.getNoteService().modify(noteEntry, entry -> {
                var noteData = entry.get();
                var grid = context.getViewSettings().gridInfoProperty().get();
                double unsnappedX = entry.getUnsnappedX() + deltaX;
                entry.setUnsnappedX(unsnappedX);
                double startStep = grid.snapWorldXToNearestStep(unsnappedX);
                double endStep = startStep + noteData.getDuration();
                int newNoteIndex = (int) (noteData.getPitch().getNoteIndex() - cellsY) + 1;
                NotePitch newPitch = NotePitch.from(newNoteIndex);
                return noteData.withStartStep((int) startStep).withEndStep((int) endStep).withPitch(newPitch);
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
