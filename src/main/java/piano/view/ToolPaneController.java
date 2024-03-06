package piano.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import piano.EditorContext;
import util.FxUtil;

public class ToolPaneController {
    public ToggleButton toggleSelectTool;
    public ToggleButton togglePencilTool;
    public ToggleButton toggleSliceTool;
    public Button playlistPause;
    public Button playlistPlay;
    public Button playlistStop;
    EditorContext context;

    public ToolPaneController(EditorContext context) {
        this.context = context;
    }

    public static ToolPaneController create(EditorContext context) {
        FXMLLoader loader = FxUtil.load("/fxml/ToolPane.fxml");
        ToolPaneController controller = new ToolPaneController(context);
        loader.setController(controller);

        try {
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return loader.getController();
    }

    public void setSelectTool(ActionEvent actionEvent) {
    }

    public void setPencilTool(ActionEvent actionEvent) {
    }

    public void setSliceTool(ActionEvent actionEvent) {
    }

    public void playlistPause(ActionEvent actionEvent) {
    }

    public void playlistPlay(ActionEvent actionEvent) {
    }

    public void playlistStop(ActionEvent actionEvent) {
    }

    public void serializeNoteRegistry(ActionEvent actionEvent) {
    }
}
