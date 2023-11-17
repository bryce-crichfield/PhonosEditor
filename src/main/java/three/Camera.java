package three;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class Camera {
    private Vector3f position;
    private Matrix4f view;
    private Matrix4f projection;

    public Camera() {
        position = new Vector3f(0.0f, 0.0f, 10);

        view = new Matrix4f();
        Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);
        view.lookAt(position, center, new Vector3f(0.0f, 1.0f, 0.0f));

        projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(45), 1, 0.1f, 1000.0f);
    }

    public void resizeScreen(float aspect) {
        projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(45), aspect, 0.1f, 1000.0f);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Matrix4f getView() {
        return new Matrix4f(view);
    }

    public Matrix4f getProjection() {
        return new Matrix4f(projection);
    }

    public void onRender(Integer programId) {
        var viewLoc = glGetUniformLocation(programId, "view");
        Matrix4f view = getView();
        glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));

        var projectionLoc = glGetUniformLocation(programId, "projection");
        glUniformMatrix4fv(projectionLoc, false, projection.get(new float[16]));
    }

    public void translate(float x, float y, float z) {
        position.add(x, y, z);
        view = new Matrix4f();
        Vector3f center = new Vector3f(position.x, position.y, 0);
        view.lookAt(position, center, new Vector3f(0.0f, 1.0f, 0.0f));
    }
}
