package config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import encoding.serialize.ReflectionSerializer;
import util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

// Loads a Json File into a Config object instance by setting all object properties to the values in the json file
public final class ConfigFactory<T extends Config> {
    private final Path absolutePath;
    private final Class<T> clazz;
    private final SimpleModule module = new SimpleModule();
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Consumer<T>> initializers = new ArrayList<>();
    private final T instance;

    // @param clazz: The class type of the config object to be loaded
    // @param relativePath: The relative path to the json file that contains the config object
    public ConfigFactory(Class<T> clazz, T instance, String relativePath) {
        this(clazz, instance, relativePath, new ReflectionSerializer<>(clazz), new ReflectionObjectPropertyDeserialier<>(clazz, instance));
    }
    private ConfigFactory(Class<T> clazz, T instance, String relativePath, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        this.clazz = clazz;
        this.instance = instance;
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

    public Optional<T> load(boolean shouldInitialize) {
        Optional<String> jsonString = FileUtil.read(absolutePath.toAbsolutePath().toString());

        if (jsonString.isEmpty()) {
            System.out.println("[ConfigFactory] Failed to read file: " + absolutePath.toAbsolutePath().toString());
            return Optional.empty();
        }

        try {
            mapper.registerModule(module);
            T mappedInstance = mapper.readValue(jsonString.get(), clazz);
            // Ensure that mappedInstance and instance are the same object reference
            if (mappedInstance != instance) {
                System.out.println("[ConfigFactory] Mapped instance and instance are not the same object reference");
                return Optional.empty();
            }
            if (shouldInitialize)
                initializers.forEach(init -> init.accept(instance));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        return Optional.of(instance);
    }

    public Path getAbsolutePath() {
        return absolutePath;
    }
}
