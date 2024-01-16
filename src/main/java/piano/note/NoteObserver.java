package piano.note;

import piano.note.model.*;

@FunctionalInterface
public interface NoteObserver {
    void accept(NoteEntry entry, NoteData oldData, NoteData newData);
}
