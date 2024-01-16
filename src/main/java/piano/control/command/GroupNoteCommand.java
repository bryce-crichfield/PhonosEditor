package piano.control.command;

import piano.model.note.NoteEntry;
import piano.model.note.NoteGroup;
import piano.model.note.NoteRegistry;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupNoteCommand implements NoteCommand {
    private final Set<NoteCommand> actions;

    public GroupNoteCommand(Set<NoteCommand> actions) {
        this.actions = actions;
    }

    public static GroupNoteCommand fromFactory(NoteGroup group, Function<NoteEntry, NoteCommand> factory) {
        Set<NoteCommand> actions = group.stream().map(factory).collect(Collectors.toSet());
        return new GroupNoteCommand(actions);
    }

    @Override
    public void execute(NoteRegistry registry) {
        for (NoteCommand action : actions) {
            action.execute(registry);
        }
    }

    @Override
    public void undo(NoteRegistry registry) {
        for (NoteCommand action : actions) {
            action.undo(registry);
        }
    }
}
