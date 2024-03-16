package config;

import atlantafx.base.theme.PrimerDark;
import encoding.deserialize.ColorDeserializer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Theme implements Config {
    public final Color defaultColor = Color.WHITE;
    public final ObjectProperty<Color> textDark = defaultColorProperty();
    public final ObjectProperty<Color> textLight = defaultColorProperty();
    public final ObjectProperty<Color> backgroundGridDark = defaultColorProperty();
    public final ObjectProperty<Color> backgroundGridLight = defaultColorProperty();
    public final Color backgroundGridLineDark = Color.WHITE;
    public final Color backgroundGridLineLight = Color.WHITE;

    // [ DEBUG ]: This is a debug message
    {
        textDark.addListener((observable, oldValue, newValue) -> {
            System.out.println("[Theme] Text Dark changed to: " + newValue);
        });

        textLight.addListener((observable, oldValue, newValue) -> {
            System.out.println("[Theme] Text Light changed to: " + newValue);
        });
    }

    public static ConfigFactory<Theme> getFactory() {
        // Make sure we load from the correct file path.
        var loader = new ConfigFactory<>(Theme.class, new Theme(), "theme.json");
        // Add custom deserializers for this config object
        loader.addDeserializer(Color.class, new ColorDeserializer());
        // Add custom initializers for this config object
        loader.addInitializer(theme -> {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            Font.loadFont(Theme.class.getResourceAsStream("/fonts/WhiteRabbit.ttf"), 10);
        });
        return loader;
    }

    public static ObjectProperty<Color> defaultColorProperty() {
        return new SimpleObjectProperty<>(Color.WHITE);
    }
}
