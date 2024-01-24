package piano.state.note.command;

import piano.state.note.model.*;

import java.util.*;

public class DeleteNoteCommand implements NoteCommand {
    private NoteEntry entry;
    private Optional<NoteData> memo = Optional.empty();

    public DeleteNoteCommand(NoteEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean execute(NoteRegistry registry) {
        undo(registry);
        registry.unregister(entry);
        memo = Optional.of(entry.get());

        return true;
    }

    @Override
    public void undo(NoteRegistry registry) {
        memo.ifPresent(noteData -> entry = registry.register(noteData));
        memo = Optional.empty();
    }
}
