package piano.view.note;

import javafx.scene.layout.StackPane;
import lombok.Data;
import piano.state.note.model.NoteEntry;

@Data
public class NoteView extends StackPane {
    private final NoteEntry noteEntry;
}
