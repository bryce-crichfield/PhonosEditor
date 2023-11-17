package three;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import util.Singleton;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstanceRenderer {
    private static final String VERTEX_SHADER_SOURCE = """
                #version 330 core
                
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec3 aNormal;
                layout (location = 2) in vec2 aTexCoord;
//                layout (location = 3) in vec3 aTransform;
                
                out vec3 FragPos;
                out vec3 Normal;
                out vec2 TexCoord;

                // Uniforms
                uniform mat4 uView;
                uniform mat4 uProj;
                
                void main() {
//                    vec3 position = aPos + aTransform;
                    gl_Position = uProj * uView * vec4(aPos, 1.0);
                    
                    FragPos = aPos;
                    Normal = aNormal;
                    TexCoord = aTexCoord;
                }
            """;

    private static final String FRAGMENT_SHADER_SOURCE = """
                #version 330 core
                
                in vec3 FragPos;
                in vec3 Normal;
                in vec2 TexCoord;
                
                out vec4 FragColor;
                
                uniform vec3 lightPos;
                uniform vec3 lightColor;
                
                void main() {
                    FragColor = vec4(1, 0, 0, 1);
                }
            """;

    private static final Singleton<Integer> program = new Singleton<>(() -> {
        try {
            return Pipeline.compile(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        } catch (Exception cause) {
            cause.printStackTrace();
            throw new RuntimeException(cause);
        }
    });
// =====================================================================================================================
    private Integer programId;
    private Integer vaoId;
    private Integer vboId;
    private Integer instanceVboId;
    private Integer eboId;
    private Integer instanceCount;

    private Integer vertexPerMesh;
    private Integer vertexCount;
    public InstanceRenderer(Mesh instance, int instanceCount) throws Exception {
        // compile the shader program
        try {
            programId = Pipeline.compile(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
            glUseProgram(programId);
        } catch (Exception cause) {
            cause.printStackTrace();
            throw new Exception(cause);
        }

        // create the vertex array object
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create the vertex buffer, configure the vertex attributes, and store the vertices in it
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        int aPosLocation = glGetAttribLocation(programId, "aPos");
        glVertexAttribPointer(aPosLocation, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(aPosLocation);

        int aNormalLocation = glGetAttribLocation(programId, "aNormal");
        glVertexAttribPointer(aNormalLocation, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(aNormalLocation);

        int aTexCoordLocation = glGetAttribLocation(programId, "aTexCoord");
        glVertexAttribPointer(aTexCoordLocation, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(aTexCoordLocation);

        float[] positions = instance.getPositions();
        float[] normals = instance.getNormals();
        float[] texCoords = instance.getTexCoords();

        vertexPerMesh = positions.length / 6;
        vertexCount = vertexPerMesh * instanceCount;
        float[] vertices = new float[vertexPerMesh * 8];
        for (int i = 0; i < vertices.length; i += 8) {
            vertices[i] = positions[i % 3];
            vertices[i + 1] = positions[i % 3 + 1];
            vertices[i + 2] = positions[i % 3 + 2];

            vertices[i + 3] = normals[i % 3];
            vertices[i + 4] = normals[i % 3 + 1];
            vertices[i + 5] = normals[i % 3 + 2];

            vertices[i + 6] = texCoords[i % 2];
            vertices[i + 7] = texCoords[i % 2 + 1];
        }

        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // create the instance buffer
//        instanceVboId = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, instanceVboId);
//
//        int aTransformLocation = glGetAttribLocation(programId, "aTransform");
//        glVertexAttribPointer(aTransformLocation, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
//        glEnableVertexAttribArray(aTransformLocation);
//
//        glBindBuffer(GL_ARRAY_BUFFER, 0);
//        glVertexAttribDivisor(aTransformLocation, 1);

        // Create the element buffer and store the indices in it
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);

        int[] indices = instance.getIndices();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // unbind the VAO
        glBindVertexArray(0);

        // store the instance count
        this.instanceCount = instanceCount;
    }

    public void load(Model[] models) throws Exception {
        Vector3f[] transforms = new Vector3f[models.length];

        for (int i = 0; i < models.length; i++) {
            Vector3f position = new Vector3f();
            models[i].getTransform().getTranslation(position);
            transforms[i] = position;
        }

        if (transforms.length != instanceCount) {
            throw new RuntimeException("Transforms length must match instance count");
        }

//        glBindBuffer(GL_ARRAY_BUFFER, instanceVboId);

//        float[] data = new float[transforms.length * 3];
//        for (int i = 0; i < transforms.length; i++) {
//            data[i * 3] = transforms[i].x;
//            data[i * 3 + 1] = transforms[i].y;
//            data[i * 3 + 2] = transforms[i].z;
//        }
//
//        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
    }

    public void render(Camera camera, Light light) {
        camera.onRender(programId);
        light.onRender(programId);

        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
//        glDrawElementsInstanced(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0, instanceCount);
    }

    public void dispose() {
        glDeleteProgram(programId);
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteBuffers(instanceVboId);
        glDeleteBuffers(eboId);
    }
}
