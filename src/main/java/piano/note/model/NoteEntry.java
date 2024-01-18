package piano.note.model;

import javafx.beans.property.*;

import java.util.*;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    private double unsnappedX;

    public double getUnsnappedX() {
        return unsnappedX;
    }

    public void setUnsnappedX(double unsnappedX) {
        this.unsnappedX = unsnappedX;
    }

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
