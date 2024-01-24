package piano.state.note;

import piano.state.note.command.*;
import piano.state.note.model.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class NoteService {
    private final NoteRegistry registry;
    private final NoteSelection noteSelection;
    private final NoteCommands noteCommands;
    private final Stack<NoteCommand> undoStack = new Stack<>();
    private final Stack<NoteCommand> redoStack = new Stack<>();

    public NoteService(NoteRegistry registry) {
        this.registry = registry;
        this.noteSelection = new NoteSelection();
        this.noteCommands = new NoteCommands(registry, noteSelection);
    }

    public void redo() {
        if (redoStack.isEmpty())
            return;

        NoteCommand action = redoStack.pop();
        action.execute(registry);
        undoStack.push(action);

        // Now uses command tree to handle redo
        noteCommands.redo();
    }

    public void undo() {
        if (undoStack.isEmpty())
            return;

        NoteCommand action = undoStack.pop();
        action.undo(registry);
        redoStack.push(action);

        // Now uses command tree to handle undo
        noteCommands.undo();
    }

    public void create(NoteData data) {
        execute(new CreateNoteCommand(data));
    }

    public void execute(NoteCommand action) {
        if (!action.execute(registry)) {
            return;
        }

        noteCommands.execute(action);
        undoStack.push(action);
        redoStack.clear();
    }

    public void create(Collection<NoteData> data) {
        Set<NoteCommand> commands = data.stream().map(CreateNoteCommand::new).collect(Collectors.toSet());
        GroupNoteCommand command = new GroupNoteCommand(commands);
        execute(command);
    }

    private void multicast(NoteEntry entry, Function<NoteEntry, Optional<NoteCommand>> factory) {
        // For either the selected notes, or the single note, create a command that modifies those notes and
        // all notes in their groups.

        Set<NoteCommand> commands = (noteSelection.isEmpty() ?
                Stream.of(entry) :
                noteSelection.stream()).flatMap(e -> e.getGroup().map(Collection::stream).orElse(Stream.of(e)))
                .distinct()
                .map(factory::apply)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (commands.isEmpty())
            return;
        execute(new GroupNoteCommand(commands));
    }

    // Performs a side-effecting operation the selected notes' entries, and all entries of notes in their groups.
    // Provided a function that takes any of those entries and returns a new NoteData
    public void modify(NoteEntry entry, Function<NoteEntry, Optional<NoteData>> update) {
        // This is strange, but we take a Function<NoteEntry, NoteData> instead of a Function<NoteData, NoteData>
        // because we want to be able to use the update function on any note entry the service decides to modify.
        // This is useful for modifying all notes in a group, for example.
        multicast(entry, e -> update.apply(e).map(d -> new ModifyNoteCommand(e, d)));
    }

    // Performs a side-effecting operation on the selected notes' entries, and all entries of notes in their groups.
    public void update(NoteEntry entry, Consumer<NoteEntry> update) {
        (noteSelection.isEmpty() ?
                Stream.of(entry) :
                noteSelection.stream()).flatMap(e -> e.getGroup().map(Collection::stream).orElse(Stream.of(e)))
                .distinct()
                .forEach(update);
    }

    public void delete(NoteEntry entry) {
        multicast(entry, e -> Optional.of(new DeleteNoteCommand(e)));
    }

    public NoteSelection getSelection() {
        return noteSelection;
    }

    public NoteRegistry getRegistry() {
        return registry;
    }
}
