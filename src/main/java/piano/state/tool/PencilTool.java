package piano.state.tool;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import piano.EditorContext;
import piano.state.note.model.NoteData;
import piano.state.note.model.NotePitch;
import piano.view.note.NoteView;
import piano.view.note.NotesPane;
import piano.view.zoom.GridInfo;
import util.MathUtil;

public class PencilTool implements EditorTool {
    private final NotesPane view;
    private final EditorContext context;

    public PencilTool(NotesPane view, EditorContext context) {
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

                int mappedIndex = (int) MathUtil.reverse(cellY, 0, 87);
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
