package piano.note.model;


import com.sun.javafx.collections.*;
import javafx.beans.property.*;

import java.util.*;

public class NoteSelection extends SimpleListProperty<NoteEntry> {
    public NoteSelection() {
        super(new ObservableListWrapper<>(new LinkedList<>()));
    }
}
