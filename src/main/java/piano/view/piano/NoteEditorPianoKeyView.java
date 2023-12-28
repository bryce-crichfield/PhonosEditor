package piano.view.piano;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.model.NoteData;

public class NoteEditorPianoKeyView extends Rectangle {
    private Color defaultFill;

    public NoteEditorPianoKeyView(int index, EditorContext context) {
        super();
        var gi = context.getViewSettings().getGridInfo();
        setWidth(125);
        setHeight(gi.getCellHeight());
        setFill(Color.WHITE);
        setStroke(Color.BLACK);
        setStrokeWidth(1);

        setTranslateY(index * gi.getCellHeight());

        String note = NoteData.noteToString(index);
        note = note.substring(0, note.length() - 1);
        switch (note) {
            case "C", "D", "E", "F", "G", "A", "B" -> defaultFill = Color.WHITE;
            case "C#", "D#", "F#", "G#", "A#" -> {
                defaultFill = Color.BLACK;
            }
        }

        setFill(defaultFill);

        setOnMouseEntered(event -> {
            Color fill = defaultFill;
            if (fill == Color.WHITE) {
                fill = Color.DARKGRAY;
            } else {
                fill = Color.DARKGRAY.darker().darker().darker();
            }
            setFill(fill);
        });

        setOnMouseExited(event -> {
            setFill(defaultFill);
        });

        context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
            var newHeight = newValue.getCellHeight();
            setHeight(newHeight);
            setTranslateY(index * newHeight);
        });

    }
}
