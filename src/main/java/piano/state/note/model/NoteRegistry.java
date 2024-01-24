package piano.state.note.model;

import javafx.collections.*;
import piano.state.note.*;

import java.util.*;

public class NoteRegistry {
    private final Set<NoteEntry> notes;
    private final List<NoteObserver> onCreatedObservers;
    private final List<NoteObserver> onDeletedObservers;
    private final List<NoteObserver> onModifiedObservers;

    public NoteRegistry() {
        notes = new HashSet<>();

        onCreatedObservers = FXCollections.observableArrayList();
        onDeletedObservers = FXCollections.observableArrayList();
        onModifiedObservers = FXCollections.observableArrayList();
    }

    public NoteEntry register(NoteData note) {
        var entry = new NoteEntry(note);
        notes.add(entry);
        onCreatedObservers.forEach(callback -> callback.accept(entry, null, entry.get()));
        entry.addListener((observable, oldValue, newValue) -> {
            if (notes.contains(entry)) {
                onModifiedObservers.forEach(callback -> callback.accept(entry, oldValue, newValue));
            }
        });

        return entry;
    }

    public void unregister(NoteEntry note) {
        boolean isContained = notes.contains(note);
        if (isContained) {
            onDeletedObservers.forEach(callback -> callback.accept(note, note.get(), null));
        }
        notes.remove(note);
    }

    public void onCreatedListener(NoteObserver observer) {
        onCreatedObservers.add(observer);
    }

    public void onModifiedListener(NoteObserver observer) {
        onModifiedObservers.add(observer);
    }

    public void onDeletedListener(NoteObserver observer) {
        onDeletedObservers.add(observer);
    }

    public Collection<NoteEntry> getEntries() {
        return notes;
    }
}
