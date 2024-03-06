package piano.state.note;

import piano.state.note.model.NoteData;
import piano.state.note.model.NoteEntry;

@FunctionalInterface
public interface NoteObserver {
    void accept(NoteEntry entry, NoteData oldData, NoteData newData);
}
