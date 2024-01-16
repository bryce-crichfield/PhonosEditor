package piano.control.command;

import piano.model.note.NoteData;
import piano.model.note.NoteEntry;
import piano.model.note.NoteRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateNoteCommand implements NoteCommand {
    private final List<NoteData> data = new ArrayList<>();
    private final List<NoteEntry> memo = new ArrayList<>();

    public CreateNoteCommand(NoteData data) {
        this.data.add(data);
    }

    public CreateNoteCommand(Collection<NoteData> data) {
        this.data.addAll(data);
    }

    @Override
    public void execute(NoteRegistry registry) {
        undo(registry);

        for (NoteData data : this.data) {
            NoteEntry entry = registry.register(data);
            memo.add(entry);
        }
    }

    @Override
    public void undo(NoteRegistry registry) {
        for (NoteEntry entry : memo) {
            registry.unregister(entry);
        }

        memo.clear();
    }
}
