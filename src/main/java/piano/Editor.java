package piano;

import component.HorizontalScrollBar;
import component.ScrollBar;
import component.VerticalScrollBar;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.control.BaseNoteService;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;
import piano.playback.BasePlaybackService;
import piano.playback.PlaybackState;
import piano.tool.*;
import piano.view.midi.NoteMidiEditor;
import piano.view.parameter.NoteParameterEditor;
import piano.view.piano.NoteEditorPianoView;
import piano.view.settings.ViewSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class Editor {
    // FXML (Tool bar)
    public ToggleButton toggleToolPlayhead;
    public ToggleButton toggleToolSelect;
    public ToggleButton toggleToolPencil;
    public ToggleButton toggleToolSlice;
    public BorderPane bodyBorderPane;
    public AnchorPane root;
    public VBox toolBarRoot;
    public BorderPane rootBorderPane;
    public ComboBox<String> comboZoomLevel;
    // Non-FXML (Note editor)
    private SplitPane splitPane;
    private NoteMidiEditor noteMidiEditor;
    private NoteParameterEditor noteParameterEditor;
    private NoteEditorPianoView pianoView;
    private EditorContext context;
    private final ObjectProperty<Optional<EditorTool>> currentTool = new SimpleObjectProperty<>(Optional.empty());
    ToggleGroup tools = new ToggleGroup();


    public Editor() {
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
            context = new EditorContext(playbackService, noteService, viewSettings);

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
            verticalScrollBar.onScroll(scroll -> {
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
        VBox topBox = new VBox();
        // Initialize horizontal scroll bar ----------------------------------------------------------------------------
        {
            Rectangle topSpacer = new Rectangle(0, 1);
            topSpacer.setFill(Color.TRANSPARENT);
            topBox.getChildren().add(topSpacer);

            // When scrolled, move the note pattern editor and note parameter editor by the same amount as along the
            // width of the background surface as the percentage scrolled along the scroll bar
            horizontalScrollBar.onScroll(scroll -> {
                double newTranslateX = scroll.getRelativePosition() * noteMidiEditor.getBackgroundSurface().getWidth();
                newTranslateX = Util.map(newTranslateX, 0, noteMidiEditor.getBackgroundSurface().getWidth(), 0,
                                         noteMidiEditor.getBackgroundSurface().getWidth() - noteMidiEditor.getWidth()
                );
                noteMidiEditor.scrollToX(-newTranslateX);
                noteParameterEditor.scrollX(-newTranslateX);
            });
            topBox.getChildren().add(horizontalScrollBar);

            Rectangle bottomSpacer = new Rectangle(0, 2);
            bottomSpacer.setFill(Color.TRANSPARENT);
            topBox.getChildren().add(bottomSpacer);

            bodyBorderPane.setTop(topBox);
        }

        // Initialize scrolling ---------------------------------------------------------------------------------------
        noteMidiEditor.setOnScroll((EventHandler<? super ScrollEvent>) event -> {
            if (!event.isAltDown() && event.isControlDown()) {
                horizontalScrollBar.scrollBy(-event.getDeltaY());
            } else {
                verticalScrollBar.scrollBy(-event.getDeltaY());
            }
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
                } else if (tool instanceof PlayheadTool) {
                    tools.selectToggle(toggleToolPlayhead);
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
            tools.getToggles().add(toggleToolPlayhead);
            tools.getToggles().add(toggleToolSelect);
            tools.getToggles().add(toggleToolPencil);
            tools.getToggles().add(toggleToolSlice);

            toggleToolPlayhead.setOnAction(event -> noteMidiEditor.setTool(
                    new PlayheadTool()));

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

        // Add options to Zoom Level Combo Box -------------------------------------------------------------------------
        ChangeListener<String> comboZoomLevelListener = (observable, oldValue, newValue) -> {
            String zoomLevel = comboZoomLevel.getSelectionModel().getSelectedItem().toString();
            double percentage = Double.parseDouble(zoomLevel.replaceAll("[^0-9]", "")) / 100;
            double cellWidth = GridInfo.MAX_CELL_WIDTH * percentage;
            double cellHeight = GridInfo.MAX_CELL_HEIGHT * percentage;
            var gi = context.getViewSettings().gridInfoProperty().get();
            var newGi = gi.withCellWidth(cellWidth).withCellHeight(cellHeight);
            context.getViewSettings().setGridInfo(newGi);
            horizontalScrollBar.scrollBy(0);
            verticalScrollBar.scrollBy(0);
        };

        {
            comboZoomLevel.valueProperty().addListener(comboZoomLevelListener);
            comboZoomLevel.getItems().addAll("50%", "75%", "100%", "125%", "150%", "200%");
        }

        context.getPlayback().play();
        comboZoomLevel.setValue("100%");

        tools.selectToggle(toggleToolPlayhead);
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
