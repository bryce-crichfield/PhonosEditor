package piano.state.note.command;

import piano.state.note.model.NoteData;
import piano.state.note.model.NoteEntry;
import piano.state.note.model.NoteRegistry;

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
    public boolean execute(NoteRegistry registry) {
        undo(registry);

        for (NoteData data : this.data) {
            NoteEntry entry = registry.register(data);
            memo.add(entry);
        }

        return true;
    }

    @Override
    public void undo(NoteRegistry registry) {
        for (NoteEntry entry : memo) {
            registry.unregister(entry);
        }

        memo.clear();
    }
}
