package piano.note;

import piano.note.command.*;
import piano.note.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoteService {
    private final NoteRegistry registry;
    private final NoteSelection noteSelection;
    private final Stack<NoteCommand> undoStack = new Stack<>();
    private final Stack<NoteCommand> redoStack = new Stack<>();

    public NoteService(NoteRegistry registry) {
        this.registry = registry;
        this.noteSelection = new NoteSelection();
    }

    public void execute(NoteCommand action) {
        if (!action.execute(registry)) {
            return;
        }

        undoStack.push(action);
        redoStack.clear();
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

    public void create(Collection<NoteData> data) {
        Set<NoteCommand> commands = data.stream().map(CreateNoteCommand::new).collect(Collectors.toSet());
        GroupNoteCommand command = new GroupNoteCommand(commands);
        execute(command);
    }

    public void modify(NoteEntry entry, Function<NoteData, NoteData> update) {
         multicast(entry, e -> new ModifyNoteCommand(e, update.apply(e.get())));
    }

    public void delete(NoteEntry entry) {
        multicast(entry, DeleteNoteCommand::new);
    }

    private void multicast(NoteEntry entry, Function<NoteEntry, NoteCommand> factory) {
        Set<NoteCommand> commands = (noteSelection.isEmpty() ? Stream.of(entry) : noteSelection.stream())
                .flatMap(e -> e.getGroup().map(group -> group.stream()).orElse(Stream.of(e)))
                .distinct()
                .map(e -> factory.apply(e))
                .collect(Collectors.toSet());

        GroupNoteCommand command = new GroupNoteCommand(commands);
        execute(command);
    }

    public NoteSelection getSelection() {
        return noteSelection;
    }

    public NoteRegistry getRegistry() {
        return registry;
    }
}
