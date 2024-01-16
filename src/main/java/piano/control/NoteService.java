package piano.control;

import javafx.collections.ObservableList;
import piano.control.command.*;
import piano.model.note.NoteData;
import piano.model.note.NoteEntry;
import piano.model.note.NoteGroup;
import piano.model.note.command.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


// The service is used by the ViewController to modify the model.
// It also provides for editor features such as undo/redo, selection, query, and clipboard.
public interface NoteService {
    default void create(NoteData data) {
        execute(new CreateNoteCommand(data));
    }

    void execute(NoteCommand action);

    default void createMany(Collection<NoteData> data) {
        System.out.println("createMany");
        execute(new CreateNoteCommand(data));
    }

    default void modify(NoteEntry entry, Function<NoteData, NoteData> update) {
        // Two cases:
        // 1. If there are selected entries, then modify all of them.
        // 2. If there are no selected entries, then modify the entry.

        // In either case, for each entry, we need to check if it is part of a group.
        // If it is, then we need to modify the entire group, otherwise we can just modify the entry.
        Set<NoteGroup> modifiedGroups = new HashSet<>();
        Consumer<NoteEntry> modifyGroup =  e -> {
            Optional<NoteGroup> group = e.getGroup();
            if (group.isPresent() && !modifiedGroups.contains(group)) {
                modifiedGroups.add(group.get());
                var command = GroupNoteCommand.fromFactory(group.get(), e2 -> new ModifyNoteCommand(e2, update.apply(e2.get())));
                execute(command);
            } else {
                execute(new ModifyNoteCommand(e, update.apply(e.get())));
            }
        };

        if (!getSelectedEntries().isEmpty()) {
            for (NoteEntry selectedEntry : getSelectedEntries()) {
                modifyGroup.accept(selectedEntry);
            }
        } else {
            modifyGroup.accept(entry);
        }
    }

    ObservableList<NoteEntry> getSelectedEntries();

    default void delete(NoteEntry entry) {
        execute(new DeleteNoteCommand(entry));
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
