package piano.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

import java.util.*;
import java.util.function.Predicate;

public class BaseNoteService implements NoteService {
    private final NoteRegistry registry;
    private final Stack<NoteAction> undoStack = new Stack<>();
    private final Stack<NoteAction> redoStack = new Stack<>();
    private final ObservableList<NoteEntry> selectedEntries = FXCollections.observableArrayList();

    public BaseNoteService(NoteRegistry registry) {
        this.registry = registry;
    }

    public void execute(NoteAction action) {
        action.execute(registry);
        undoStack.push(action);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            NoteAction action = undoStack.pop();
            System.out.println("undo " + action.getClass().getSimpleName());
            action.undo(registry);
            redoStack.push(action);
        }
    }

    @Override
    public Collection<NoteEntry> query(Predicate<NoteEntry> predicate) {
        List<NoteEntry> result = new ArrayList<>();
        for (NoteEntry entry : registry.getEntries()) {
            if (predicate.test(entry)) {
                result.add(entry);
            }
        }
        return result;
    }

    @Override
    public void onCreate(NoteObserver observer) {
        registry.onAdded(entry -> observer.accept(entry, entry.get(), entry.get()));
    }

    @Override
    public void onModify(NoteObserver observer) {
        registry.onUpdated(observer);
    }

    @Override
    public void onDelete(NoteObserver observer) {
        registry.onRemoved(entry -> observer.accept(entry, entry.get(), entry.get()));
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            NoteAction action = redoStack.pop();
            action.execute(registry);

            undoStack.push(action);

        }
    }

    public void select(NoteEntry entry) {
        selectedEntries.add(entry);
    }

    @Override
    public void clearSelection() {
        selectedEntries.clear();
    }

    @Override
    public ObservableList<NoteEntry> getSelectedEntries() {
        return selectedEntries;
    }
}
