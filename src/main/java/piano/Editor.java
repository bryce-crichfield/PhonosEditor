package piano;

import component.ScrollBar;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import piano.control.BaseNoteService;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;
import piano.playback.BasePlaybackService;
import piano.playback.PlaybackState;
import piano.tool.PencilTool;
import piano.tool.SelectTool;
import piano.view.midi.NoteMidiEditor;
import piano.view.parameter.NoteParameterEditor;
import piano.view.piano.NoteEditorPianoView;
import piano.view.settings.ViewSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class Editor {
    // FXML (Tool bar)
    public ToggleButton toggleToolSelect;
    public ToggleButton toggleToolPencil;
    public BorderPane bodyBorderPane;
    public AnchorPane root;
    public VBox toolBarRoot;
    public BorderPane rootBorderPane;
    // Non-FXML (Note editor)
    private SplitPane splitPane;
    private NoteMidiEditor noteMidiEditor;
    private NoteParameterEditor noteParameterEditor;
    private NoteEditorPianoView pianoView;
    private EditorContext context;

    public Editor() {
        super();
    }

    public void initialize() {
        bodyBorderPane = new BorderPane();

        // Create the editor context -----------------------------------------------------------------------------------
        {
            var gridInfo = new GridInfo(88, 16 * 16, 32, 16);
            var viewSettings = new ViewSettings(gridInfo);

            var noteRegistry = new NoteRegistry();
            var playbackState = new PlaybackState(0, gridInfo.getColumns(), 120, false);

            var playbackService = new BasePlaybackService(new SimpleObjectProperty<>(playbackState));
            var noteService = new BaseNoteService(noteRegistry);
            context = new EditorContext(playbackService, noteService, viewSettings);
        }


        // Initialize note pattern and property editors ----------------------------------------------------------------
        {
            // The pattern editor represents the world as a large rectangle with a grid drawn on it.  The large
            // rectangle is the background surface, and the grid is drawn on top of it.
            noteMidiEditor = new NoteMidiEditor(context);
            bodyBorderPane.setCenter(noteMidiEditor);

            noteParameterEditor = new NoteParameterEditor(context);
            noteParameterEditor.setMinHeight(100);
            noteParameterEditor.setMaxHeight(500);
        }

        ScrollBar verticalScrollBar = new ScrollBar(Orientation.VERTICAL);
        // Initialize vertical scroll bar ------------------------------------------------------------------------------
        {

            // When scrolled, move the note pattern editor and piano roll by the same amount as along the height of the
            // background surface as the percentage scrolled along the scroll bar
            verticalScrollBar.setOnScroll((Consumer<Double>) percentage -> {
                double newTranslateY = percentage * noteMidiEditor.getBackgroundSurface().getHeight();
                newTranslateY = Util.map(newTranslateY, 0, noteMidiEditor.getBackgroundSurface().getHeight(), 0,
                                         noteMidiEditor.getBackgroundSurface().getHeight() - noteMidiEditor.getHeight()
                );
                noteMidiEditor.scrollToY(-newTranslateY);
                pianoView.scrollY(-newTranslateY);
            });

            bodyBorderPane.setRight(verticalScrollBar);
        }

        ScrollBar horizontalScrollBar = new ScrollBar(Orientation.HORIZONTAL);
        // Initialize horizontal scroll bar ----------------------------------------------------------------------------
        {

            // When scrolled, move the note pattern editor and note parameter editor by the same amount as along the
            // width of the background surface as the percentage scrolled along the scroll bar
            horizontalScrollBar.setOnScroll((Consumer<Double>) percentage -> {
                double newTranslateX = percentage * noteMidiEditor.getBackgroundSurface().getWidth();
                newTranslateX = Util.map(newTranslateX, 0, noteMidiEditor.getBackgroundSurface().getWidth(), 0,
                                         noteMidiEditor.getBackgroundSurface().getWidth() - noteMidiEditor.getWidth()
                );
                noteMidiEditor.scrollToX(-newTranslateX);
                noteParameterEditor.scrollX(-newTranslateX);
            });

            bodyBorderPane.setTop(horizontalScrollBar);
        }

        noteMidiEditor.setOnScroll((EventHandler<? super ScrollEvent>) event -> {
            if (event.isAltDown() && !event.isControlDown()) {
                scaleGrid(event.getDeltaX(), event.getDeltaY());
            } else if (!event.isAltDown() && event.isControlDown()) {
                horizontalScrollBar.scroll(-event.getDeltaY());
            } else {
                verticalScrollBar.scroll(-event.getDeltaY());
            }
        });

        noteMidiEditor.setOnMouseDragged(event -> {
            // if nearing the edge of the screen, scroll the screen
            double x = event.getX();
            double y = event.getY();

            double width = noteMidiEditor.getWidth();
            double height = noteMidiEditor.getHeight();

            double scrollSpeed = 25;

            if (x < scrollSpeed) {
                horizontalScrollBar.scroll(-scrollSpeed);
            } else if (x > width - scrollSpeed) {
                horizontalScrollBar.scroll(scrollSpeed);
            }

            if (y < scrollSpeed) {
                verticalScrollBar.scroll(-scrollSpeed);
            } else if (y > height - scrollSpeed) {
                verticalScrollBar.scroll(scrollSpeed);
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
                    NoteData newData = new NoteData(data.getNote(), newStart, newEnd, data.getVelocity());
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
            ToggleGroup tools = new ToggleGroup();
            tools.getToggles().add(toggleToolSelect);
            tools.getToggles().add(toggleToolPencil);
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
    }

    public void scaleGrid(double deltaX, double deltaY) {
        var gi = context.getViewSettings().gridInfoProperty().get();
        double newCellWidth = gi.getCellWidth() + deltaX;
        newCellWidth = Util.clamp(newCellWidth, 10, 48);
        double newCellHeight = gi.getCellHeight() + deltaY;
        newCellHeight = Util.clamp(newCellHeight, 10, 48);

        var newGi = gi.withCellWidth(newCellWidth).withCellHeight(newCellHeight);
        context.getViewSettings().setGridInfo(newGi);
    }

    public void changeToSelect(ActionEvent actionEvent) {
        noteMidiEditor.setTool(new SelectTool(noteMidiEditor, noteMidiEditor.getWorld(), context));
    }

    public void changeToPencil(ActionEvent actionEvent) {
        noteMidiEditor.setTool(new PencilTool(noteMidiEditor, context));
    }

    public void scaleUpX() {
        scaleGrid(1, 0);
    }

    public void scaleDownX() {
        scaleGrid(-1, 0);
    }

    public void scaleUpY() {
        scaleGrid(0, 1);
    }

    public void scaleDownY() {
        scaleGrid(0, -1);
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
