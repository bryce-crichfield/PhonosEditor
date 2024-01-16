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

    public Collection<NoteEntry> query(Predicate<NoteEntry> predicate) {
        List<NoteEntry> result = new ArrayList<>();
        for (NoteEntry entry : registry.getEntries()) {
            if (predicate.test(entry)) {
                result.add(entry);
            }
        }
        return result;
    }

    public void create(NoteData data) {
        execute(new CreateNoteCommand(data));
    }

    public void execute(NoteCommand action) {
        action.execute(registry);
        undoStack.push(action);
        redoStack.clear();
    }

    public void create(Collection<NoteData> data) {
        Set<NoteCommand> commands = data.stream().map(CreateNoteCommand::new).collect(Collectors.toSet());
        GroupNoteCommand command = new GroupNoteCommand(commands);
        execute(command);
    }

    public void modify(NoteEntry entry, Function<NoteData, NoteData> update) {
         (noteSelection.isEmpty() ? Stream.of(entry) : noteSelection.stream())
                .flatMap(e -> e.getGroup().map(group -> group.stream()).orElse(Stream.of(e)))
                .distinct()
                .map(e -> new ModifyNoteCommand(e, update.apply(e.get())))
                .collect(Collectors.toSet())
                .forEach(this::execute);
    }

    public void delete(NoteEntry entry) {
        execute(new DeleteNoteCommand(entry));
    }

    public NoteSelection getSelection() {
        return noteSelection;
    }

    public NoteRegistry getRegistry() {
        return registry;
    }
}
