package piano.control.command;

import piano.model.note.NoteData;
import piano.model.note.NoteEntry;
import piano.model.note.NoteRegistry;

import java.util.Optional;

public class DeleteNoteCommand implements NoteCommand {
    private NoteEntry entry;
    private Optional<NoteData> memo = Optional.empty();

    public DeleteNoteCommand(NoteEntry entry) {
        this.entry = entry;
    }

    @Override
    public void execute(NoteRegistry registry) {
        undo(registry);
        registry.unregister(entry);
        memo = Optional.of(entry.get());
    }

    @Override
    public void undo(NoteRegistry registry) {
        memo.ifPresent(noteData -> entry = registry.register(noteData));
        memo = Optional.empty();
    }
}
