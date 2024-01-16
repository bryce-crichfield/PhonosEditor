package piano.model;

import javafx.collections.FXCollections;
import piano.control.NoteObserver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class NoteRegistry {
    private final List<NoteEntry> notes;
    private final List<Consumer<NoteEntry>> onAddedCallbacks;
    private final List<Consumer<NoteEntry>> onRemovedCallbacks;
    private final List<NoteObserver> onUpdatedCallbacks;

    public NoteRegistry() {
        notes = new LinkedList<>();
        onAddedCallbacks = FXCollections.observableArrayList();
        onRemovedCallbacks = FXCollections.observableArrayList();
        onUpdatedCallbacks = FXCollections.observableArrayList();
    }

    public NoteEntry register(NoteData note) {
        var entry = new NoteEntry(note);
        notes.add(entry);
        onAddedCallbacks.forEach(callback -> callback.accept(entry));
        entry.addListener((observable, oldValue, newValue) -> {
            if (notes.contains(entry)) {
                onUpdatedCallbacks.forEach(callback -> callback.accept(entry, oldValue, newValue));
            }
        });

        return entry;
    }

    public void unregister(NoteEntry note) {
        boolean isContained = notes.contains(note);
        if (isContained) {
            onRemovedCallbacks.forEach(callback -> callback.accept(note));
        }
        notes.remove(note);
    }

    public void onAdded(Consumer<NoteEntry> callback) {
        onAddedCallbacks.add(callback);
    }

    public void onUpdated(NoteObserver observer) {
        onUpdatedCallbacks.add(observer);
    }

    public void onRemoved(Consumer<NoteEntry> callback) {
        onRemovedCallbacks.add(callback);
    }

    public Collection<NoteEntry> getEntries() {
        return notes;
    }
}
