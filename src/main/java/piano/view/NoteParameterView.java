package piano.view;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import piano.Util;
import piano.animation.AnimationState;
import piano.animation.InertialState;
import piano.animation.Interpolator;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteRegistry;

import java.util.concurrent.atomic.AtomicReference;

class NoteParameterView extends StackPane {
    private final Pane parent;
    private Circle circle;
    private Line dropLine;

    public NoteRegistry.Entry getNoteEntry() {
        return noteEntry;
    }

    private NoteRegistry.Entry noteEntry;
    private ObjectProperty<GridInfo> gridInfo;

    public NoteParameterView(Pane parent, NoteRegistry.Entry note, ObjectProperty<GridInfo> gridInfo) {
        super();

        this.parent = parent;
        this.noteEntry = note;
        this.gridInfo = gridInfo;

        circle = new Circle(10);
        circle.setFill(Color.GREEN.darker().darker().darker());
        circle.setStroke(Color.GRAY);
        dropLine = new Line();
        dropLine.setStroke(Color.GRAY);

        // when note changes, update the circle's position
        note.addListener((observable, oldValue, newValue) -> {
            reposition();
        });

        // when gridInfo changes, update the circle's position
        gridInfo.addListener((observable, oldValue, newValue) -> {
            reposition();
        });

        // when the parent's height changes, update the circle's position
        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            reposition();
        });

        reposition();

        // when the circle is dragged, update the note's velocity
        circle.setOnMouseDragged(event -> {

        });

        this.getChildren().add(circle);
        this.getChildren().add(dropLine);
    }

    private void reposition() {
        var note = this.noteEntry.get();

        double x = note.calculateX(gridInfo.get());
        circle.setTranslateX(x);

        double y = parent.getHeight() * (100 - note.getVelocity()) / 100;
        circle.setTranslateY(y);

        dropLine.setStartX(x);
        dropLine.setEndX(x);

        dropLine.setStartY(0);
        dropLine.setEndY(y);
    }
}
