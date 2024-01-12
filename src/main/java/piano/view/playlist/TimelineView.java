package piano.view.playlist;

import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piano.EditorContext;
import piano.model.NoteData;
import piano.model.NoteEntry;

import java.util.HashMap;
import java.util.Map;


public class TimelineView extends AnchorPane {
    private final EditorContext context;
    Rectangle background;
    Camera camera;
    Group world;

    class TimelineMarker extends StackPane {
        Text text;
        int columnIndex;

        TimelineMarker(int columnIndex, EditorContext context) {
            this.columnIndex = columnIndex;

            text = new Text(Integer.toString(columnIndex + 1));
            this.getChildren().addAll(text);

            // whenever gridInfo changes, repostion the marker
            context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
                this.setTranslateX(columnIndex * 16 * newValue.getCellWidth());
                this.setTranslateY(0);
            });
        }
    }


    public TimelineView(EditorContext context) {
        this.context = context;

        // Create a camera to view the 3D shapes
        camera = new ParallelCamera();
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-100);

        // Create the background surface which spans the entire grid area
        var gridInfo = context.getViewSettings().gridInfoProperty();
        background = gridInfo.get().createRectangle();
        background.setFill(createGridLineFill());
        gridInfo.addListener((observable, oldValue, newValue) -> {
            background.setWidth(newValue.getColumns() * newValue.getCellWidth());
            background.setHeight(newValue.getRows() * newValue.getCellHeight());
            background.setFill(createGridLineFill());
        });
        background.setTranslateZ(0);

        // Configure the world and the scene
        world = new Group(background);

        SubScene scene = new SubScene(world, 0, 0, true, SceneAntialiasing.BALANCED);

        // 125 is accounting for the width of the piano roll (not dynamic)
        scene.layoutXProperty().bind(this.layoutXProperty().add(125));
//        scene.layoutYProperty().bind(this.layoutYProperty());
        scene.widthProperty().bind(this.widthProperty().subtract(125));
        scene.heightProperty().bind(this.heightProperty());

        scene.setCamera(camera);
        scene.setManaged(false);
        scene.setRoot(world);
        scene.setFill(Color.TRANSPARENT);

        this.getChildren().add(scene);

        // generate the timeline markers and add them to the world
        int numColumns = context.getViewSettings().getGridInfo().getColumns();
        for (int i = 0; i < numColumns; i += 16) {
            TimelineMarker marker = new TimelineMarker(i / 16, context);
            marker.setTranslateX(i * context.getViewSettings().getGridInfo().getCellWidth());
            marker.setTranslateY(0);
            marker.setTranslateZ(0);
            world.getChildren().add(marker);
        }
    }

    public static Paint createGridLineFill() {
        return Color.TRANSPARENT;
    }

    public void scrollX(double v) {
        world.setTranslateX(v);
    }
}
