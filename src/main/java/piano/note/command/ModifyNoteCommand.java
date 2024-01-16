package piano.note.command;

import piano.note.model.NoteData;
import piano.note.model.NoteEntry;
import piano.note.model.NoteRegistry;

import java.util.Optional;

public class ModifyNoteCommand implements NoteCommand {
    private final NoteEntry entry;
    private final NoteData newData;
    private Optional<NoteData> oldData = Optional.empty();

    public ModifyNoteCommand(NoteEntry entry, NoteData newData) {
        this.entry = entry;
        this.newData = newData;
    }

    @Override
    public boolean execute(NoteRegistry registry) {
        if (newData.equals(entry.get())) {
            return false;
        }

        oldData = Optional.of(entry.get());
        entry.set(newData);
        return true;
    }

    @Override
    public void undo(NoteRegistry registry) {
        System.out.println("undo modify");
        oldData.ifPresent(old -> {
            entry.set(old);
        });
        oldData = Optional.empty();
    }
}
