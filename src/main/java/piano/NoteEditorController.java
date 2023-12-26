package piano;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;
import piano.playback.PlaybackService;
import piano.playback.PlaybackState;
import piano.tool.PencilTool;
import piano.tool.SelectTool;
import piano.view.NoteMidiEditor;
import piano.view.NoteParameterEditor;
import piano.view.PianoRoll;
import piano.view.ScrollBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class NoteEditorController {
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
    private PianoRoll pianoRoll;
    private ObjectProperty<GridInfo> gridInfo;
    private final double zoomVelocity = 0;
    private PlaybackService playbackService;

    public NoteEditorController() {
        super();
    }

    public void initialize() {
        bodyBorderPane = new BorderPane();


        // Initialize note pattern and property editors ----------------------------------------------------------------
        {
            var gi = new GridInfo(88, 16 * 16, 32, 16);
            gridInfo = new SimpleObjectProperty<>(gi);
            // Playback Service
            PlaybackState state = new PlaybackState(0, gridInfo.get().getColumns(), 120, false);
            playbackService = new PlaybackService(new SimpleObjectProperty<>(state));
            // The pattern editor represents the world as a large rectangle with a grid drawn on it.  The large
            // rectangle is the background surface, and the grid is drawn on top of it.
            noteMidiEditor = new NoteMidiEditor(gridInfo, new NoteRegistry(), playbackService);
            bodyBorderPane.setCenter(noteMidiEditor);

            noteParameterEditor = new NoteParameterEditor(gridInfo, noteMidiEditor.getNoteRegistry());
            noteParameterEditor.setMinHeight(100);
            noteParameterEditor.setMaxHeight(500);
        }

        piano.view.ScrollBar verticalScrollBar = new piano.view.ScrollBar(Orientation.VERTICAL);
        // Initialize vertical scroll bar ------------------------------------------------------------------------------
        {

            // When scrolled, move the note pattern editor and piano roll by the same amount as along the height of the
            // background surface as the percentage scrolled along the scroll bar
            verticalScrollBar.setOnScroll((Consumer<Double>) percentage -> {
                double newTranslateY = percentage * noteMidiEditor.getBackgroundSurface().getHeight();
                newTranslateY = Util.map(newTranslateY, 0, noteMidiEditor.getBackgroundSurface().getHeight(), 0,
                                         noteMidiEditor.getBackgroundSurface().getHeight() - noteMidiEditor.getHeight()
                );
                noteMidiEditor.scrollY(-newTranslateY);
                pianoRoll.scrollY(-newTranslateY);
            });


            bodyBorderPane.setRight(verticalScrollBar);
        }

        piano.view.ScrollBar horizontalScrollBar = new ScrollBar(Orientation.HORIZONTAL);
        // Initialize horizontal scroll bar ----------------------------------------------------------------------------
        {

            // When scrolled, move the note pattern editor and note parameter editor by the same amount as along the
            // width of the background surface as the percentage scrolled along the scroll bar
            horizontalScrollBar.setOnScroll((Consumer<Double>) percentage -> {
                double newTranslateX = percentage * noteMidiEditor.getBackgroundSurface().getWidth();
                newTranslateX = Util.map(newTranslateX, 0, noteMidiEditor.getBackgroundSurface().getWidth(), 0,
                                         noteMidiEditor.getBackgroundSurface().getWidth() - noteMidiEditor.getWidth()
                );
                noteMidiEditor.scrollX(-newTranslateX);
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

        root.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("Z")) {
                noteMidiEditor.getController().undo();
            }

            if (event.isControlDown() && event.getCode().toString().equals("Y")) {
                noteMidiEditor.getController().redo();
            }

            if (event.isControlDown() && event.getCode().toString().equals("B")) {
                Collection<NoteEntry> selected = noteMidiEditor.getController().getSelectedEntries();

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

                noteMidiEditor.getController().createMany(newNotes);
            }
        });

        // Initialize piano roll ---------------------------------------------------------------------------------------
        {
            pianoRoll = new PianoRoll(noteMidiEditor.getBackgroundSurface().getHeight(), gridInfo);
            noteMidiEditor.heightProperty().addListener((observable, oldValue, newValue) -> {
                pianoRoll.setPrefHeight(newValue.doubleValue());
            });
            bodyBorderPane.setLeft(pianoRoll);
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


        playbackService.play();
    }

    public void scaleGrid(double deltaX, double deltaY) {
        var gi = gridInfo.get();
        double newCellWidth = gi.getCellWidth() + deltaX;
        newCellWidth = Util.clamp(newCellWidth, 10, 48);
        double newCellHeight = gi.getCellHeight() + deltaY;
        newCellHeight = Util.clamp(newCellHeight, 10, 48);

        gridInfo.set(gi.withCellWidth(newCellWidth).withCellHeight(newCellHeight));
    }

    public void changeToSelect(ActionEvent actionEvent) {
        noteMidiEditor.setTool(new SelectTool(noteMidiEditor, noteMidiEditor.getWorld()));
    }

    public void changeToPencil(ActionEvent actionEvent) {
        noteMidiEditor.setTool(new PencilTool(noteMidiEditor));
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
        playbackService.pause();
    }

    public void playlistPlay(ActionEvent actionEvent) {
        playbackService.play();
    }

    public void playlistStop(ActionEvent actionEvent) {
        playbackService.stop();
    }
}
