package piano.note.command;

import piano.note.model.*;

import java.util.*;

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
