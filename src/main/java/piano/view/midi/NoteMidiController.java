package piano.view.midi;

import javafx.scene.input.MouseEvent;

interface NoteMidiController {
    default void stackPaneOnMouseDragged(MouseEvent event) {
        event.consume();
    }

    default void rectangleOnMouseMoved(MouseEvent event) {
        event.consume();
    }




}
