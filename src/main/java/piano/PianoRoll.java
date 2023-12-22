package piano;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class PianoRoll extends AnchorPane {
    public static final int ROWS = 88;
    private Image fill;
    private Rectangle background;
    private ObjectProperty<GridInfo> gridInfo;
    private final double canvasHeight;
    private double scrollY;
    public PianoRoll(double canvasHeight, ObjectProperty<GridInfo> gridInfo) {
        super();

        setWidth(125);

        this.gridInfo = gridInfo;

        fill = generateFillImage(125, canvasHeight);

        background = new Rectangle();
        background.setWidth(125);
        background.heightProperty().bind(heightProperty());

        gridInfo.addListener((observable, oldValue, newValue) -> {
            fill = generateFillImage(125, canvasHeight);
            scroll(scrollY);
        });

        this.canvasHeight = canvasHeight;
        getChildren().add(background);

        scroll(0);
    }

    public void scroll(double deltaY) {
        scrollY = deltaY / canvasHeight;
        ImagePattern subImage = new ImagePattern(fill, 0, deltaY, getWidth(), canvasHeight, false);
        background.setFill(subImage);
    }

    private final static boolean[] keyMask = {true, false, true, false, true, true, false, true, false, true, false, true, true};

    private Image generateFillImage(double width, double height) {
        var gridInfo = this.gridInfo.get();
        double gridWidth = gridInfo.getCellWidth();
        double gridHeight = gridInfo.getCellHeight();

        Canvas canvas = new Canvas(width, height);
        var gc = canvas.getGraphicsContext2D();

        // Draw the white keys and the horizontal lines separating them, white keys span 2 rows
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.DARKGRAY);
        for (int row = 0; row < ROWS; row++) {
            gc.strokeLine(0, row * gridHeight, width, row * gridHeight);
        }

        // We are starting from C8 and going down to A0
        int keyIndex = 12;
        gc.setFill(Color.BLACK);
        for (int row = ROWS - 1; row >= 0; row--) {
            int y = (int) (row * gridHeight);
            if (!keyMask[keyIndex]) {
                gc.setFill(Color.BLACK);
                gc.fillRect(0, y, width, gridHeight);
            }

            if (keyIndex == 0) {
                keyIndex = 11;
            } else {
                keyIndex--;
            }
        }

        return canvas.snapshot(new SnapshotParameters(), null);
    }
}
