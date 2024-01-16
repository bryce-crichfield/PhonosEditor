package piano.control.command;

import piano.model.note.NoteRegistry;

public interface NoteCommand {
    void execute(NoteRegistry registry);
    void undo(NoteRegistry registry);
}
