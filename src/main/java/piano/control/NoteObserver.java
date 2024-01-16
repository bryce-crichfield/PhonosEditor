package piano.control;

import piano.model.note.NoteData;
import piano.model.note.NoteEntry;

@FunctionalInterface
public interface NoteObserver {
    void accept(NoteEntry entry, NoteData oldData, NoteData newData);
}
