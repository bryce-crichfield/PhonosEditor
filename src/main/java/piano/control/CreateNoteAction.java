package piano.control;

import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

import java.util.Optional;

public class CreateNoteAction implements NoteAction {
    private NoteData data;
    private Optional<NoteEntry> memo = Optional.empty();

    public CreateNoteAction(NoteData data) {
        this.data = data;
    }

    @Override
    public void execute(NoteRegistry registry) {
        undo(registry);
        NoteEntry entry = registry.register(data);
        memo = Optional.of(entry);
    }

    @Override
    public void undo(NoteRegistry registry) {
        memo.ifPresent(registry::unregister);
        memo = Optional.empty();
    }
}
