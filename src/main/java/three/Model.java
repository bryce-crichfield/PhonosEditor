package three;

import lombok.Data;
import org.joml.Matrix4f;

@Data
public class Model {
    private final Mesh mesh;
    private Material material;
    private Matrix4f transform;

    public Model(Mesh mesh) {
        this.mesh = mesh;
        this.material = new Material();
    }
}
