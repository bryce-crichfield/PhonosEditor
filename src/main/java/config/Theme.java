package config;

import atlantafx.base.theme.PrimerDark;
import encoding.deserialize.ColorDeserializer;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Theme implements Config {
    public static ConfigFactory<Theme> getFactory() {
        var loader = new ConfigFactory<>(Theme.class, "theme.json");
        loader.addDeserializer(Color.class, new ColorDeserializer());

        loader.addInitializer(theme -> {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            Font.loadFont(Theme.class.getResourceAsStream("/fonts/WhiteRabbit.ttf"), 10);
        });

        return loader;
    }

    public final Color defaultColor = Color.WHITE;
    public final Color gray0 = Color.WHITE;
    public final Color gray1 = Color.WHITE;
    public final Color gray2 = Color.WHITE;
    public final Color gray3 = Color.WHITE;
    public final Color gray4 = Color.WHITE;
    public final Color gray5 = Color.WHITE;
    public final Color background = Color.WHITE;
}
