package piano.note.model;


import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.SimpleListProperty;

import java.util.LinkedList;

public class NoteSelection extends SimpleListProperty<NoteEntry> {
    public NoteSelection() {
        super(new ObservableListWrapper<>(new LinkedList<>()));
    }
}
