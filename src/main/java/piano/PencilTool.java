package piano;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

class PencilTool implements EditorTool {
    private final NoteEditor editor;

    public PencilTool(NoteEditor editor) {
        super();
        this.editor = editor;
    }

    @Override
    public void onMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            PickResult pickResult = event.getPickResult();
            Point3D point = pickResult.getIntersectedPoint();
            System.out.println(point);
            Node node = pickResult.getIntersectedNode();
            if (node.equals(editor.getBackground())) {
                ObjectProperty<GridInfo> gridInfo = editor.getGridInfo();
                Note note = new Note(editor.getNotes(), point.getX(), point.getY(), gridInfo);
                editor.addNote(note);
            }
        }
    }
}
