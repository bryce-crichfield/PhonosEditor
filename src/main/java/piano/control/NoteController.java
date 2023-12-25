package piano.control;

import javafx.collections.ObservableList;
import piano.model.NoteData;
import piano.model.NoteEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface NoteController {
    default void create(NoteData data) {
        execute(new CreateNoteAction(data));
    }
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

    default void delete(NoteEntry entry) {
        execute(new DeleteNoteAction(entry));
    }

    void execute(NoteAction action);
    void undo();
    void select(NoteEntry entry);
    void clearSelection();

    ObservableList<NoteEntry> getSelectedEntries();

    Collection<NoteEntry> query(Predicate<NoteEntry> predicate);
}
