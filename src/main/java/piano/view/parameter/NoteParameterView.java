package piano.view.parameter;

import javafx.collections.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import piano.*;
import piano.state.note.*;
import piano.state.note.model.*;

import java.util.*;
import java.util.concurrent.atomic.*;

class NoteParameterView extends Rectangle {
    private final MidiEditorContext context;
    private final Pane parent;
    private final NoteEntry noteEntry;
    private Color currentColor;

    public NoteParameterView(Pane parent, NoteEntry note, MidiEditorContext context) {
        super();
        this.context = context;
        this.parent = parent;
        this.noteEntry = note;
        calculateColor();


        // when note changes, update the circle's position
        note.addListener((observable, oldValue, newValue) -> {
            recalculateViewFromModel();
        });

        // when gridInfo changes, update the circle's position
        context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
            recalculateViewFromModel();
        });

        // when the parent's height changes, update the circle's position
        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            recalculateViewFromModel();
        });

        recalculateViewFromModel();

        this.setOnMouseMoved(event -> {
            // Determine which handle the mouse is over
            // if we are in the top 25% of the rectangle, we are in the top handle
            // otherwise, we are in the body
            double y = event.getY();
            double height = this.getHeight();
            // Change the cursor to indicate which handle we are over
            this.getScene().setCursor(javafx.scene.Cursor.N_RESIZE);
        });

        this.setOnMouseEntered(event -> {
            this.setStrokeWidth(1);

            context.getNoteService().getSelection().add(noteEntry);
        });

        this.setOnMouseExited(event -> {
            this.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            this.setStrokeWidth(2);
            context.getNoteService().getSelection().remove(noteEntry);
        });


        // I don't really know why we have to track the start position of the mouse drag, but it works.
        AtomicReference<Double> startY = new AtomicReference<>((double) 0);
        this.setOnMousePressed(event -> {
            startY.set(event.getY());
        });

        this.setOnMouseDragged(event -> {
            // The mouse drag is relative to the start position of the mouse drag, so we need to calculate the
            // difference between the current mouse position and the start position of the mouse drag to get the
            // delta.
            double dy = event.getY() - startY.get();
            double ty = this.getTranslateY() + dy;
            double velocity = Util.clamp(1 - (ty / parent.getHeight()), 0, 1);

            context.getNoteService().modify(noteEntry, entry -> {
                NoteData newNoteData = entry.get().withVelocity((int) (velocity * 100));
                return Optional.of(newNoteData);
            });
        });

        // Sheet Metal gradient

        this.setStrokeWidth(2);
        this.setStroke(Color.BLACK);

        noteEntry.highlightedProperty().addListener(($0, $1, highlighted) -> {
            if (highlighted) {
                this.setStroke(Color.CYAN.desaturate());
            } else {
                this.setStroke(Color.BLACK);
            }
        });
    }

    private void recalculateViewFromModel() {
        var note = this.noteEntry.get();
        var gridInfo = context.getViewSettings().getGridInfo();

        double width = 10;

        double x = note.calcXPosOnGrid(gridInfo) - width / 2;
        double y = (1 - note.getVelocityAsPercentage()) * parent.getHeight();

        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setWidth(width);
        this.setHeight(parent.getHeight());

        calculateColor();
    }

    public void calculateColor() {
        double velocity = noteEntry.get().getVelocityAsPercentage();
        velocity = Util.Easing.easeInCubic(velocity);
        double hue = Util.map(velocity, 0, 1, 0, 130);
        hue = Util.reverse(hue, 0, 130);
        currentColor = Color.hsb(hue, 0.85, 0.65);
        setFill(currentColor);
    }

    public NoteEntry getNoteEntry() {
        return noteEntry;
    }
}
