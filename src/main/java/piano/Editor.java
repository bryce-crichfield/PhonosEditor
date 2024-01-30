package piano;

import component.ScrollBar;
import component.*;
import config.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import piano.state.note.*;
import piano.state.note.model.*;
import piano.state.playback.*;
import piano.state.tool.*;
import piano.view.note.*;
import piano.view.parameter.*;
import piano.view.piano.*;
import piano.view.playlist.*;
import piano.view.zoom.*;
import util.*;

import java.util.*;

public class Editor {
    private final ObjectProperty<Optional<EditorTool>> currentTool = new SimpleObjectProperty<>(Optional.empty());
    // FXML (Tool bar)
    public ToggleButton toggleToolSelect;
    public ToggleButton toggleToolPencil;
    public ToggleButton toggleToolSlice;
    public BorderPane bodyBorderPane;
    public AnchorPane root;
    public VBox toolBarRoot;
    public BorderPane rootBorderPane;
    ToggleGroup tools = new ToggleGroup();
    // Non-FXML (Note editor)
    private SplitPane splitPane;
    private NotesPane noteViewEditor;
    private ParametersPane parametersPane;
    private KeysPane pianoView;
    private EditorContext context;

    private ScrollBar verticalScrollBar;
    private ScrollBar horizontalScrollBar;


    public Editor() {
        super();
    }

    public void initialize() {
        bodyBorderPane = new BorderPane();

        // Create the editor context -----------------------------------------------------------------------------------
        {
            var gridInfo = new GridInfo(88, 16, 32, 16, 1);
            var viewSettings = new ViewSettings(gridInfo, true);

            var playbackState = new PlaybackState(0, 256, 120, false);

            var playbackService = new BasePlaybackService(new SimpleObjectProperty<>(playbackState));
            var noteService = new NoteService();
            context = new EditorContext(playbackService, noteService, viewSettings);

            playbackService.observe((String noteName) -> {
                System.out.println("Triggering note: " + noteName);
            });
        }


        // Initialize note pattern and property editors ----------------------------------------------------------------
        {
            // The pattern editor represents the world as a large rectangle with a grid drawn on it.  The large
            // rectangle is the background surface, and the grid is drawn on top of it.
            noteViewEditor = new NotesPane(context, currentTool);
            bodyBorderPane.setCenter(noteViewEditor);

            parametersPane = new ParametersPane(context);
            parametersPane.setMinHeight(100);
            parametersPane.setMaxHeight(500);
        }

        initVerticalScrollBar();
        initHorizontalScrollBar();

        // Initialize scrolling ---------------------------------------------------------------------------------------
        SmoothZoomController zoomController = new SmoothZoomController(context);
        zoomController.spawn();

        noteViewEditor.setOnScroll((EventHandler<? super ScrollEvent>) event -> {
            // None  -> Vertical scroll
            // Shift -> Horizontal scroll
            // Ctrl -> Zoom Both (Vertical and Horizontal)
            // Ctrl + Shift -> Zoom Horizontal
            // Ctrl + Alt -> Zoom Vertical

            double horizontalDelta = event.getDeltaX();
            double verticalDelta = event.getDeltaY();

            if (event.isControlDown() && event.isShiftDown()) {
                zoomController.shoveHorizontal(horizontalDelta);
                return;
            }

            if (event.isControlDown() && event.isAltDown()) {
                zoomController.shoveVertical(-verticalDelta);
                return;
            }

            if (event.isControlDown()) {
                zoomController.shoveHorizontal(horizontalDelta);
                zoomController.shoveVertical(verticalDelta);
                return;
            }

            if (event.isShiftDown()) {
                horizontalScrollBar.scrollBy(horizontalDelta);
                return;
            }

            verticalScrollBar.scrollBy(verticalDelta);
        });

        // When dragging the mouse, scroll the screen if the mouse is near the edge of the screen ----------------------
        noteViewEditor.setOnMouseDragged(event -> {
            // if nearing the edge of the screen, scroll the screen
            double x = event.getX();
            double y = event.getY();

            double width = noteViewEditor.getWidth();
            double height = noteViewEditor.getHeight();

            double scrollSpeed = 25;

            // TODO: Scroll when approaching the edge of the screen
        });

        currentTool.addListener((observable, oldValue, newValue) -> {
            // if there is a tool toggle the corresponding button
            if (newValue.isPresent()) {
                EditorTool tool = newValue.get();
                if (tool instanceof PencilTool) {
                    tools.selectToggle(toggleToolPencil);
                } else if (tool instanceof SelectTool) {
                    tools.selectToggle(toggleToolSelect);
                } else if (tool instanceof SliceTool) {
                    tools.selectToggle(toggleToolSlice);
                }
            }
        });

        root.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("Z")) {
                context.getNoteService().undo();
            }

