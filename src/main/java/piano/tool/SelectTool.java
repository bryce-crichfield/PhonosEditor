package piano.tool;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.shape.Box;
import piano.tool.EditorTool;

public class SelectTool implements EditorTool {
    Box selectedBox = null;

    @Override
    public void onMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            PickResult pickResult = event.getPickResult();
            Node node = pickResult.getIntersectedNode();
            if (node instanceof Box box) {
                selectedBox = box;
            }
        }

        if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            selectedBox = null;
        }

        if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (selectedBox != null) {
                PickResult pickResult = event.getPickResult();
                Point3D point = pickResult.getIntersectedPoint();
                selectedBox.setTranslateX(point.getX());
                selectedBox.setTranslateY(point.getY());
                selectedBox.setTranslateZ(point.getZ());
            }
        }
    }
}
