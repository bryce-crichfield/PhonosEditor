package config;

import encoding.deserialize.KeyCombinationDeserializer;
import javafx.scene.input.KeyCombination;

public class Keybindings implements Config {
    public static ConfigFactory<Keybindings> getFactory() {
        var factory = new ConfigFactory<>(Keybindings.class, "keybindings.json");
        factory.addDeserializer(KeyCombination.class, new KeyCombinationDeserializer());
        factory.addInitializer(keybindings -> {

        });
        return factory;
    }

    public final KeyCombination setToolToSelect = null;
    public final KeyCombination setToolToPencil = null;
    public final KeyCombination setToolToSlice = null;
    public final KeyCombination setToolToPoint = null;
}
