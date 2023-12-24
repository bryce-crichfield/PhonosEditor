package piano;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
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
import piano.model.GridInfo;
import piano.model.NoteRegistry;
import piano.tool.PencilTool;
import piano.tool.SelectTool;
import piano.view.NoteMidiEditor;
import piano.view.NoteParameterEditor;
import piano.view.PianoRoll;
import piano.view.ScrollBar;

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
    private double zoomVelocity = 0;

    public NoteEditorController() {
        super();
    }

    public void initialize() {
        bodyBorderPane = new BorderPane();

        // Initialize note pattern and property editors ----------------------------------------------------------------
        {
            var gi = new GridInfo(88, 16 * 16, 32, 16);
            gridInfo = new SimpleObjectProperty<>(gi);

            // The pattern editor represents the world as a large rectangle with a grid drawn on it.  The large
            // rectangle is the background surface, and the grid is drawn on top of it.
            noteMidiEditor = new NoteMidiEditor(gridInfo, new NoteRegistry());
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
            } else  {
                verticalScrollBar.scroll(-event.getDeltaY());
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
            toggleToolSelect.setSelected(true);
            toggleToolSelect.setOnAction(this::changeToSelect);
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
    }

    public void changeToSelect(ActionEvent actionEvent) {
        noteMidiEditor.setTool(new SelectTool());
    }

    public void changeToPencil(ActionEvent actionEvent) {
        noteMidiEditor.setTool(new PencilTool(noteMidiEditor));
    }

    public void scaleGrid(double deltaX, double deltaY) {
        var gi = gridInfo.get();
        double newCellWidth = gi.getCellWidth() + deltaX;
        newCellWidth =Util.clamp(newCellWidth, 10, 48);
        double newCellHeight = gi.getCellHeight() + deltaY;
        newCellHeight = Util.clamp(newCellHeight, 10, 48);

        gridInfo.set(gi.withCellWidth(newCellWidth).withCellHeight(newCellHeight));
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
}
