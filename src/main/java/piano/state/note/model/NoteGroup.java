package piano.state.note.model;

import java.util.*;

public class NoteGroup extends HashSet<NoteEntry> {
    public NoteGroup() {
        super();
    }

    @Override
    public boolean add(NoteEntry note) {
        // If the note is already in a group, remove it from that group and add it to this group
        note.getGroup().ifPresent(group -> group.remove(note));
        note.setGroup(this);
        return super.add(note);
    }

    @Override
    public boolean remove(Object note) {
        if (note instanceof NoteEntry) {
            ((NoteEntry) note).setGroup(null);
        }

        return super.remove(note);
    }
}
