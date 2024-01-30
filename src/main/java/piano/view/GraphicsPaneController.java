package piano.view;

import atlantafx.base.controls.*;
import javafx.scene.control.*;
import javafx.stage.*;
import piano.*;
import piano.view.zoom.*;

public class GraphicsPaneController {
    private final EditorContext context;
    public ToggleSwitch showPianoRollNoteLetters;
    public ToggleSwitch showNoteLetters;
    public ToggleSwitch smoothScrollEnabled;
    public ToggleSwitch smoothZoomEnabled;
    public Spinner<Integer> patternLengthSpinner;
    public Spinner<Integer> timeNumeratorSpinner;
    public Spinner<Integer> timeDenominatorSpinner;
    public ColorPicker colorPicker;


    public GraphicsPaneController(EditorContext context) {
        this.context = context;
    }

    public void initialize() {
        showPianoRollNoteLetters.selectedProperty().bindBidirectional(
                context.getViewSettings().showPianoRollNoteLettersProperty());

        showNoteLetters.selectedProperty().bindBidirectional(
                context.getViewSettings().showNoteLettersProperty());

        smoothZoomEnabled.selectedProperty().bindBidirectional(
                context.getViewSettings().smoothZoomEnabled);

        smoothScrollEnabled.selectedProperty().bindBidirectional(
                context.getViewSettings().smoothScrollEnabled);

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

        timeNumeratorSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16));
        timeNumeratorSpinner.getValueFactory().setValue(4);
        timeNumeratorSpinner.setEditable(true);
        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            TimeSignature time = grid.getTime();
            timeNumeratorSpinner.getValueFactory().setValue(time.getNumerator());
        });

        timeNumeratorSpinner.valueProperty().addListener(($0, $1, grid) -> {
            TimeSignature time = context.getViewSettings().getGridInfo().getTime();
            context.getViewSettings().setGridInfo(context.getViewSettings().getGridInfo().withTime(time.withNumerator(grid)));
        });

        timeDenominatorSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16));
        timeDenominatorSpinner.getValueFactory().setValue(4);
        timeDenominatorSpinner.setEditable(true);
        context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
            TimeSignature time = grid.getTime();
            timeDenominatorSpinner.getValueFactory().setValue(time.getDenominator());
        });

        timeDenominatorSpinner.valueProperty().addListener(($0, $1, grid) -> {
            TimeSignature time = context.getViewSettings().getGridInfo().getTime();
            context.getViewSettings().setGridInfo(context.getViewSettings().getGridInfo().withTime(time.withDenominator(grid)));
        });
    }
}
