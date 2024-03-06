package config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


// Represents a table of type -> instance.  This is used to store the configuration objects that are loaded from
// the file system.  The configuration object are all subtypes of Config.  The ConfigObjectFactory is
public class Configs {
    private final static Map<Class<? extends Config>, Config> configurations = new HashMap<>();

    public static <T> T get(Class<T> clazz) {
        return clazz.cast(configurations.get(clazz));
    }


    public static Map<Class<? extends Config>, ConfigFactory<? extends Config>> createLoaders() {
        return new HashMap<>();
    }

    public static void load(Map<Class<? extends Config>, ConfigFactory<? extends Config>> factories) {
        for (var pair : factories.entrySet()) {
            Class<? extends Config> clazz = pair.getKey();
            ConfigFactory<? extends Config> factory = pair.getValue();
            Optional<Config> objectOpt = factory.loadInstance().map(Config.class::cast);
            objectOpt.ifPresent(object -> configurations.put(clazz, object));
        }
    }
}
