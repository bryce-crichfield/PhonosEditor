package piano.view.settings;

import javafx.scene.control.RadioButton;
import piano.MidiEditorContext;

public class ViewSettingsController {
    private final MidiEditorContext context;
    public RadioButton showPianoRollNoteLetters;

    public ViewSettingsController(MidiEditorContext context) {
        this.context = context;
    }

    public void initialize() {
        showPianoRollNoteLetters.selectedProperty().bindBidirectional(
                context.getViewSettings().showPianoRollNoteLettersProperty());
    }
}
