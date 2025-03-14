import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {
    static class NoteData {
        private final int startStep;
        private final int endStep;
        private final NotePitch pitch;

        public NoteData(int startStep, int endStep, NotePitch pitch) {
            this.startStep = startStep;
            this.endStep = endStep;
            this.pitch = pitch;
        }

        public int getStartStep() { return startStep; }
        public int getEndStep() { return endStep; }
        public NotePitch getPitch() { return pitch; }
    }

    static class NotePitch {
        private final int noteIndex;

        public NotePitch(int noteIndex) {
            this.noteIndex = noteIndex;
        }

        public int getNoteIndex() { return noteIndex; }
    }

    static class GridModel {
        private final int stepsPerBeat;

        public GridModel(int stepsPerBeat) {
            this.stepsPerBeat = stepsPerBeat;
        }

        public int getStepsPerBeat() {
            return stepsPerBeat;
        }

        public int getBeatsPerMeasures() {
            return 4;
        }

        public int getStepsPerMeasures() {
            return getBeatsPerMeasures() * stepsPerBeat;
        }
    }

    static class GridViewModel {
        private final GridModel model;
        private final ZoomState zoomState;
        private final DoubleProperty baseCellWidth = new SimpleDoubleProperty(32);
        private final DoubleProperty baseCellHeight = new SimpleDoubleProperty(16);

        GridViewModel(GridModel model, ZoomState zoomState) {
            this.model = model;
            this.zoomState = zoomState;
        }

        public double getCellWidth() {
            return baseCellWidth.get() * zoomState.getHorizontalZoom();
        }

        public double getCellHeight() {
            return baseCellHeight.get() * zoomState.getVerticalZoom();
        }

        public double stepToPixelX(int step) {
            return step * (getCellWidth() / model.getStepsPerBeat());
        }

        public int pixelToStep(double pixelX) {
            return (int)(pixelX / (getCellWidth() / model.getStepsPerBeat()));
        }

        public int getStepsPerBeat() {
            return model.getStepsPerBeat();
        }

        public int getStepsPerMeasure() {
            return model.getStepsPerMeasures();
        }
    }

    static class ScrollState {
        private final DoubleProperty horizontalScrollPosition = new SimpleDoubleProperty(0);
        private final DoubleProperty verticalScrollPosition = new SimpleDoubleProperty(0);

        public void scrollHorizontal(double delta) {
            horizontalScrollPosition.set(horizontalScrollPosition.get() + delta);
        }

        public void scrollVertical(double delta) {
            verticalScrollPosition.set(verticalScrollPosition.get() + delta);
        }

        public double getHorizontalScroll() {
            return horizontalScrollPosition.get();
        }

        public double getVerticalScroll() {
            return verticalScrollPosition.get();
        }
    }

    static class ZoomState {
        private final DoubleProperty horizontalZoom = new SimpleDoubleProperty(1.0);
        private final DoubleProperty verticalZoom = new SimpleDoubleProperty(1.0);

        public double getHorizontalZoom() {
            return horizontalZoom.get();
        }

        public DoubleProperty horizontalZoomProperty() {
            return horizontalZoom;
        }

        public double getVerticalZoom() {
            return verticalZoom.get();
        }

        public DoubleProperty verticalZoomProperty() {
            return verticalZoom;
        }

        public void zoomHorizontal(double factor, double focusX) {
            horizontalZoom.set(Math.max(0.1, Math.min(10.0, horizontalZoom.get() * factor)));
        }

        public void zoomVertical(double factor, double focusY) {
            verticalZoom.set(Math.max(0.1, Math.min(10.0, verticalZoom.get() * factor)));
        }
    }

    static class NoteQuadTree {
        private static final int MAX_NOTES_PER_NODE = 10;
        private static final int MAX_DEPTH = 8;
        private QuadTreeNode root;

        public NoteQuadTree(int startStep, int endStep, int lowestPitch, int highestPitch) {
            this.root = new QuadTreeNode(new Rectangle(startStep, lowestPitch,
                    endStep - startStep, highestPitch - lowestPitch), 0);
        }

        public void insert(NoteData note) {
            if (!containsNote(note)) {
                return;
            }
            root.insert(note);
        }

        private boolean containsNote(NoteData note) {
            return note.getStartStep() >= root.bounds.getX() &&
                    note.getEndStep() <= root.bounds.getX() + root.bounds.getWidth() &&
                    note.getPitch().getNoteIndex() >= root.bounds.getY() &&
                    note.getPitch().getNoteIndex() <= root.bounds.getY() + root.bounds.getHeight();
        }

        public List<NoteData> queryRange(int startStep, int endStep, int lowestPitch, int highestPitch) {
            List<NoteData> result = new ArrayList<>();
            Rectangle queryRect = new Rectangle(startStep, lowestPitch, endStep - startStep, highestPitch - lowestPitch);
            root.queryRange(queryRect, result);
            return result;
        }

        private static class QuadTreeNode {
            private final Rectangle bounds;
            private final List<NoteData> notes;
            private QuadTreeNode[] children;
            private final int depth;

            public QuadTreeNode(Rectangle bounds, int depth) {
                this.bounds = bounds;
                this.notes = new ArrayList<>();
                this.children = null;
                this.depth = depth;
            }

            public void insert(NoteData note) {
                if (depth >= MAX_DEPTH) {
                    notes.add(note);
                    return;
                }

                if (children == null && notes.size() < MAX_NOTES_PER_NODE) {
                    notes.add(note);
                    return;
                }

                if (children == null) {
                    split();
                }

                for (int i = 0; i < 4; i++) {
                    if (intersects(note, children[i].bounds)) {
                        children[i].insert(note);
                    }
                }
            }

            private boolean intersects(NoteData note, Rectangle rect) {
                return note.getStartStep() <= rect.getX() + rect.getWidth() &&
                        note.getEndStep() >= rect.getX() &&
                        note.getPitch().getNoteIndex() <= rect.getY() + rect.getHeight() &&
                        note.getPitch().getNoteIndex() >= rect.getY();
            }

            private void split() {
                double x = bounds.getX();
                double y = bounds.getY();
                double halfWidth = bounds.getWidth() / 2;
                double halfHeight = bounds.getHeight() / 2;

                children = new QuadTreeNode[4];
                children[0] = new QuadTreeNode(new Rectangle(x, y, halfWidth, halfHeight), depth + 1);
                children[1] = new QuadTreeNode(new Rectangle(x + halfWidth, y, halfWidth, halfHeight), depth + 1);
                children[2] = new QuadTreeNode(new Rectangle(x, y + halfHeight, halfWidth, halfHeight), depth + 1);
                children[3] = new QuadTreeNode(new Rectangle(x + halfWidth, y + halfHeight, halfWidth, halfHeight), depth + 1);

                List<NoteData> oldNotes = new ArrayList<>(notes);
                notes.clear();

                for (NoteData note : oldNotes) {
                    insert(note);
                }
            }

            public void queryRange(Rectangle queryRect, List<NoteData> result) {
                Bounds bounds = new BoundingBox(this.bounds.getX(), this.bounds.getY(),
                        this.bounds.getWidth(), this.bounds.getHeight());
                if (!queryRect.intersects(bounds)) {
                    return;
                }

                for (NoteData note : notes) {
                    if (intersects(note, queryRect)) {
                        result.add(note);
                    }
                }

                if (children != null) {
                    for (QuadTreeNode child : children) {
                        child.queryRange(queryRect, result);
                    }
                }
            }
        }
    }

    static class GridRenderer extends Region {
        private final GridViewModel viewModel;
        private final ScrollState scrollState;
        private final NoteQuadTree noteQuadTree;

        GridRenderer(GridViewModel viewModel, ScrollState scrollState, NoteQuadTree noteQuadTree) {
            this.viewModel = viewModel;
            this.scrollState = scrollState;
            this.noteQuadTree = noteQuadTree;

            // Add scroll handling
            setOnScroll(e -> {
                if (e.isShiftDown()) {
                    scrollState.scrollHorizontal(-e.getDeltaY());
                } else {
                    scrollState.scrollVertical(-e.getDeltaY());
                }
                requestLayout();
            });

            // Add mouse drag panning
            setOnMousePressed(e -> {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            });

            setOnMouseDragged(e -> {
                double deltaX = lastMouseX - e.getX();
                double deltaY = lastMouseY - e.getY();
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                scrollState.scrollHorizontal(deltaX);
                scrollState.scrollVertical(deltaY);
                requestLayout();
            });
        }

        private double lastMouseX, lastMouseY;

        @Override
        protected void layoutChildren() {
            getChildren().clear();

            double viewportWidth = getWidth();
            double scrollX = scrollState.getHorizontalScroll();
            double scrollY = scrollState.getVerticalScroll();

            int startStep = viewModel.pixelToStep(scrollX);
            int endStep = viewModel.pixelToStep(scrollX + viewportWidth) + viewModel.getStepsPerMeasure();

            startStep = Math.floorDiv(startStep, viewModel.getStepsPerMeasure()) * viewModel.getStepsPerMeasure();

            for (int step = startStep; step <= endStep; step++) {
                double x = viewModel.stepToPixelX(step) - scrollX;

                Line line = new Line(x, 0, x, getHeight());

                if (step % viewModel.getStepsPerMeasure() == 0) {
                    line.setStrokeWidth(2.0);
                    line.setStyle("-fx-stroke: white;");
                } else if (step % viewModel.getStepsPerBeat() == 0) {
                    line.setStrokeWidth(1.0);
                    line.setStyle("-fx-stroke: lightgray;");
                } else {
                    line.setStrokeWidth(0.5);
                    line.setStyle("-fx-stroke: #444444;");
                }

                getChildren().add(line);

                // Add measure numbers for measure lines
                if (step % viewModel.getStepsPerMeasure() == 0) {
                    javafx.scene.text.Text text = new javafx.scene.text.Text(x + 5, 20,
                            String.valueOf(step / viewModel.getStepsPerMeasure() + 1));
                    text.setStyle("-fx-fill: white;");
                    getChildren().add(text);
                }
            }

        }
    }

    static class NoteRenderer extends Region {
        private final GridViewModel gridViewModel;
        private final NoteQuadTree noteQuadTree;
        private final ScrollState scrollState;

        NoteRenderer(GridViewModel gridViewModel, NoteQuadTree noteQuadTree, ScrollState scrollState) {
            this.gridViewModel = gridViewModel;
            this.noteQuadTree = noteQuadTree;
            this.scrollState = scrollState;
            setPickOnBounds(false);
        }

        @Override
        protected void layoutChildren() {
            getChildren().clear();

            double viewportWidth = getWidth();
            double scrollX = scrollState.getHorizontalScroll();
            double scrollY = scrollState.getVerticalScroll();

            int startStep = gridViewModel.pixelToStep(scrollX);
            int endStep = gridViewModel.pixelToStep(scrollX + viewportWidth) + gridViewModel.getStepsPerMeasure();

            startStep = Math.floorDiv(startStep, gridViewModel.getStepsPerMeasure()) * gridViewModel.getStepsPerMeasure();
            List<NoteData> visibleNotes = noteQuadTree.queryRange(startStep, endStep, 0, 88);

            for (NoteData note : visibleNotes) {
                // Calculate note position and size
                double x = gridViewModel.stepToPixelX(note.getStartStep()) - scrollX;
                double width = gridViewModel.stepToPixelX(note.getEndStep() - note.getStartStep());

                // Map MIDI pitch to Y coordinate (simplified)
                double y = mapPitchToY(note.getPitch().getNoteIndex()) - scrollY;
                double height = 20; // Fixed height for this example

                Rectangle noteRect = new Rectangle(x, y, width, height);
                noteRect.setArcWidth(5);
                noteRect.setArcHeight(5);
                noteRect.setFill(javafx.scene.paint.Color.DODGERBLUE);
                noteRect.setStroke(javafx.scene.paint.Color.WHITE);

                getChildren().add(noteRect);
            }
        }

        private double mapPitchToY(int pitch) {
            // Simple mapping of MIDI pitch to Y coordinate
            // In a real piano roll, this would be more sophisticated
            return (88 - pitch) * 20; // 20px per semitone, inverted (higher = lower Y)
        }
    }
    class EventBus {
        private final Map<EventType<?>, List<EventHandler<? extends Event>>> handlers = new HashMap<>();

        public <T extends Event> void subscribe(EventType<T> type, EventHandler<T> handler) {
            handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        }

        @SuppressWarnings("unchecked")
        public <T extends Event> void publish(T event) {
            List<EventHandler<? extends Event>> typeHandlers = handlers.getOrDefault(event.getEventType(), List.of());
            for (EventHandler<? extends Event> h : typeHandlers) {
                ((EventHandler<T>)h).handle(event);
            }
        }
    }

    @Getter
    class ZoomEvent extends Event {
        public static final EventType<ZoomEvent> ZOOM = new EventType<>(Event.ANY, "ZOOM");
        private final double factor;
        private final double focusX;
        private final double focusY;
        private final boolean horizontalOnly;

        public ZoomEvent(double factor, double focusX, double focusY, boolean horizontalOnly) {
            super(ZOOM);
            this.factor = factor;
            this.focusX = focusX;
            this.focusY = focusY;
            this.horizontalOnly = horizontalOnly;
        }

        // Getters
    }

    @Override
    public void start(Stage stage) {
        EventBus eventBus = new EventBus();
        GridModel gridModel = new GridModel(4);
        ScrollState scrollState = new ScrollState();
        ZoomState zoomState = new ZoomState();
        GridViewModel gridViewModel = new GridViewModel(gridModel, zoomState);
        NoteQuadTree noteQuadTree = new NoteQuadTree(0, 100, 0, 88);
        for (int i = 0; i < 100; i++) {
            var startStep = i * 4;
            var endStep = startStep + 4;
            noteQuadTree.insert(new NoteData(startStep, endStep, new NotePitch(i % 88)));
        }
        GridRenderer gridRenderer = new GridRenderer(gridViewModel, scrollState, noteQuadTree);
        NoteRenderer noteRenderer = new NoteRenderer(gridViewModel, noteQuadTree, scrollState);

        StackPane stackPane = new StackPane(gridRenderer, noteRenderer);
        stackPane.setStyle("-fx-background-color: #2D2D30;");

        stackPane.setOnScroll(e -> {
            if (e.isControlDown()) {
                // Zoom
                double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
                if (e.isShiftDown()) {
                    zoomState.zoomHorizontal(zoomFactor, e.getX());
                } else {
                    zoomState.zoomHorizontal(zoomFactor, e.getX());
                    zoomState.zoomVertical(zoomFactor, e.getY());
                }
            } else {
                // Scroll
                if (e.isShiftDown()) {
                    scrollState.scrollHorizontal(-e.getDeltaY());
                } else {
                    scrollState.scrollVertical(-e.getDeltaY());
                }
            }
            gridRenderer.requestLayout();
            noteRenderer.requestLayout();
        });

        stackPane.setOnMouseClicked(e -> {
            gridRenderer.fireEvent(e.copyFor(gridRenderer, gridRenderer));
        });

        stackPane.setOnMouseDragged(e -> {
            gridRenderer.fireEvent(e.copyFor(gridRenderer, gridRenderer));
        });


        Scene scene = new Scene(stackPane, 800, 600);
        stage.setTitle("Infinite Grid Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}