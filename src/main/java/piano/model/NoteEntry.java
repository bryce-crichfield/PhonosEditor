package piano.model;

import javafx.beans.property.SimpleObjectProperty;

import java.util.function.Function;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    public NoteEntry(NoteData note) {
        super(note);
    }
}
