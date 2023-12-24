package piano.tool;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import piano.tool.EditorTool;
import piano.view.NoteMidiEditor;

public class SelectTool implements EditorTool {
    private final Group world;
    private final NoteMidiEditor editor;
    Rectangle selectionBox;
    Point2D anchorPoint;

    public SelectTool(NoteMidiEditor editor, Group world) {
        this.world = world;
        this.editor = editor;
    }

    public void onSelectionStart(Point3D point) {
        selectionBox = new Rectangle();
        selectionBox.setTranslateX(point.getX());
        selectionBox.setTranslateY(point.getY());
        selectionBox.setTranslateZ(point.getZ());
        selectionBox.setWidth(0);
        selectionBox.setHeight(0);
        selectionBox.setMouseTransparent(true);
        selectionBox.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.25));
        selectionBox.setStrokeWidth(2);
        selectionBox.setStroke(Color.WHITE);
        selectionBox.setArcHeight(10);
        selectionBox.setArcWidth(10);
        world.getChildren().add(selectionBox);

        anchorPoint = new Point2D(point.getX(), point.getY());
    }

    public void onSelectionUpdate(Point3D point) {
        double x = Math.min(point.getX(), anchorPoint.getX());
        double y = Math.min(point.getY(), anchorPoint.getY());
        double width = Math.abs(point.getX() - anchorPoint.getX());
        double height = Math.abs(point.getY() - anchorPoint.getY());
        selectionBox.setTranslateX(x);
        selectionBox.setTranslateY(y);
        selectionBox.setWidth(width);
        selectionBox.setHeight(height);
    }

    public void onSelectionEnd(Point3D point) {
        // Find all the nodes that are inside the selection box
        world.getChildren().remove(selectionBox);
    }



    @Override
    public void onMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isPrimaryButtonDown()) {
            PickResult pickResult = event.getPickResult();
            Node node = pickResult.getIntersectedNode();
            if (node.equals(editor.getBackgroundSurface())) {
                onSelectionStart(pickResult.getIntersectedPoint());
            }
        }

        if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && event.isPrimaryButtonDown()) {
            PickResult pickResult = event.getPickResult();
            Node node = pickResult.getIntersectedNode();
            if (node.equals(editor.getBackgroundSurface())) {
                onSelectionUpdate(pickResult.getIntersectedPoint());
            }
        }

        if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            onSelectionEnd(null);
        }
    }
}
