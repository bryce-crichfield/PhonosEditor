package piano.note.command;

import piano.note.model.NoteEntry;
import piano.note.model.NoteGroup;
import piano.note.model.NoteRegistry;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
