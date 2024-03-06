package piano.state.note.command;

import piano.state.note.model.NoteEntry;
import piano.state.note.model.NoteGroup;
import piano.state.note.model.NoteRegistry;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupNoteCommand implements NoteCommand {
    private final Set<NoteCommand> actions;

    public GroupNoteCommand(Set<NoteCommand> actions) {
        this.actions = actions;
    }

    public static GroupNoteCommand fromFactory(NoteGroup group, Function<NoteEntry, NoteCommand> factory
    ) {
        Set<NoteCommand> actions = group.stream().map(factory).collect(Collectors.toSet());
        return new GroupNoteCommand(actions);
    }

    @Override
    public boolean execute(NoteRegistry registry) {
        return actions.stream().map(action -> action.execute(registry)).reduce(Boolean::logicalOr).get();
    }

    @Override
    public void undo(NoteRegistry registry) {
        for (NoteCommand action : actions) {
            action.undo(registry);
        }
    }
}
