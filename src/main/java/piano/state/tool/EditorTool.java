package piano.state.tool;

import javafx.scene.input.*;

public interface EditorTool {
    void onEnter();

    EditorTool onMouseEvent(MouseEvent event);
}