            if (event.isControlDown() && event.getCode().toString().equals("Y")) {
                context.getNoteService().redo();
            }

            if (event.isControlDown() && event.getCode().toString().equals("B")) {
                Collection<NoteEntry> selected = context.getNoteService().getSelection();

                // Find the lowest start and highest end to determine the length of the pattern to create
                int lowestStart = Integer.MAX_VALUE;
                int highestEnd = Integer.MIN_VALUE;
                for (NoteEntry entry : selected) {
                    lowestStart = Math.min(lowestStart, entry.get().getStartStep());
                    highestEnd = Math.max(highestEnd, entry.get().getEndStep());
                }

                // Create a new pattern with the same length as the selected pattern
                int length = highestEnd - lowestStart;
                Collection<NoteData> newNotes = new ArrayList<>();
                for (NoteEntry entry : selected) {
                    NoteData data = entry.get();

                    int newStart = data.getStartStep() + length;
                    int newEnd = data.getEndStep() + length;
                    NoteData newData = new NoteData(data.getPitch(), newStart, newEnd, data.getVelocity());
                    newNotes.add(newData);
                }

                context.getNoteService().create(newNotes);
            }

            if (event.isControlDown() && event.getCode().toString().equals("G")) {
                System.out.println("Grouping n notes: " + context.getNoteService().getSelection().size());
                Collection<NoteEntry> selected = context.getNoteService().getSelection();
                // create group
                NoteGroup group = new NoteGroup();
                for (NoteEntry entry : selected) {
                    group.add(entry);
                }
            }

        });

        // Initialize piano roll ---------------------------------------------------------------------------------------
        {
            pianoView = new KeysPane(context);
            noteViewEditor.heightProperty().addListener((observable, oldValue, newValue) -> {
                pianoView.setPrefHeight(newValue.doubleValue());
            });
            bodyBorderPane.setLeft(pianoView);
        }

        // Initialize tool bar buttons ---------------------------------------------------------------------------------
        {
            tools.getToggles().add(toggleToolSelect);
            tools.getToggles().add(toggleToolPencil);
            tools.getToggles().add(toggleToolSlice);

            toggleToolSelect.setOnAction(event -> noteViewEditor.setTool(
                    new SelectTool(noteViewEditor, noteViewEditor.getWorld(), context)));

            toggleToolPencil.setOnAction(event -> noteViewEditor.setTool(new PencilTool(noteViewEditor, context)));

            toggleToolSlice.setOnAction(event -> noteViewEditor.setTool(new SliceTool()));

        }

        // Ensure that the split pane fills the entire window ----------------------------------------------------------
        {
            AnchorPane pane = new AnchorPane();
            AnchorPane.setTopAnchor(bodyBorderPane, 0.0);
            AnchorPane.setBottomAnchor(bodyBorderPane, 0.0);
            AnchorPane.setLeftAnchor(bodyBorderPane, 0.0);
            AnchorPane.setRightAnchor(bodyBorderPane, 0.0);
            pane.getChildren().add(bodyBorderPane);
            splitPane = new SplitPane(pane, parametersPane);
            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.prefWidthProperty().bind(root.widthProperty());
            root.heightProperty().addListener((observable, oldValue, newValue) -> {
                float newHeight = (float) (newValue.floatValue() - toolBarRoot.getHeight());
                splitPane.setMinHeight(newHeight);
                splitPane.setMaxHeight(newHeight);
                splitPane.setPrefHeight(newHeight);
            });
            rootBorderPane.setCenter(splitPane);
        }

        context.getPlayback().play();

        // Force grid info to 100% (this triggers all the views to initialize)
        var oldGi = context.getViewSettings().getGridInfo();
        context.getViewSettings().gridInfoProperty().set(oldGi.withRows(0));
        context.getViewSettings().gridInfoProperty().set(oldGi);
    }

    private void initHorizontalScrollBar() {
        horizontalScrollBar = new HorizontalScrollBar();
        TimelineView timelineView = new TimelineView(context);
        timelineView.setMinHeight(15);
        timelineView.setMaxHeight(15);
        bodyBorderPane.setTop(timelineView);

        // When scrolled, move the note pattern editor and note parameter editor by the same amount as along the
        // width of the background surface as the percentage scrolled along the scroll bar
        horizontalScrollBar.setOnHandleScroll(scroll -> {
            var grid = context.getViewSettings().gridInfoProperty().get();
            double newTranslateX = scroll.getRelativePosition() * grid.getTotalWidth();
            if (grid.getTotalWidth() > noteViewEditor.getWidth()) {
                newTranslateX = MathUtil.map(newTranslateX, 0, grid.getTotalWidth(), 0,
                                             grid.getTotalWidth() - noteViewEditor.getWidth()
                );

                noteViewEditor.scrollToX(-newTranslateX);
                parametersPane.scrollX(-newTranslateX);
                timelineView.scrollX(-newTranslateX);
            }
        });

        // whenver grid info changes, update the scroll bar to have the same absolute position
        context.getViewSettings().gridInfoProperty().addListener(($0, oldGi, newGi) -> {
        });


        HBox bottomBox = new HBox();
        Rectangle pianoWidthSpacer = new Rectangle(125, 1);
        pianoWidthSpacer.setFill(Color.TRANSPARENT);
        bottomBox.getChildren().add(pianoWidthSpacer);
        bottomBox.getChildren().add(horizontalScrollBar);
        HBox.setHgrow(horizontalScrollBar, Priority.ALWAYS);
        bodyBorderPane.setBottom(bottomBox);
    }

    private void initVerticalScrollBar() {
        verticalScrollBar = new VerticalScrollBar();
        HBox rightBox = new HBox();
        Rectangle leftSpacer = new Rectangle(1, 0);
        leftSpacer.setFill(Color.TRANSPARENT);
        rightBox.getChildren().add(leftSpacer);

        // When scrolled, move the note pattern editor and piano roll by the same amount as along the height of the
        // background surface as the percentage scrolled along the scroll bar
        verticalScrollBar.setOnHandleScroll(scroll -> {
            double newTranslateY = scroll.getRelativePosition() * noteViewEditor.getBackgroundSurface().getHeight();
            newTranslateY = MathUtil.map(newTranslateY, 0, noteViewEditor.getBackgroundSurface().getHeight(), 0,
                                         noteViewEditor.getBackgroundSurface().getHeight() - noteViewEditor.getHeight()
            );
            noteViewEditor.scrollToY(-newTranslateY);
            pianoView.scrollY(-newTranslateY);
        });

        // whenver grid info changes, the scroll bar needs to move such that its at the same center position
        context.getViewSettings().gridInfoProperty().addListener(($0, oldGi, newGi) -> {

        });

        rightBox.getChildren().add(verticalScrollBar);

        bodyBorderPane.setRight(rightBox);
    }

    public void playlistPause(ActionEvent actionEvent) {
        context.getPlayback().pause();
    }

    public void playlistPlay(ActionEvent actionEvent) {
        context.getPlayback().play();
    }

    public void playlistStop(ActionEvent actionEvent) {
        context.getPlayback().stop();
    }
}
