package piano.note.command;

import piano.note.model.NoteRegistry;

public interface NoteCommand {
    void execute(NoteRegistry registry);
    void undo(NoteRegistry registry);
}
