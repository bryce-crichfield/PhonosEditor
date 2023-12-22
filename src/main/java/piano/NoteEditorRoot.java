package piano;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import piano.animation.AnimationState;
import piano.animation.Interpolator;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static piano.Util.map;

public class NoteEditorRoot {
    // FXML
    public AnchorPane editorRoot;
    public ToggleButton toggleToolSelect;
    public ToggleButton toggleToolPencil;
    public BorderPane bodyBorderPane;
    public AnchorPane root;
    public VBox toolBarRoot;
    public AnchorPane notePropsEditor;
    public SplitPane bodyRoot;
    // Non-FXML
    private NoteEditor noteEditor;
    private PianoRoll pianoRoll;
    private ObjectProperty<GridInfo> gridInfo;

    public NoteEditorRoot() {
        super();
    }

    public void initialize() {

        // Initialize note editor
        var gi = new GridInfo(88, 16 * 16, 32, 16);
        gridInfo = new SimpleObjectProperty<>(gi);
        noteEditor = new NoteEditor(editorRoot, gridInfo);

        // Initialize vertical scroll bar
        ScrollBar verticalScrollBar = new ScrollBar(Orientation.VERTICAL);
        verticalScrollBar.setOnScroll((Consumer<Double>) percentage -> {
            double newTranslateY = percentage * noteEditor.getBackground().getHeight();
            newTranslateY = Util.map(newTranslateY, 0, noteEditor.getBackground().getHeight(), 0, noteEditor.getBackground().getHeight() - editorRoot.getHeight());
            noteEditor.scrollY(-newTranslateY);
            pianoRoll.scroll(-newTranslateY);
        });
        bodyBorderPane.setRight(verticalScrollBar);

        // Initialize horizontal scroll bar
        ScrollBar horizontalScrollBar = new ScrollBar(Orientation.HORIZONTAL);
        horizontalScrollBar.setOnScroll((Consumer<Double>) percentage -> {
            double newTranslateX = percentage * noteEditor.getBackground().getWidth();
            newTranslateX = Util.map(newTranslateX, 0, noteEditor.getBackground().getWidth(), 0, noteEditor.getBackground().getWidth() - editorRoot.getWidth());
            noteEditor.scrollX(-newTranslateX);
        });
//        horizontalScrollBar.widthProperty().addListener(new Util.DebugListener("horizontalScrollBar.heightProperty()"));
        bodyBorderPane.setTop(horizontalScrollBar);

        // Initialize piano roll
        pianoRoll = new PianoRoll(noteEditor.getBackground().getHeight(), gridInfo);
        editorRoot.heightProperty().addListener((observable, oldValue, newValue) -> {
            pianoRoll.setPrefHeight(newValue.doubleValue());
        });
        bodyBorderPane.setLeft(pianoRoll);

        // Initialize tool bar buttons
        ToggleGroup tools = new ToggleGroup();
        tools.getToggles().add(toggleToolSelect);
        tools.getToggles().add(toggleToolPencil);
        toggleToolSelect.setSelected(true);
        toggleToolSelect.setOnAction(this::changeToSelect);

        // Ensure that the body border pane is always the same size as thie root
        bodyBorderPane.prefWidthProperty().bind(root.widthProperty());
        notePropsEditor.setStyle("-fx-background-color: #ff0000;");
        root.heightProperty().addListener((observable, oldValue, newValue) -> {
            float newHeight = (float) (newValue.floatValue() - toolBarRoot.getHeight());
            // NOTE: We have to force the height of the body border pane to be the same as the root to prevent
            // the scroll bars from appearing outside of the root
            bodyRoot.setMinHeight(newHeight);
            bodyRoot.setMaxHeight(newHeight);
            bodyRoot.setPrefHeight(newHeight);
        });
    }

    public void changeToSelect(ActionEvent actionEvent) {
        noteEditor.setTool(new SelectTool());
    }

    public void changeToPencil(ActionEvent actionEvent) {
        noteEditor.setTool(new PencilTool(noteEditor));
    }

    AnimationState cellWidthAnimationState = new AnimationState(32, 32);
    AnimationState cellHeightAnimationState = new AnimationState(16, 16);
    public void scaleUpX(ActionEvent actionEvent) {
        var gi = gridInfo.get();

        cellWidthAnimationState.modifyTarget((target, current) -> target + 1);
        float cellWidth = (float) cellWidthAnimationState.getTarget();
        float cellHeight = (float) cellHeightAnimationState.getTarget();

        var newGridInfo = new GridInfo(gi.getColumns(), gi.getRows(), cellWidth, cellHeight);
        gridInfo.set(newGridInfo);
    }

    public void scaleDownX(ActionEvent actionEvent) {
        var gi = gridInfo.get();

        cellWidthAnimationState.modifyTarget((target, current) -> target - 1);
        float cellWidth = (float) cellWidthAnimationState.getTarget();
        float cellHeight = (float) cellHeightAnimationState.getTarget();

        var newGridInfo = new GridInfo(gi.getColumns(), gi.getRows(), cellWidth, cellHeight);
        gridInfo.set(newGridInfo);
    }

    public void scaleUpY(ActionEvent actionEvent) {
        var gi = gridInfo.get();

        cellHeightAnimationState.modifyTarget((target, current) -> target + 1);
        float cellWidth = (float) cellWidthAnimationState.getTarget();
        float cellHeight = (float) cellHeightAnimationState.getTarget();

        var newGridInfo = new GridInfo(gi.getColumns(), gi.getRows(), cellWidth, cellHeight);
        gridInfo.set(newGridInfo);
    }

    public void scaleDownY(ActionEvent actionEvent) {
        var gi = gridInfo.get();

        cellHeightAnimationState.modifyTarget((target, current) -> target - 1);
        float cellWidth = (float) cellWidthAnimationState.getTarget();
        float cellHeight = (float) cellHeightAnimationState.getTarget();

        var newGridInfo = new GridInfo(gi.getColumns(), gi.getRows(), cellWidth, cellHeight);
        gridInfo.set(newGridInfo);
    }
}
