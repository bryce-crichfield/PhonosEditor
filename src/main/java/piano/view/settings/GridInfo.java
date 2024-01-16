package piano.view.settings;

import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.With;
import piano.Util;


@With
@Getter
public class GridInfo {
    public static final double MIN_CELL_WIDTH = 10;
    public static final double MIN_CELL_HEIGHT = 10;
    public static final double MAX_CELL_WIDTH = 128;
    public static final double MAX_CELL_HEIGHT = 128;
    private int rows;
    private int columns;
    private double cellWidth;
    private double cellHeight;

    public GridInfo(int rows, int columns, double cellWidth, double cellHeight) {
        cellWidth = Util.clamp(cellWidth, MIN_CELL_WIDTH, MAX_CELL_WIDTH);
        cellHeight = Util.clamp(cellHeight, MIN_CELL_HEIGHT, MAX_CELL_HEIGHT);

        this.rows = rows;
        this.columns = columns;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    public Rectangle createRectangle() {
        return new Rectangle(0, 0, (int) cellWidth * columns, (int) cellHeight * rows);
    }

    public double getTotalWidth() {
        return columns * cellWidth;
    }

    public double getTotalHeight() {
        return rows * cellHeight;
    }
}
