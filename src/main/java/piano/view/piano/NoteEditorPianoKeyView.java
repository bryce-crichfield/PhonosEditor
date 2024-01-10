package piano.view.piano;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.Util;
import piano.model.NotePitch;
import piano.playback.PlaybackService;

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

        // Reverse the order of the indices bottom to top like a piano instead of top to bottom like a grid
        int finalIndex = (int) Util.reverse(index, 0, 87);
        String note = NotePitch.indexToString(finalIndex + 1);

        // remove any numbers from the note string
        String noteLetter = note.replaceAll("[0-9]", "");
        switch (noteLetter) {
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
            setTranslateY(finalIndex * newHeight);
        });

        String finalNote = note;
        setOnMouseClicked(event -> {
            PlaybackService playback = context.getPlayback();
            playback.triggerNote(finalNote);
        });

    }
}
