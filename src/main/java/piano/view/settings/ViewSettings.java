package piano.view.settings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import piano.model.GridInfo;

public class ViewSettings {
    private final ObjectProperty<GridInfo> gridInfo;

    public ViewSettings(GridInfo gridInfo) {
        this.gridInfo = new SimpleObjectProperty<>(gridInfo);
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
}
