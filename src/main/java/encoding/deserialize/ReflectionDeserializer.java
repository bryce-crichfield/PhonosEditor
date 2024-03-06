package encoding.deserialize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import config.Theme;
import javafx.scene.paint.Color;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class ReflectionDeserializer<T> extends JsonDeserializer<T> {
    private final Class<T> clazz;

    public ReflectionDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        // Use reflection get the fields of the Theme class.  The json object should have all them and if not,
        // default values of (Color.White) will be used.  Foreach declared field, get the value from the json object
        // and set it to the field.  Return the new Theme object.
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        Field[] fields = clazz.getDeclaredFields();

        try {
            T instance = clazz.getConstructor().newInstance();

            for (Field field : fields) {
                int modifiers = field.getModifiers();
                boolean hasCorrectModifiers = Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);
                if (!hasCorrectModifiers) {
                    continue;
                }

                String fieldName = field.getName();
                Class<?> fieldClazz = field.getType();
                JsonNode fieldNode = node.get(fieldName);
                System.out.println("Class: " + fieldClazz.getName() + " Field: " + fieldName + " Node: " + fieldNode);
                Optional<Object> objectOpt = Optional.ofNullable(codec.treeToValue(fieldNode, fieldClazz));
                if (objectOpt.isEmpty()) continue;

                field.setAccessible(true);
                field.set(instance, objectOpt.get());
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

}
