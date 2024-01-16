package piano.note;

import piano.note.model.NoteData;
import piano.note.model.NoteEntry;

@FunctionalInterface
public interface NoteObserver {
    void accept(NoteEntry entry, NoteData oldData, NoteData newData);
}
