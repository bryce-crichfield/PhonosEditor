package piano.model;

import javafx.beans.property.SimpleObjectProperty;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    public NoteEntry(NoteData note) {
        super(note);
    }
}
