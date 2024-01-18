package piano.view.settings;

import javafx.beans.property.*;
import javafx.scene.paint.*;

public class ViewSettings {
    private final ObjectProperty<GridInfo> gridInfo;
    private final BooleanProperty showPianoRollNoteLetters;
    private final BooleanProperty showNoteLetters;
    private final ObjectProperty<Color> patternColor;

    public ViewSettings(GridInfo gridInfo, boolean showPianoRollNoteLetters) {
        this.gridInfo = new SimpleObjectProperty<>(gridInfo);
        this.showPianoRollNoteLetters = new SimpleBooleanProperty(showPianoRollNoteLetters);
        this.showNoteLetters = new SimpleBooleanProperty(true);
        this.patternColor = new SimpleObjectProperty<>(Theme.ORANGE);
    }

    public GridInfo getGridInfo() {
        return gridInfo.get();
    }

    public void setGridInfo(GridInfo gridInfo) {
        this.gridInfo.set(gridInfo);
    }

    public ObjectProperty<GridInfo> gridInfoProperty() {
        return gridInfo;
    }

    public boolean isShowPianoRollNoteLetters() {
        return showPianoRollNoteLetters.get();
    }

    public void setShowPianoRollNoteLetters(boolean showPianoRollNoteLetters) {
        this.showPianoRollNoteLetters.set(showPianoRollNoteLetters);
    }

    public BooleanProperty showPianoRollNoteLettersProperty() {
        return showPianoRollNoteLetters;
    }

    public Color getPatternColor() {
        return patternColor.get();
    }

    public void setPatternColor(Color patternColor) {
        this.patternColor.set(patternColor);
    }

    public ObjectProperty<Color> patternColorProperty() {
        return patternColor;
    }

    public boolean isShowNoteLetters() {
        return showNoteLetters.get();
    }

    public void setShowNoteLetters(boolean showNoteLetters) {
        this.showNoteLetters.set(showNoteLetters);
    }

    public BooleanProperty showNoteLettersProperty() {
        return showNoteLetters;
    }
}
