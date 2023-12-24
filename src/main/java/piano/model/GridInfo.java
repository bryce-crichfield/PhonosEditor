package piano.model;

import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.With;


@With
@Getter
public class GridInfo {
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
}
