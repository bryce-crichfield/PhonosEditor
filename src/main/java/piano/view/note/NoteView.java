package piano.view.note;

import javafx.scene.layout.*;
import lombok.*;
import piano.state.note.model.*;

@Data
public class NoteView extends StackPane {
    private final NoteEntry noteEntry;
}
