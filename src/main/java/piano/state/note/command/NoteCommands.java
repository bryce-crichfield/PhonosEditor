package piano.state.note.command;

import piano.state.note.model.*;

public class NoteCommands {
    private final NoteRegistry registry;
    private final NoteSelection noteSelection;


    public NoteCommands(NoteRegistry registry, NoteSelection noteSelection) {
        this.registry = registry;
        this.noteSelection = noteSelection;
    }

    public void undo() {

    }

    public void redo() {
    }

    public void execute(NoteCommand action) {
    }
}
