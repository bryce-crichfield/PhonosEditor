package config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import encoding.deserialize.ReflectionDeserializer;
import encoding.serialize.ReflectionSerializer;
import util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

// Loads a json file into a class instance
// By default, the json file is expected to be in the resources/config directory
public final class ConfigFactory<T extends Config> {
    private final Path absolutePath;
    private final Class<T> clazz;
    private final SimpleModule module = new SimpleModule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Consumer<T>> initializers = new ArrayList<>();

    // @param clazz: The class type of the config object to be loaded
    // @param relativePath: The relative path to the json file that contains the config object
    public ConfigFactory(Class<T> clazz, String relativePath) {
        this(clazz, relativePath, new ReflectionSerializer<>(clazz), new ReflectionDeserializer<>(clazz));
    }
    private ConfigFactory(Class<T> clazz, String relativePath, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        this.clazz = clazz;
        Path rootPath = Paths.get("src/main/resources/config");
        absolutePath = rootPath.resolve(relativePath);
        module.addSerializer(serializer);
        module.addDeserializer(clazz, deserializer);
    }

    public <U> void addSerializer(Class<U> clazz, JsonSerializer<U> serializer) {
        module.addSerializer(clazz, serializer);
    }

    public <U> void addDeserializer(Class<U> clazz, JsonDeserializer<U> deserializer) {
        module.addDeserializer(clazz, deserializer);
    }

    public void addInitializer(Consumer<T> initializer) {
        initializers.add(initializer);
    }

    public Optional<T> loadInstance() {
        Optional<String> jsonString = FileUtil.read(absolutePath.toAbsolutePath().toString());

        if (jsonString.isEmpty()) {
            return Optional.empty();
        }

        try {
            mapper.registerModule(module);
            T instance = mapper.readValue(jsonString.get(), clazz);
            initializers.forEach(init -> init.accept(instance));
            return Optional.of(instance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}
