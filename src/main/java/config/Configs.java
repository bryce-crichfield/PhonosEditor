package config;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;

// Represents a table of type -> instance.  This is used to store the configuration objects that are loaded from
// the file system.  The configuration object are all subtypes of Config.  The ConfigObjectFactory is
public class Configs {
    private final static Map<Class<? extends Config>, Config> configurations = new HashMap<>();
    private final static Map<Class<? extends Config>, ConfigFactory<? extends Config>> factories = new HashMap<>();
    private final static Map<Class<? extends Config>, FileWatcher> watchers = new HashMap<>();
    public static <T> T get(Class<T> clazz) {
        return clazz.cast(configurations.get(clazz));
    }


    public static ConfigsBuilder builder() {
        return new ConfigsBuilder();
    }

    private static class FileWatcher {
        private Instant lastModified = Instant.now();
        private final Path absolutePath;
        public FileWatcher(Path absolutePath) {
            this.absolutePath = absolutePath;
        }

        public boolean check() {
            File file = new File(absolutePath.toAbsolutePath().toString());
            Instant lastModified = Instant.ofEpochMilli(file.lastModified());
            if (lastModified.isAfter(this.lastModified)) {
                this.lastModified = lastModified;
                return true;
            }
            return false;
        }
    }

    private static class ConfigsReloader implements Runnable {

        @Override
        public void run() {
            for (var pair : factories.entrySet()) {
                var clazz = pair.getKey();
                var factory = pair.getValue();
                var watcher = watchers.get(clazz);
                boolean hasChanged = watcher.check();
                if (hasChanged) {
                    factory.load(false);
                }
            }
        }

        public static ConfigsReloader create() {
            var service = Executors.newScheduledThreadPool(1);
            var worker = new ConfigsReloader();
            service.scheduleAtFixedRate(worker, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
            return worker;
        }
    }

    public static void load(ConfigsBuilder builder)
    {
        var factories = builder.build();
        for (var pair : factories.entrySet()) {
            // Create a file watcher for each factory from the factory's getAbsolutePath().  The file watcher
            // will try to load the file initially and then will watch the file for changes.  If the file changes,
            // the file watcher will reload the file and update the configuration object in the configurations map.
            var clazz = pair.getKey();
            var factory = pair.getValue();
            factories.put(clazz, factory);
            watchers.put(clazz, new FileWatcher(factory.getAbsolutePath()));

            factory.load(true).ifPresentOrElse(config -> {
                System.out.println("INITALIZED: " + clazz.getName());
                configurations.put(clazz, config);
            }, () -> {
                System.err.println("Failed to load configuration object: " + clazz.getName());
            });
        }

        var reloader = ConfigsReloader.create();
        reloader.run();
    }
}
