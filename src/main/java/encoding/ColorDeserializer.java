package encoding;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import javafx.scene.paint.*;

import java.io.*;

public class ColorDeserializer extends JsonDeserializer<Color> {

    @Override
    public Color deserialize(JsonParser jsonParser, DeserializationContext deserializationContext
    ) throws IOException, JacksonException {
        String hex = jsonParser.getText();
        return Color.web(hex);
    }
}
