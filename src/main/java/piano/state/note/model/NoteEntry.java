package piano.state.note.model;

import javafx.beans.property.*;
import piano.state.note.*;

import java.util.*;
import java.util.function.*;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    private final NoteService service;

    private double unsnappedX;
    private final BooleanProperty highlightedProperty = new SimpleBooleanProperty(false);

    public BooleanProperty highlightedProperty() {
        return highlightedProperty;
    }

    public double getUnsnappedX() {
        return unsnappedX;
    }

    public void setUnsnappedX(double unsnappedX) {
        this.unsnappedX = unsnappedX;
    }

    private Optional<NoteGroup> group = Optional.empty();

    public NoteEntry(NoteData note, NoteService service) {
        super(note);
        this.service = service;
    }

    public Optional<NoteGroup> getGroup() {
        return group;
    }

    public void setGroup(NoteGroup group) {
        this.group = Optional.ofNullable(group);
    }

    public void foreach(Consumer<NoteEntry> callback) {
        service.foreach(this, callback);
    }

    public void map(Function<NoteEntry, Optional<NoteData>> callback) {
        service.modify(this, callback);
    }
}
