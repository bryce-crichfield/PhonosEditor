package config;

import java.util.HashMap;
import java.util.Map;

public class ConfigsBuilder {
    Map<Class<? extends Config>, ConfigFactory<? extends Config>> factories;

    public ConfigsBuilder() {
        factories = new HashMap<>();
    }

    public ConfigsBuilder addFactory(Class<? extends Config> clazz, ConfigFactory<? extends Config> factory) {
        factories.put(clazz, factory);
        return this;
    }

    public Map<Class<? extends Config>, ConfigFactory<? extends Config>> build() {
        return factories;
    }
}
