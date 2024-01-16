package piano;

import component.HorizontalScrollBar;
import component.ScrollBar;
import component.VerticalScrollBar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.control.BaseNoteService;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;
import piano.playback.BasePlaybackService;
import piano.playback.PlaybackState;
import piano.tool.EditorTool;
import piano.tool.PencilTool;
import piano.tool.SelectTool;
import piano.tool.SliceTool;
import piano.view.midi.NoteMidiEditor;
import piano.view.parameter.NoteParameterEditor;
import piano.view.piano.NoteEditorPianoView;
import piano.view.playlist.TimelineView;
import piano.view.settings.ViewSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class MidiEditor {
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
    private NoteMidiEditor noteMidiEditor;
    private NoteParameterEditor noteParameterEditor;
    private NoteEditorPianoView pianoView;
    private MidiEditorContext context;


    public MidiEditor() {
        super();
    }

    public void initialize() {
        bodyBorderPane = new BorderPane();

        // Create the editor context -----------------------------------------------------------------------------------
        {
            var gridInfo = new GridInfo(88, 16 * 16, 32, 16);
            var viewSettings = new ViewSettings(gridInfo, true);

            var noteRegistry = new NoteRegistry();
            var playbackState = new PlaybackState(0, 4, 120, false);

            var playbackService = new BasePlaybackService(new SimpleObjectProperty<>(playbackState));
            var noteService = new BaseNoteService(noteRegistry);
            context = new MidiEditorContext(playbackService, noteService, viewSettings);

            playbackService.observe((String noteName) -> {
                System.out.println("Triggering note: " + noteName);
            });
        }


        // Initialize note pattern and property editors ----------------------------------------------------------------
        {
            // The pattern editor represents the world as a large rectangle with a grid drawn on it.  The large
            // rectangle is the background surface, and the grid is drawn on top of it.
            noteMidiEditor = new NoteMidiEditor(context, currentTool);
            bodyBorderPane.setCenter(noteMidiEditor);

            noteParameterEditor = new NoteParameterEditor(context);
            noteParameterEditor.setMinHeight(100);
            noteParameterEditor.setMaxHeight(500);
        }

        ScrollBar verticalScrollBar = new VerticalScrollBar();
        HBox rightBox = new HBox();
        // Initialize vertical scroll bar ------------------------------------------------------------------------------
        {
            Rectangle leftSpacer = new Rectangle(1, 0);
            leftSpacer.setFill(Color.TRANSPARENT);
            rightBox.getChildren().add(leftSpacer);

            // When scrolled, move the note pattern editor and piano roll by the same amount as along the height of the
            // background surface as the percentage scrolled along the scroll bar
            verticalScrollBar.setOnHandleScroll(scroll -> {
                double newTranslateY = scroll.getRelativePosition() * noteMidiEditor.getBackgroundSurface().getHeight();
                newTranslateY = Util.map(newTranslateY, 0, noteMidiEditor.getBackgroundSurface().getHeight(), 0,
                                         noteMidiEditor.getBackgroundSurface().getHeight() - noteMidiEditor.getHeight()
                );
                noteMidiEditor.scrollToY(-newTranslateY);
                pianoView.scrollY(-newTranslateY);
            });

            rightBox.getChildren().add(verticalScrollBar);

            bodyBorderPane.setRight(rightBox);
        }

        ScrollBar horizontalScrollBar = new HorizontalScrollBar();
        // Initialize horizontal scroll bar ----------------------------------------------------------------------------
        {

            TimelineView timelineView = new TimelineView(context);
            timelineView.setMinHeight(15);
            timelineView.setMaxHeight(15);
            bodyBorderPane.setTop(timelineView);

            // When scrolled, move the note pattern editor and note parameter editor by the same amount as along the
            // width of the background surface as the percentage scrolled along the scroll bar
            horizontalScrollBar.setOnHandleScroll(scroll -> {
                double newTranslateX = scroll.getRelativePosition() * noteMidiEditor.getBackgroundSurface().getWidth();
                newTranslateX = Util.map(newTranslateX, 0, noteMidiEditor.getBackgroundSurface().getWidth(), 0,
                                         noteMidiEditor.getBackgroundSurface().getWidth() - noteMidiEditor.getWidth()
                );
                noteMidiEditor.scrollToX(-newTranslateX);
                noteParameterEditor.scrollX(-newTranslateX);
                timelineView.scrollX(-newTranslateX);

            });

            HBox bottomBox = new HBox();
            Rectangle pianoWidthSpacer = new Rectangle(125, 1);
            pianoWidthSpacer.setFill(Color.TRANSPARENT);
            bottomBox.getChildren().add(pianoWidthSpacer);
            bottomBox.getChildren().add(horizontalScrollBar);
            HBox.setHgrow(horizontalScrollBar, Priority.ALWAYS);
            bodyBorderPane.setBottom(bottomBox);
        }

        // Initialize scrolling ---------------------------------------------------------------------------------------
        noteMidiEditor.setOnScroll((EventHandler<? super ScrollEvent>) event -> {
            // None  -> Vertical scroll
            // Shift -> Horizontal scroll
            // Ctrl -> Zoom Both (Vertical and Horizontal)
            // Ctrl + Shift -> Zoom Horizontal
            // Ctrl + Alt -> Zoom Vertical

            // Sometimes the delta is x, and sometimes it is y, so we need to check both, and just use whichever one is
            // not 0
            final double delta = event.getDeltaX() == 0 ? event.getDeltaY() : event.getDeltaX();
            Runnable zoomVertical = () -> {
                double percentage = delta / 1000;
                double newCellHeight = context.getViewSettings().getGridInfo().getCellHeight() * (1 + percentage);
                var gi = context.getViewSettings().getGridInfo();
                var newGi = gi.withCellHeight(newCellHeight);
                context.getViewSettings().setGridInfo(newGi);
            };

            Runnable zoomHorizontal = () -> {
                double percentage = delta / 1000;
                double newCellWidth = context.getViewSettings().getGridInfo().getCellWidth() * (1 + percentage);
                var gi = context.getViewSettings().getGridInfo();
                var newGi = gi.withCellWidth(newCellWidth);
                context.getViewSettings().setGridInfo(newGi);
            };


            if (event.isControlDown() && event.isShiftDown()) {
                zoomHorizontal.run();
                return;
            }

            if (event.isControlDown() && event.isAltDown()) {
                zoomVertical.run();
                return;
            }

            if (event.isControlDown()) {
                zoomVertical.run();
                zoomHorizontal.run();
                return;
            }

            if (event.isShiftDown()) {
                horizontalScrollBar.scrollBy(-event.getDeltaY());
                return;
            }

            verticalScrollBar.scrollBy(-event.getDeltaY());
        });

        // When dragging the mouse, scroll the screen if the mouse is near the edge of the screen ----------------------
        noteMidiEditor.setOnMouseDragged(event -> {
            // if nearing the edge of the screen, scroll the screen
            double x = event.getX();
            double y = event.getY();

            double width = noteMidiEditor.getWidth();
            double height = noteMidiEditor.getHeight();

            double scrollSpeed = 25;

            if (x < scrollSpeed) {
                horizontalScrollBar.scrollBy(-scrollSpeed);
            } else if (x > width - scrollSpeed) {
                horizontalScrollBar.scrollBy(scrollSpeed);
            }

            if (y < scrollSpeed) {
                verticalScrollBar.scrollBy(-scrollSpeed);
            } else if (y > height - scrollSpeed) {
                verticalScrollBar.scrollBy(scrollSpeed);
            }
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
                context.getNotes().undo();
            }

            if (event.isControlDown() && event.getCode().toString().equals("Y")) {
                context.getNotes().redo();
            }

            if (event.isControlDown() && event.getCode().toString().equals("B")) {
                Collection<NoteEntry> selected = context.getNotes().getSelectedEntries();

                // Find the lowest start and highest end to determine the length of the pattern to create
                int lowestStart = Integer.MAX_VALUE;
                int highestEnd = Integer.MIN_VALUE;
                for (NoteEntry entry : selected) {
                    lowestStart = Math.min(lowestStart, entry.get().getStart());
                    highestEnd = Math.max(highestEnd, entry.get().getEnd());
                }

                // Create a new pattern with the same length as the selected pattern
                int length = highestEnd - lowestStart;
                Collection<NoteData> newNotes = new ArrayList<>();
                for (NoteEntry entry : selected) {
                    NoteData data = entry.get();

                    int newStart = data.getStart() + length;
                    int newEnd = data.getEnd() + length;
                    NoteData newData = new NoteData(data.getPitch(), newStart, newEnd, data.getVelocity());
                    newNotes.add(newData);
                }

                context.getNotes().createMany(newNotes);
            }
        });

        // Initialize piano roll ---------------------------------------------------------------------------------------
        {
            pianoView = new NoteEditorPianoView(context);
            noteMidiEditor.heightProperty().addListener((observable, oldValue, newValue) -> {
                pianoView.setPrefHeight(newValue.doubleValue());
            });
            bodyBorderPane.setLeft(pianoView);
        }

        // Initialize tool bar buttons ---------------------------------------------------------------------------------
        {
            tools.getToggles().add(toggleToolSelect);
            tools.getToggles().add(toggleToolPencil);
            tools.getToggles().add(toggleToolSlice);

            toggleToolSelect.setOnAction(event -> noteMidiEditor.setTool(
                    new SelectTool(noteMidiEditor, noteMidiEditor.getWorld(), context)));

            toggleToolPencil.setOnAction(event -> noteMidiEditor.setTool(
                    new PencilTool(noteMidiEditor, context)));

            toggleToolSlice.setOnAction(event -> noteMidiEditor.setTool(
                    new SliceTool()));

        }

        // Ensure that the split pane fills the entire window ----------------------------------------------------------
        {
            splitPane = new SplitPane(Util.createAnchorPane(bodyBorderPane), noteParameterEditor);
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
