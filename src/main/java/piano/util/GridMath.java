package piano.util;

import piano.model.GridInfo;


public class GridMath {
    public static double snapToGridX(GridInfo info, double x) {
        return Math.floor(x / info.getCellWidth()) * info.getCellWidth();
    }

    public static double snapToGridY(GridInfo info, double y) {
        return Math.floor(y / info.getCellHeight()) * info.getCellHeight();
    }

}
