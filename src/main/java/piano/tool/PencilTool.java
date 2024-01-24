package piano.tool;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.input.*;
import piano.*;
import piano.state.note.model.*;
import piano.view.midi.*;
import piano.view.settings.*;

public class PencilTool implements EditorTool {
    private final NoteEditorView view;
    private final MidiEditorContext context;

    public PencilTool(NoteEditorView view, MidiEditorContext context) {
        super();
        this.view = view;
        this.context = context;
    }

    @Override
    public void onEnter() {

    }

    @Override
    public EditorTool onMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isPrimaryButtonDown()) {
            PickResult pickResult = event.getPickResult();
            Point3D point = pickResult.getIntersectedPoint();
            Node node = pickResult.getIntersectedNode();
            if (node.equals(view.getBackgroundSurface())) {
                ObjectProperty<GridInfo> gridInfo = context.getViewSettings().gridInfoProperty();
                var gi = gridInfo.get();

                int startStep = (int) (gi.snapWorldXToNearestStep(point.getX()));
                int endStep = (int) (startStep + gi.snapInSteps());
                int cellY = (int) (gi.snapToGridY(point.getY()) / gi.getCellHeight());

                int mappedIndex = (int) Util.reverse(cellY, 0, 87);
                NotePitch pitch = NotePitch.from(mappedIndex + 1);
                NoteData data = new NoteData(pitch, startStep, endStep, 100);

                context.getNoteService().getSelection().clear();
                context.getNoteService().create(data);
            }
        }

        // Remove note with right click
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isSecondaryButtonDown()) {

            PickResult pickResult = event.getPickResult();
            Point3D point = pickResult.getIntersectedPoint();
            Node node = pickResult.getIntersectedNode();
            // We intersect the text of the note?  I guess this works, we just need to check the parent tree
            // For the noteMidiView and if we have it, we can remove it
            NoteView noteView = null;
            if (node instanceof NoteView parentNoteView) {
                noteView = parentNoteView;
            } else {
                Node parent = node.getParent();
                while (parent != null) {
                    if (parent instanceof NoteView parentNoteView) {
                        noteView = parentNoteView;
                        break;
                    }
                    parent = parent.getParent();
                }
            }

            if (noteView != null) {
                context.getNoteService().delete(noteView.getNoteEntry());
            }
        }

        return this;
    }
}
