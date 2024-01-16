package animation.particle.component;

import animation.particle.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;

import java.time.*;

public class TextureComponent extends ParticleComponent {
    private final Image image;

    public TextureComponent(Image image) {
        this.image = image;
    }

    @Override
    public void onTick(Duration time, Duration delta) {
        ImagePattern pattern = new ImagePattern(image);
        base.setFill(pattern);
    }
}
