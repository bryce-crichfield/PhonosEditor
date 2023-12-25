package piano.control;

import piano.model.NoteRegistry;

public interface NoteAction {
    void execute(NoteRegistry registry);
    void undo(NoteRegistry registry);
}
