package config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Optional;

// Uses reflection to set all ObjectProperty<T> fields of a class instance to the deserialized value of the corresponding
// json object fields. This is useful for deserializing complex objects that contain ObjectProperty<T> fields.
public final class ReflectionObjectPropertyDeserialier<T> extends JsonDeserializer<T> {
    private final Class<T> clazz;
    private final T instance;

    public ReflectionObjectPropertyDeserialier(Class<T> clazz, T instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        Field[] fields = clazz.getDeclaredFields();

        try {
            for (Field field : fields) {
                // [ Preconditions ]
                var modifiers = field.getModifiers();
                boolean hasCorrectModifiers = Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);
                if (!hasCorrectModifiers) {
                    continue;
                }

                var fieldName = field.getName();
                boolean isObjectProperty = field.getType().isAssignableFrom(ObjectProperty.class);
                if (!isObjectProperty) {
                    System.out.println("[ReflectionDeserializer] Cannot assign field: " + fieldName + " to ObjectProperty");
                    continue;
                }

                var type = field.getGenericType();
                boolean isParameterizedType = type instanceof ParameterizedType;
                if (!isParameterizedType) {
                    System.out.println("[ReflectionDeserializer] Field: " + fieldName + " does not have a parameterized type.");
                    continue;
                }

                var parameterizedType = (ParameterizedType) type;
                var typeArguments = parameterizedType.getActualTypeArguments();
                boolean hasOneTypeArgument = typeArguments.length == 1;
                if (!hasOneTypeArgument) {
                    System.out.println("[ReflectionDeserializer] Field: " + fieldName + " does not have exactly one type argument, and therefore cannot be assigned to ObjectProperty<T>.");
                    continue;
                }

                // [ Deserialize the JsonNode and set the value of the ObjectProperty<T> instance ]
                Class<?> fieldClazz = Class.forName(typeArguments[0].getTypeName());
                JsonNode fieldNode = node.get(fieldName);
                Optional<Object> objectOpt = Optional.ofNullable(codec.treeToValue(fieldNode, fieldClazz));
                if (objectOpt.isPresent()) {
                    var objectProperty = (ObjectProperty<T>) field.get(instance);
                    objectProperty.set((T) objectOpt.get());
                }
            }
        } catch (IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return instance;
    }

}
