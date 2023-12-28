package piano.control;

import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.model.NoteRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateNoteAction implements NoteAction {
    private final List<NoteData> data = new ArrayList<>();
    private final List<NoteEntry> memo = new ArrayList<>();

    public CreateNoteAction(NoteData data) {
        this.data.add(data);
    }

    public CreateNoteAction(Collection<NoteData> data) {
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
