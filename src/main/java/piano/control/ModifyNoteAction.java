package piano.control;

import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

import java.util.Optional;

public class ModifyNoteAction implements NoteAction {
    private NoteEntry entry;
    private Optional<NoteData> oldData = Optional.empty();
    private NoteData newData;

    public ModifyNoteAction(NoteEntry entry, NoteData newData) {
        this.entry = entry;
        this.newData = newData;
    }

    @Override
    public void execute(NoteRegistry registry) {
        undo(registry);
        oldData = Optional.of(entry.get());
        entry.set(newData);
    }

    @Override
    public void undo(NoteRegistry registry) {
        oldData.ifPresent(entry::set);
        oldData = Optional.empty();
    }
}
