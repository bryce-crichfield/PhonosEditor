package piano.control;

import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

import java.util.Optional;

public class DeleteNoteAction implements NoteAction {
    private NoteEntry entry;
    private Optional<NoteData> memo = Optional.empty();

    public DeleteNoteAction(NoteEntry entry) {
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
