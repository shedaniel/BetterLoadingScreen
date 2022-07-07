package me.shedaniel.betterloadingscreen.launch;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;

public class EarlyShaderProgram {
    public static final Map<String, EarlyShaderProgram> SHADER_PROGRAM_MAP = new HashMap<>();
    private final int handle;
    
    static EarlyShaderProgram getProgram(String shaderName) {
        EarlyShaderProgram program = SHADER_PROGRAM_MAP.computeIfAbsent(shaderName, $ -> {
            EarlyShader vertexShader = EarlyShader.createVertexShader(shaderName);
            EarlyShader fragmentShader = EarlyShader.createFragmentShader(shaderName);
            EarlyShaderProgram p = new EarlyShaderProgram(vertexShader, fragmentShader);
            GL20.glDeleteShader(vertexShader.getHandle());
            GL20.glDeleteShader(fragmentShader.getHandle());
            return p;
        });
        
        if (!GL20.glIsProgram(program.handle)) {
            GL20.glValidateProgram(program.handle);
            program.delete();
            SHADER_PROGRAM_MAP.remove(shaderName);
            return getProgram(shaderName);
        }
        
        return program;
    }
    
    public EarlyShaderProgram(EarlyShader vertexShader, EarlyShader fragmentShader) {
        handle = GL20.glCreateProgram();
        GL20.glAttachShader(handle, vertexShader.getHandle());
        GL20.glAttachShader(handle, fragmentShader.getHandle());
        GL20.glLinkProgram(handle);
        
        String infoLog = GL20.glGetProgramInfoLog(handle, 512);
        System.out.println("Shader linkage status: " + infoLog);
    }
    
    void use() {
        GL20.glUseProgram(handle);
    }
    
    int getUniform(String uniformName) {
        return GL20.glGetUniformLocation(handle, uniformName);
    }
    
    public void delete() {
        GL20.glDeleteProgram(handle);
    }
}
