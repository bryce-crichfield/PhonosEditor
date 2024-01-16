package piano.tool;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.input.*;
import piano.*;
import piano.note.model.*;
import piano.util.*;
import piano.view.midi.*;
import piano.view.settings.*;

public class PencilTool implements EditorTool {
    private final NoteMidiEditor view;
    private final MidiEditorContext context;

    public PencilTool(NoteMidiEditor view, MidiEditorContext context) {
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

                int cellX = (int) (GridMath.snapToGridX(gi, point.getX()) / gi.getCellWidth());
                int cellY = (int) (GridMath.snapToGridY(gi, point.getY()) / gi.getCellHeight());

                int mappedIndex = (int) Util.reverse(cellY, 0, 87);
                NotePitch pitch = NotePitch.from(mappedIndex + 1);
                NoteData data = new NoteData(pitch, cellX, cellX + 1, 100);

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
            NoteMidiView noteMidiView = null;
            if (node instanceof NoteMidiView parentNoteMidiView) {
                noteMidiView = parentNoteMidiView;
            } else {
                Node parent = node.getParent();
                while (parent != null) {
                    if (parent instanceof NoteMidiView parentNoteMidiView) {
                        noteMidiView = parentNoteMidiView;
                        break;
                    }
                    parent = parent.getParent();
                }
            }

            if (noteMidiView != null) {
                context.getNoteService().delete(noteMidiView.getNoteEntry());
            }
        }

        return this;
    }
}
