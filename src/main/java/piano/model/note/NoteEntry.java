package piano.model.note;

import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    private Optional<NoteGroup> group = Optional.empty();
    public NoteEntry(NoteData note) {
        super(note);
    }

    public Optional<NoteGroup> getGroup() {
        return group;
    }

    public void setGroup(NoteGroup group) {
        this.group = Optional.ofNullable(group);
    }
}
