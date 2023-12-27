package piano.tool;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import piano.EditorContext;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.util.GridMath;
import piano.view.midi.NoteMidiView;
import piano.view.midi.NoteMidiEditor;

public class PencilTool implements EditorTool {
    private final NoteMidiEditor view;
    private final EditorContext context;

    public PencilTool(NoteMidiEditor view, EditorContext context) {
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
                NoteData data = new NoteData(cellY, cellX, cellX + 1, 100);

                context.getNotes().clearSelection();
                context.getNotes().create(data);
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
                context.getNotes().delete(noteMidiView.getNoteEntry());
            }
        }

        return this;
    }
}
