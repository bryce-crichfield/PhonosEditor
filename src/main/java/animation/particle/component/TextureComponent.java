package animation.particle.component;

import animation.particle.ParticleComponent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.time.Duration;

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
