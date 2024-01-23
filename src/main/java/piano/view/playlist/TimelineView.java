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
        int measures = context.getViewSettings().getGridInfo().getMeasures();
        for (int i = 0; i < measures; i ++) {
            TimelineMarker marker = new TimelineMarker(i, context);
            var gi = context.getViewSettings().getGridInfo();
            double x = i * gi.getTotalWidth() / measures;
            marker.setTranslateX(x);
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

        private double unsnappedStart;
        private double unsnappedEnd;

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
                unsnappedStart = getX();
                unsnappedEnd = getX() + getWidth();
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

            context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
                var playback = context.getPlayback().getState();
                double x = playback.getHead() * grid.getStepDisplayWidth();
                double width = playback.getDuration() * grid.getStepDisplayWidth();
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
            var grid = context.getViewSettings().getGridInfo();
            var playback = context.getPlayback();

            unsnappedStart += deltaX;
            double startSteps = grid.snapWorldXToNearestStep(unsnappedStart);
            double endSteps = playback.getState().getTail();

            if (!insideBounds(startSteps) | !insideBounds(endSteps)) {
                return;
            }

            setX(startSteps * grid.getStepDisplayWidth());
            setWidth((endSteps - startSteps) * grid.getStepDisplayWidth());

            playback.setHead(startSteps);
            playback.setTail(endSteps);
        }

        private void onCenterHandleDragged(MouseEvent event) {
            var grid = context.getViewSettings().getGridInfo();
            var playback = context.getPlayback();

            unsnappedStart += deltaX;
            double startSteps = grid.snapWorldXToNearestStep(unsnappedStart);

            unsnappedEnd += deltaX;
            double endSteps = grid.snapWorldXToNearestStep(unsnappedEnd);

            if (!insideBounds(startSteps) | !insideBounds(endSteps)) {
                return;
            }

            setX(startSteps * grid.getStepDisplayWidth());
            setWidth((endSteps - startSteps) * grid.getStepDisplayWidth());

            playback.setHead(startSteps);
            playback.setTail(endSteps);
        }

        private void onRightHandleDragged(MouseEvent event) {
            var grid = context.getViewSettings().getGridInfo();
            var playback = context.getPlayback();

            unsnappedEnd += deltaX;
            double endSteps = grid.snapWorldXToNearestStep(unsnappedEnd);
            double startSteps = playback.getState().getHead();

            if (!insideBounds(startSteps) | !insideBounds(endSteps)) {
                return;
            }

            setWidth((endSteps - startSteps) * grid.getStepDisplayWidth());
            playback.setTail(endSteps);
        }

        private boolean insideBounds(double steps) {
            var grid = context.getViewSettings().getGridInfo();
            return steps >= 0 && steps <= grid.getTotalSteps();
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

        TimelineMarker(int measure, MidiEditorContext context) {
            this.columnIndex = measure;

            text = new Text(Integer.toString(measure + 1));
            this.getChildren().addAll(text);

            // whenever gridInfo changes, repostion the marker
            context.getViewSettings().gridInfoProperty().addListener(($0, $1, grid) -> {
                double width = grid.getTotalWidth() / grid.getMeasures();
                this.setTranslateX(measure * width);
                this.setTranslateY(0);
            });
        }
    }
}
