package piano.state.note;

import piano.state.note.model.*;

@FunctionalInterface
public interface NoteObserver {
    void accept(NoteEntry entry, NoteData oldData, NoteData newData);
}
