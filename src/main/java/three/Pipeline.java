package three;

import static org.lwjgl.opengl.GL20.*;

public class Pipeline {
    private static final String EXC_VERTEX_SHADER_SOURCE = """
                #version 330 core
                   
                // include the mesh attributes
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec3 aNormal;
                layout (location = 2) in vec2 aTexCoord;
                
                // include the material attributes
                layout (location = 3) in float ambientReflectivity;
                layout (location = 4) in float diffuseReflectivity;
                layout (location = 5) in float specularReflectivity;
                layout (location = 6) in vec3 ambientColor;
                layout (location = 7) in vec3 diffuseColor;
                layout (location = 8) in vec3 specularColor;
                
                // include the model matrix attribute
                layout (location = 9) in mat4 model;
                
                // Output to Fragment Shader
                out vec3 FragPos;
                out vec3 Normal;
                out vec3 ViewPos;
                out vec2 TexCoord;
                
                out float AmbientReflectivity;
                out float DiffuseReflectivity;
                out float SpecularReflectivity;
                out vec3 AmbientColor;
                out vec3 DiffuseColor;
                out vec3 SpecularColor;
                out mat4 Model;
                
                // Uniforms
                uniform mat4 view;
                uniform mat4 projection;
                
                void main() {
                    // Calculate Position
                    gl_Position = projection * view * model * vec4(aPos, 1.0);        
                    
                    // Calculate Fragment Attributes
                    FragPos = vec3(model * vec4(aPos, 1.0));
                    Normal = mat3(transpose(inverse(model))) * aNormal;
                    ViewPos = vec3(inverse(view) * vec4(0.0, 0.0, 0.0, 1.0));
                    TexCoord = vec2(aTexCoord.x, aTexCoord.y);
                    
                    // Pass Material Attributes
                    AmbientReflectivity = ambientReflectivity;
                    DiffuseReflectivity = diffuseReflectivity;
                    SpecularReflectivity = specularReflectivity;
                    AmbientColor = ambientColor;
                    DiffuseColor = diffuseColor;
                    SpecularColor = specularColor;

                }
            """;

    private static final String EXC_FRAGMENT_SHADER_SOURCE = """
                #version 330 core
                
                out vec4 FragColor;
                
                in vec3 FragPos;
                in vec3 Normal;
                in vec3 ViewPos;
                in vec2 TexCoord;
                
                in float AmbientReflectivity;
                in float DiffuseReflectivity;
                in float SpecularReflectivity;
                in vec3 AmbientColor;
                in vec3 DiffuseColor;
                in vec3 SpecularColor;
                
                uniform vec3 lightPos;
                uniform vec3 lightColor;
                
                void main() {
                    // Calculate Ambient Lighting
                    vec3 ambient = AmbientReflectivity * AmbientColor;
                    
                    // Calculate Diffuse Lighting
                    vec3 norm = normalize(Normal);
                    vec3 lightDir = normalize(lightPos - FragPos);
                    float diff = max(dot(norm, lightDir), 0.0);
                    vec3 diffuse = DiffuseReflectivity * diff * DiffuseColor;
                    
                    // Calculate Specular Lighting
                    vec3 viewDir = normalize(ViewPos - FragPos);
                    vec3 reflectDir = reflect(-lightDir, norm);
                    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
                    vec3 specular = SpecularReflectivity * spec * SpecularColor;
                    
                    // Calculate Final Color
                    FragColor = vec4(1, 0, 0, 1);
                }
            """;

    public static Integer compile(String vertexShader, String fragmentShader) throws Exception {
        // Shader Setup
        var vShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vShaderId, vertexShader);
        glCompileShader(vShaderId);
        var vertSuccess = glGetShaderi(vShaderId, GL_COMPILE_STATUS);
        if (vertSuccess == 0) {
            var infoLog = glGetShaderInfoLog(vShaderId);
            throw new Exception(infoLog);
        }

        var fShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fShaderId, fragmentShader);
        glCompileShader(fShaderId);
        var fragSuccess = glGetShaderi(fShaderId, GL_COMPILE_STATUS);
        if (fragSuccess == 0) {
            var infoLog = glGetShaderInfoLog(fShaderId);
            throw new Exception(infoLog);
        }

        int programId = glCreateProgram();
        glAttachShader(programId, vShaderId);
        glAttachShader(programId, fShaderId);
        glLinkProgram(programId);
        var programSuccess = glGetProgrami(programId, GL_LINK_STATUS);
        if (programSuccess == 0) {
            var infoLog = glGetProgramInfoLog(programId);
            throw new Exception(infoLog);
        }

        return programId;
    }
}
