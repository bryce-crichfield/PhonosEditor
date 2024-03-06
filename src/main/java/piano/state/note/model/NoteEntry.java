package piano.state.note.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import piano.state.note.NoteService;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    private final NoteService service;
    private final BooleanProperty highlightedProperty = new SimpleBooleanProperty(false);
    private double unsnappedX;
    private Optional<NoteGroup> group = Optional.empty();

    public NoteEntry(NoteData note, NoteService service) {
        super(note);
        this.service = service;
    }

    public BooleanProperty highlightedProperty() {
        return highlightedProperty;
    }

    public double getUnsnappedX() {
        return unsnappedX;
    }

    public void setUnsnappedX(double unsnappedX) {
        this.unsnappedX = unsnappedX;
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
