package piano.control;

import javafx.collections.ObservableList;
import piano.model.NoteData;
import piano.model.NoteEntry;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;


// The service is used by the ViewController to modify the model.
// It also provides for editor features such as undo/redo, selection, query, and clipboard.
public interface NoteService {
    default void create(NoteData data) {
        execute(new CreateNoteAction(data));
    }

    void execute(NoteAction action);

    default void createMany(Collection<NoteData> data) {
        System.out.println("createMany");
        execute(new CreateNoteAction(data));
    }

    default void modify(NoteEntry entry, Function<NoteData, NoteData> update) {
        if (!getSelectedEntries().isEmpty()) {
            for (NoteEntry selectedEntry : getSelectedEntries()) {
                NoteData data = selectedEntry.get();
                execute(new ModifyNoteAction(selectedEntry, update.apply(data)));
            }
        } else {
            execute(new ModifyNoteAction(entry, update.apply(entry.get())));
        }
    }

    ObservableList<NoteEntry> getSelectedEntries();

    default void delete(NoteEntry entry) {
        execute(new DeleteNoteAction(entry));
    }

    void undo();

    void redo();

    void select(NoteEntry entry);

    void clearSelection();

    Collection<NoteEntry> query(Predicate<NoteEntry> predicate);

    void onCreate(NoteObserver observer);

    void onModify(NoteObserver observer);

    void onDelete(NoteObserver observer);
}
