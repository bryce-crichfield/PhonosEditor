package encoding.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import config.Theme;

import java.io.IOException;
import java.lang.reflect.Field;

public class ReflectionSerializer<T> extends StdSerializer<T> {
    private final Class<T> clazz;

    public ReflectionSerializer(Class<T> clazz) {
        super(clazz);
        this.clazz = clazz;
    }

    @Override
    public void serialize(T instance, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // Use reflection to get the fields of the Theme class.  Foreach declared field, write the field name and
        // value to the json object.
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                jsonGenerator.writeFieldName(field.getName());
                jsonGenerator.writeObject(field.get(clazz));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
