package piano.note.command;

import piano.note.model.NoteData;
import piano.note.model.NoteEntry;
import piano.note.model.NoteRegistry;

import java.util.Optional;

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
