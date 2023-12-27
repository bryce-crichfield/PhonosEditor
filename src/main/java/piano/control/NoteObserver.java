package piano.control;

import piano.model.NoteData;
import piano.model.NoteEntry;

@FunctionalInterface
public interface NoteObserver {
    void accept(NoteEntry entry, NoteData oldData, NoteData newData);
}
