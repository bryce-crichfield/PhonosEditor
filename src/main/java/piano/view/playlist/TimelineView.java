package piano.view.playlist;

import component.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import piano.*;


public class TimelineView extends AnchorPane {
    private final MidiEditorContext context;
    Rectangle background;
    Camera camera;
    Group world;

    public TimelineView(MidiEditorContext context) {
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
            background.setWidth(newValue.getMeasures() * newValue.getBeatDisplayWidth());
            background.setHeight(newValue.getRows() * newValue.getCellHeight());
            background.setFill(createGridLineFill());
        });
        background.setTranslateZ(0);

        // Configure the world and the scene
        world = new Group(background);

        SubScene scene = new SubScene(world, 0, 0, true, SceneAntialiasing.BALANCED);

        // 125 is accounting for the width of the piano roll (not dynamic)
        scene.layoutXProperty().bind(this.layoutXProperty().add(125));
        scene.widthProperty().bind(this.widthProperty().subtract(125));
        scene.heightProperty().bind(this.heightProperty());

        scene.setCamera(camera);
        scene.setManaged(false);
        scene.setRoot(world);
        scene.setFill(ScrollBar.TRACK_COLOR);

        this.getChildren().add(scene);

        // generate the timeline markers and add them to the world
        int numColumns = context.getViewSettings().getGridInfo().getMeasures();
        for (int i = 0; i < numColumns; i += 16) {
            TimelineMarker marker = new TimelineMarker(i / 16, context);
            marker.setTranslateX(i * context.getViewSettings().getGridInfo().getBeatDisplayWidth());
            marker.setTranslateY(0);
            marker.setTranslateZ(10);
            world.getChildren().add(marker);
        }


        PlayheadHandle playheadHandle = new PlayheadHandle(context);
        playheadHandle.heightProperty().bind(this.heightProperty());
        world.getChildren().add(playheadHandle);
    }

    public static Paint createGridLineFill() {
        return Color.TRANSPARENT;
    }

    public void scrollX(double v) {
        world.setTranslateX(v);
    }

    private static class PlayheadHandle extends Rectangle {
        private static final int HANDLE_WIDTH = 15;
        private final MidiEditorContext context;
        private final StringProperty currentHandle = new SimpleStringProperty("None");
        private boolean isDragging = false;

        private double unsnappedX = 0;
        private double unsnappedWidth = 0;

        private double lastX = 0;
        private double deltaX = 0;

        public PlayheadHandle(MidiEditorContext context) {

            this.setFill(Color.CYAN.deriveColor(1, 1, 1, 0.75));
            this.setTranslateZ(0);
            this.setOpacity(0.5);

            this.context = context;

            this.setOnMouseMoved((event) -> {
                if (this.isDragging) {
                    return;
                }

                chooseHandle(event);
            });

            this.setOnMousePressed((event) -> {
                this.isDragging = true;
                unsnappedX = getX();
                unsnappedWidth = getWidth();
                lastX = event.getX();
                chooseHandle(event);
            });

            this.setOnMouseDragged(this::onMouseDragged);

            this.setOnMouseReleased((event) -> {
                this.isDragging = false;
                releaseHandle(event);
            });

            this.setWidth(100);

            currentHandle.addListener((observable, oldVal, newVal) -> {
                switch (newVal) {
                    case "Left":
                        setCursor(Cursor.W_RESIZE);
                        break;
                    case "Center":
                        setCursor(Cursor.MOVE);
                        break;
                    case "Right":
                        setCursor(Cursor.E_RESIZE);
                        break;
                    default:
                        setCursor(Cursor.DEFAULT);
                        break;
                }
            });

            context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
                var gi = newValue;
                var playback = context.getPlayback().getState();
                double x = playback.getHead() * gi.getBeatDisplayWidth();
                double width = (playback.getTail() - playback.getHead()) * gi.getBeatDisplayWidth();
                setX(x);
                setWidth(width);
            });
        }

        private void onMouseDragged(MouseEvent event) {
            double nowX = event.getX();
            deltaX = nowX - lastX;
            lastX = nowX;

            switch (currentHandle.get()) {
                case "Left":
                    onLeftHandleDragged(event);
                    break;
                case "Center":
                    onCenterHandleDragged(event);
                    break;
                case "Right":
                    onRightHandleDragged(event);
                    break;
                default:
                    break;
            }
        }

        private void onLeftHandleDragged(MouseEvent event) {
            var gi = context.getViewSettings().getGridInfo();
            var playback = context.getPlayback();

            double x = unsnappedX + deltaX;
            x = gi.snapWorldXToNearestStep(x) * gi.getStepDisplayWidth();
            x = Util.clamp(x, 0, gi.getTotalWidth() - gi.getBeatDisplayWidth());
            unsnappedX += deltaX;

            double width = unsnappedWidth - deltaX;
            width =(gi.snapWorldXToNearestStep(width) + gi.getStepDisplayWidth()) + gi.getBeatDisplayWidth();
            width = Util.clamp(width, gi.getBeatDisplayWidth(), gi.getTotalWidth() - x);
            unsnappedWidth -= deltaX;

            double head = Math.floor(x / gi.getBeatDisplayWidth());
            double tail = Math.floor((x + width) / gi.getBeatDisplayWidth());

            setX(x);
            setWidth(width);
            playback.setHead(head);
            playback.setTail(tail);
        }

        private void onCenterHandleDragged(MouseEvent event) {
            var gi = context.getViewSettings().getGridInfo();
            var playback = context.getPlayback();
            var x = unsnappedX + deltaX;
            x = gi.snapWorldXToNearestStep(x) * gi.getStepDisplayWidth();
            x = Util.clamp(x, 0, gi.getTotalWidth() - getWidth());
            unsnappedX += deltaX;
            setX(x);
            playback.setHead(Math.floor(x / gi.getBeatDisplayWidth()));
            playback.setTail(Math.floor((x + getWidth()) / gi.getBeatDisplayWidth()));
        }

        private void onRightHandleDragged(MouseEvent event) {
            var gi = context.getViewSettings().getGridInfo();
            var playback = context.getPlayback();
            double width = unsnappedWidth + deltaX;
            width = gi.snapWorldXToNearestStep(width) * gi.getStepDisplayWidth();
            width = Util.clamp(width, gi.getBeatDisplayWidth(), gi.getTotalWidth() - getX());
            unsnappedWidth += deltaX;
            setWidth(width);
            playback.setTail(Math.floor((unsnappedX + width) / gi.getBeatDisplayWidth()));
        }


        private void chooseHandle(MouseEvent event) {
            if (event.getX() < getX() + HANDLE_WIDTH) {
                currentHandle.set("Left");
            } else if (event.getX() > getX() + getWidth() - HANDLE_WIDTH) {
                currentHandle.set("Right");
            } else {
                currentHandle.set("Center");
            }
        }

        private void releaseHandle(MouseEvent event) {
            currentHandle.set("None");
        }
    }

    class TimelineMarker extends StackPane {
        Text text;
        int columnIndex;

        TimelineMarker(int columnIndex, MidiEditorContext context) {
            this.columnIndex = columnIndex;

            text = new Text(Integer.toString(columnIndex + 1));
            this.getChildren().addAll(text);

            // whenever gridInfo changes, repostion the marker
            context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
                this.setTranslateX(columnIndex * 16 * newValue.getBeatDisplayWidth());
                this.setTranslateY(0);
            });
        }
    }
}
