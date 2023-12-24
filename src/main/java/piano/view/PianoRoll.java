package piano.view;

import javafx.beans.property.ObjectProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import piano.model.GridInfo;

public class PianoRoll extends AnchorPane {
    public static final int ROWS = 88;
    public static final double DEFAULT_WIDTH = 125;
    private Image fill;
    private ObjectProperty<GridInfo> gridInfo;
    private final double canvasHeight;
    private double scrollY;
    public PianoRoll(double canvasHeight, ObjectProperty<GridInfo> gridInfo) {
        super();

        setMinWidth(125);
        setStyle("-fx-background-color: #000ff0");

        this.gridInfo = gridInfo;

        fill = generateFillImage(125, canvasHeight);

        gridInfo.addListener((observable, oldValue, newValue) -> {
            fill = generateFillImage(125, canvasHeight);
            scrollY(scrollY);
        });

        this.canvasHeight = canvasHeight;

        scrollY(0);
    }

    public void scrollY(double deltaY) {
        scrollY = deltaY / canvasHeight;
        ImagePattern subImage = new ImagePattern(fill, 0, deltaY, getWidth(), canvasHeight, false);
        var background = new Background(new javafx.scene.layout.BackgroundFill(subImage, null, null));
        setBackground(background);
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
