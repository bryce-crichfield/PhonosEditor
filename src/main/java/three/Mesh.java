package three;

import lombok.Data;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static org.lwjgl.assimp.Assimp.*;

@Data
public class Mesh {
    private final float[] positions;
    private final float[] normals;
    private final float[] texCoords;
    private final int[] indices;

    public Mesh clone() {
        float[] newPositions = new float[positions.length];
        float[] newNormals = new float[normals.length];
        float[] newTexCoords = new float[texCoords.length];
        int[] newIndices = new int[indices.length];

        System.arraycopy(positions, 0, newPositions, 0, positions.length);
        System.arraycopy(normals, 0, newNormals, 0, normals.length);
        System.arraycopy(texCoords, 0, newTexCoords, 0, texCoords.length);
        System.arraycopy(indices, 0, newIndices, 0, indices.length);

        return new Mesh(newPositions, newNormals, newTexCoords, newIndices);
    }

    public static Mesh load(String path) {
        File file = new File(path);

        AIScene scene = aiImportFile(file.getAbsolutePath(), aiProcess_Triangulate | aiProcess_FlipUVs);
        if (scene == null) {
            throw new RuntimeException(aiGetErrorString());
        }

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));

        float[] positions = new float[mesh.mNumVertices() * 3];
        float[] normals = new float[mesh.mNumVertices() * 3];
        float[] texCoords = new float[mesh.mNumVertices() * 2];
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            positions[i] = mesh.mVertices().get(i).x();
            positions[i + 1] = mesh.mVertices().get(i).y();
            positions[i + 2] = mesh.mVertices().get(i).z();

            normals[i + 1] = mesh.mNormals().get(i).x();
            normals[i + 2] = mesh.mNormals().get(i).y();
            normals[i + 3] = mesh.mNormals().get(i).z();

            if (mesh.mTextureCoords(0) != null) {
                texCoords[i + 1] = mesh.mTextureCoords(0).get(i).x();
                texCoords[i + 2] = mesh.mTextureCoords(0).get(i).y();
            } else {
                texCoords[i + 1] = 0;
                texCoords[i + 2] = 0;
            }
        }

        int[] indices = new int[mesh.mNumFaces() * 3];
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            int offset = i * 3;

            indices[offset] = mesh.mFaces().get(i).mIndices().get(0);
            indices[offset + 1] = mesh.mFaces().get(i).mIndices().get(1);
            indices[offset + 2] = mesh.mFaces().get(i).mIndices().get(2);
        }

        return new Mesh(positions, normals, texCoords, indices);
    }

    public static Map<String, Mesh> loadMeshesFromDirectory(String directory) {
        String modelsDirectoryPath = Mesh.class.getClassLoader().getResource(directory).getPath();
        File modelsDirectory = new File(modelsDirectoryPath);

        Function<File, String> getMeshId = file -> {
            String fileName = file.getName();
            return fileName.substring(0, fileName.lastIndexOf("."));
        };

        Function<File, Optional<Mesh>> loadMesh = file -> {
            try {
                return Optional.of(Mesh.load(file.getAbsolutePath()));
            } catch (Exception error) {
                error.printStackTrace();
                return Optional.empty();
            }
        };


        return Arrays.stream(modelsDirectory.listFiles())
                .filter(file -> file.getName().endsWith(".obj"))
                .collect(HashMap::new, (map, file) -> {
                    String meshId = getMeshId.apply(file);
                    Optional<Mesh> mesh = loadMesh.apply(file);
                    mesh.ifPresent(value -> map.put(meshId, value));
                }, HashMap::putAll);
    }
}
