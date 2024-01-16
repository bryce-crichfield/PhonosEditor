package piano.view.settings;

import javafx.beans.property.*;

public class ViewSettings {
    private final ObjectProperty<GridInfo> gridInfo;
    private final BooleanProperty showPianoRollNoteLetters;

    public ViewSettings(GridInfo gridInfo, boolean showPianoRollNoteLetters) {
        this.gridInfo = new SimpleObjectProperty<>(gridInfo);
        this.showPianoRollNoteLetters = new SimpleBooleanProperty(showPianoRollNoteLetters);
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
}
