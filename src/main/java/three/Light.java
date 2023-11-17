package three;

import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL20.*;

public class Light {
    Vector3f position = new Vector3f(0.0f, 0.0f, 0);
    Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

    public void onRender(Integer programId) {
        var lightPositionLoc = glGetUniformLocation(programId, "lightPos");
        glUniform3f(lightPositionLoc, position.x, position.y, position.z);

        var lightColorLoc = glGetUniformLocation(programId, "lightColor");
        glUniform3f(lightColorLoc, color.x, color.y, color.z);
    }
    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(float r, float g, float b) {
        this.color = new Vector3f(r, g, b);
    }
}
