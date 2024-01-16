package piano.model;

import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.With;


@With
@Getter
public class GridInfo {
    public static final double MIN_CELL_WIDTH = 10;
    public static final double MAX_CELL_WIDTH = 24;
    public static final double MIN_CELL_HEIGHT = 10;
    public static final double MAX_CELL_HEIGHT = 24;

    private int rows;
    private int columns;
    private double cellWidth;
    private double cellHeight;

    public GridInfo(int rows, int columns, double cellWidth, double cellHeight) {
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
