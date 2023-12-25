package piano.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

import java.util.*;
import java.util.function.Predicate;

public class MemoNoteController implements NoteController {
    private static Optional<MemoNoteController> instance = Optional.empty();
    private final NoteRegistry registry;
    private final Stack<NoteAction> undoStack = new Stack<>();
    private final Stack<NoteAction> redoStack = new Stack<>();
    private final ObservableList<NoteEntry> selectedEntries = FXCollections.observableArrayList();

    public MemoNoteController(NoteRegistry registry) {
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

    public static MemoNoteController createInstance(NoteRegistry registry) {
        instance = Optional.of(new MemoNoteController(registry));
        return instance.orElseThrow();
    }

    public static MemoNoteController getInstance() {
        return instance.orElseThrow();
    }
}
