package piano.note.command;

import piano.note.model.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class GroupNoteCommand implements NoteCommand {
    private final Set<NoteCommand> actions;

    public GroupNoteCommand(Set<NoteCommand> actions) {
        this.actions = actions;
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

    public static GroupNoteCommand fromFactory(NoteGroup group, Function<NoteEntry, NoteCommand> factory
    ) {
        Set<NoteCommand> actions = group.stream().map(factory).collect(Collectors.toSet());
        return new GroupNoteCommand(actions);
    }
}
