package three;

import lombok.Data;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

@Data
public class Material {
    float ambientReflectivity = 0.25f;
    float diffuseReflectivity = 0.75f;
    float specularReflectivity = 0.01f;

    Vector3f ambientColor = new Vector3f(1.0f, 1f, 1f);
    Vector3f diffuseColor = new Vector3f(1.0f, 1.0f, 1.0f);
    Vector3f specularColor = new Vector3f(1.0f, 1.0f, 1.0f);


    public void onRender(Integer programId) {
        var ambientReflectivityLoc = glGetUniformLocation(programId, "ambientReflectivity");
        glUniform1f(ambientReflectivityLoc, ambientReflectivity);

        var diffuseReflectivityLoc = glGetUniformLocation(programId, "diffuseReflectivity");
        glUniform1f(diffuseReflectivityLoc, diffuseReflectivity);

        var specularReflectivityLoc = glGetUniformLocation(programId, "specularReflectivity");
        glUniform1f(specularReflectivityLoc, specularReflectivity);

        var ambientColorLoc = glGetUniformLocation(programId, "ambientColor");
        glUniform3f(ambientColorLoc, ambientColor.x, ambientColor.y, ambientColor.z);

        var diffuseColorLoc = glGetUniformLocation(programId, "diffuseColor");
        glUniform3f(diffuseColorLoc, diffuseColor.x, diffuseColor.y, diffuseColor.z);

        var specularColorLoc = glGetUniformLocation(programId, "specularColor");
        glUniform3f(specularColorLoc, specularColor.x, specularColor.y, specularColor.z);
    }


    public void setAmbientReflectivity(float ambientReflectivity) {
        this.ambientReflectivity = ambientReflectivity;
    }

    public void setDiffuseReflectivity(float diffuseReflectivity) {
        this.diffuseReflectivity = diffuseReflectivity;
    }

    public void setSpecularReflectivity(float specularReflectivity) {
        this.specularReflectivity = specularReflectivity;
    }

    public void setDiffuseColor(float r, float g, float b) {
        this.diffuseColor = new Vector3f(r, g, b);
    }

    public void setSpecularColor(float r, float g, float b) {
        this.specularColor = new Vector3f(r, g, b);
    }

    public void setAmbientColor(float r, float g, float b) {
        this.ambientColor = new Vector3f(r, g, b);
    }
}
