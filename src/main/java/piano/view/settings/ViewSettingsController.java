package piano.view.settings;

import javafx.scene.control.RadioButton;
import piano.EditorContext;

public class ViewSettingsController {
    public RadioButton showPianoRollNoteLetters;

    private final EditorContext context;

    public ViewSettingsController(EditorContext context) {
        this.context = context;
    }

    public void initialize() {
        showPianoRollNoteLetters.selectedProperty().bindBidirectional(context.getViewSettings().showPianoRollNoteLettersProperty());
    }
}
