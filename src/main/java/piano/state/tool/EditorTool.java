package piano.state.tool;

import javafx.scene.input.MouseEvent;

public interface EditorTool {
    void onEnter();

    EditorTool onMouseEvent(MouseEvent event);
}
