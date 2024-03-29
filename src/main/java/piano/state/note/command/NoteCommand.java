package piano.state.note.command;

import piano.state.note.model.NoteRegistry;

public interface NoteCommand {
    // @return true if the command changed the registry
    //        false if the command did not change the registry
    boolean execute(NoteRegistry registry);

    void undo(NoteRegistry registry);
}
