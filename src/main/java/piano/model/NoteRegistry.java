package piano.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.function.Consumer;
import java.util.function.Function;

public class NoteRegistry {
    public static class Entry extends SimpleObjectProperty<NoteData> {
        public Entry(NoteData note) {
            super(note);
        }

        public void modify(Function<NoteData, NoteData> modifier) {
            set(modifier.apply(get()));
        }
    }

    private final ObservableList<Entry> notes;

    public NoteRegistry() {
        notes = FXCollections.observableArrayList();
    }

    public Entry register(NoteData note) {
        var entry = new Entry(note);
        notes.add(entry);
        return entry;
    }

    public void unregister(Entry note) {
        notes.remove(note);
    }
    public void onAdded(Consumer<Entry> callback) {
        notes.addListener((ListChangeListener<Entry>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Entry entry : change.getAddedSubList()) {
                        callback.accept(entry);
                    }
                }
            }
        });
    }

    public void onRemoved(Consumer<Entry> callback) {
        notes.addListener((ListChangeListener<Entry>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Entry entry : change.getRemoved()) {
                        callback.accept(entry);
                    }
                }
            }
        });
    }
}
