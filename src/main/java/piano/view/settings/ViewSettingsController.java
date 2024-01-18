package piano.view.settings;

import javafx.scene.control.*;
import javafx.stage.*;
import piano.*;

public class ViewSettingsController {
    private final MidiEditorContext context;
    public RadioButton showPianoRollNoteLetters;
    public RadioButton showNoteLetters;
    public Spinner<Integer> patternLengthSpinner;
    public ColorPicker colorPicker;
    public Button closeStageButton;

    private final Stage stage;

    public ViewSettingsController(MidiEditorContext context, Stage stage) {
        this.context = context;
        this.stage = stage;
    }

    public void initialize() {
        showPianoRollNoteLetters.selectedProperty().bindBidirectional(
                context.getViewSettings().showPianoRollNoteLettersProperty());

        showNoteLetters.selectedProperty().bindBidirectional(
                context.getViewSettings().showNoteLettersProperty());

        patternLengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 64));
        patternLengthSpinner.getValueFactory().setValue(context.getViewSettings().getGridInfo().getMeasures());
        patternLengthSpinner.setEditable(true);
        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            patternLengthSpinner.getValueFactory().setValue(grid.getMeasures());
        });
        patternLengthSpinner.valueProperty().addListener(($0, $1, grid) -> {
            context.getViewSettings().setGridInfo(context.getViewSettings().getGridInfo().withMeasures(grid));
        });

        colorPicker.setValue(context.getViewSettings().getPatternColor());
        context.getViewSettings().patternColorProperty().bind(colorPicker.valueProperty());

        closeStageButton.setOnAction(event -> closeStage());
    }

    public void closeStage() {
        stage.close();
    }
}
