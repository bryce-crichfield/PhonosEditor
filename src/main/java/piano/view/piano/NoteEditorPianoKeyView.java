package piano.view.piano;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import piano.EditorContext;
import piano.Util;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NotePitch;
import piano.playback.PlaybackService;



public class NoteEditorPianoKeyView extends StackPane {
    private String noteString;

    public class KeyBody extends Rectangle {
        private Color defaultFill;

        public KeyBody(int index, EditorContext context) {
            super();

            // Reverse the order of the indices bottom to top like a piano instead of top to bottom like a grid
            int finalIndex = (int) Util.reverse(index, 0, 87);
            String note = NotePitch.indexToString(finalIndex + 1);
            noteString = note;
            defaultFill = getDefaultFill(note);
            setFill(defaultFill);

            setOnMouseEntered(event -> setFill(getHoverFill()));
            setOnMouseExited(event -> setFill(defaultFill));
            setOnMouseClicked(event -> context.getPlayback().triggerNote(note));
        }

        private Color getDefaultFill(String note) {
            String noteLetter = note.replaceAll("[0-9]", "");
            switch (noteLetter) {
                case "C", "D", "E", "F", "G", "A", "B" -> {
                    return Color.WHITE;
                }
                case "C#", "D#", "F#", "G#", "A#" -> {
                    return Color.BLACK;
                }
            }

            return Color.WHITE;
        }

        private Color getHoverFill() {
            Color fill = defaultFill;
            if (fill == Color.WHITE) {
                fill = Color.DARKGRAY;
            } else {
                fill = Color.DARKGRAY.darker().darker().darker();
            }
            return fill;
        }

        public double getDefaultWidth() {
            return 125;
        }
    }

    public NoteEditorPianoKeyView(int index, EditorContext context) {
        super();
        Label label = new Label();
        label.setAlignment(Pos.BASELINE_RIGHT);
        KeyBody keyBody = new KeyBody(index, context);
        label.setText(noteString);
        bindPaneToRectangle(keyBody, label);
        getChildren().add(keyBody);
        getChildren().add(label);

        // When the grid changes, we need to update the view
        context.getViewSettings().gridInfoProperty().addListener((observable1, oldValue1, newValue1) -> {
            GridInfo grid = newValue1;
            double y = index * grid.getCellHeight();
            double height = grid.getCellHeight();

            keyBody.setX(0);
            keyBody.setY(y);
            keyBody.setWidth(keyBody.getDefaultWidth());
            keyBody.setHeight(height);

            // The font height should be 80% of the cell height
            double cellHeight = context.getViewSettings().gridInfoProperty().get().getCellHeight();
            label.setFont(label.getFont().font(cellHeight * 0.35));
        });
    }

    private void bindPaneToRectangle(Rectangle rectangle, Label label) {
        // Whenever the rectangle's position or size changes, update the pane's position or size, as well as the label's position and text
        rectangle.xProperty().addListener((observable, oldValue, newValue) -> {
            label.setLayoutX(newValue.doubleValue());
            this.setLayoutX(newValue.doubleValue());
        });

        rectangle.yProperty().addListener((observable, oldValue, newValue) -> {
            label.setLayoutY(newValue.doubleValue() + rectangle.getHeight() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setLayoutY(newValue.doubleValue());
            label.setText(noteString);
        });

        rectangle.widthProperty().addListener((observable, oldValue, newValue) -> {
            label.setPrefWidth(newValue.doubleValue());
            label.setText(noteString);
        });
    }
}
