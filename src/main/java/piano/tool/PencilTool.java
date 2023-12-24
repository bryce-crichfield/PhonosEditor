package piano.tool;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.view.NoteMidiView;
import piano.view.NoteMidiEditor;

public class PencilTool implements EditorTool {
    private final NoteMidiEditor editor;

    public PencilTool(NoteMidiEditor editor) {
        super();
        this.editor = editor;
    }

    @Override
    public void onMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isPrimaryButtonDown()) {
            PickResult pickResult = event.getPickResult();
            Point3D point = pickResult.getIntersectedPoint();
            Node node = pickResult.getIntersectedNode();
            if (node.equals(editor.getBackgroundSurface())) {
                ObjectProperty<GridInfo> gridInfo = editor.getGridInfo();
                var gi = gridInfo.get();
                NoteData data = NoteData.from(point.getX(), point.getY(), gi.getCellWidth(), gi.getCellHeight(), gi);
                editor.getNoteRegistry().register(data);
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
                editor.getNoteRegistry().unregister(noteMidiView.getNoteEntry());
            }
        }
    }
}
