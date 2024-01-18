package piano.note;

import piano.note.command.*;
import piano.note.model.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class NoteService {
    private final NoteRegistry registry;
    private final NoteSelection noteSelection;
    private final Stack<NoteCommand> undoStack = new Stack<>();
    private final Stack<NoteCommand> redoStack = new Stack<>();

    public NoteService(NoteRegistry registry) {
        this.registry = registry;
        this.noteSelection = new NoteSelection();
    }

    public void redo() {
        if (redoStack.isEmpty())
            return;

        NoteCommand action = redoStack.pop();
        action.execute(registry);
        undoStack.push(action);
    }

    public void undo() {
        if (undoStack.isEmpty())
            return;

        NoteCommand action = undoStack.pop();
        action.undo(registry);
        redoStack.push(action);
    }

    public void create(NoteData data) {
        execute(new CreateNoteCommand(data));
    }

    public void execute(NoteCommand action) {
        if (!action.execute(registry)) {
            return;
        }

        undoStack.push(action);
        redoStack.clear();
    }

    public void create(Collection<NoteData> data) {
        Set<NoteCommand> commands = data.stream().map(CreateNoteCommand::new).collect(Collectors.toSet());
        GroupNoteCommand command = new GroupNoteCommand(commands);
        execute(command);
    }

    public void modify(NoteEntry entry, Function<NoteEntry, NoteData> update) {
        // This is strange, but we take a Function<NoteEntry, NoteData> instead of a Function<NoteData, NoteData>
        // because we want to be able to use the update function on any note entry the service decides to modify.
        // This is useful for modifying all notes in a group, for example.
        multicast(entry, e -> new ModifyNoteCommand(e, update.apply(e)));
    }

    private void multicast(NoteEntry entry, Function<NoteEntry, NoteCommand> factory) {
        // For either the selected notes, or the single note, create a command that modifies those notes and
        // all notes in their groups.

        Set<NoteCommand> commands = (noteSelection.isEmpty() ? Stream.of(entry) : noteSelection.stream())
                .flatMap(e -> e.getGroup().map(group -> group.stream()).orElse(Stream.of(e)))
                .distinct()
                .map(e -> factory.apply(e))
                .collect(Collectors.toSet());
        execute(new GroupNoteCommand(commands));
    }

    public void delete(NoteEntry entry) {
        multicast(entry, DeleteNoteCommand::new);
    }

    public NoteSelection getSelection() {
        return noteSelection;
    }

    public NoteRegistry getRegistry() {
        return registry;
    }
}
