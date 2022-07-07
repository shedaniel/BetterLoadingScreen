package me.shedaniel.betterloadingscreen.launch.render;

import me.shedaniel.betterloadingscreen.launch.EarlyShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class EarlyBufferBuilder {
    private final String shader;
    private final EarlyRenderFormat format;
    private final ByteBuffer buffer;
    
    public EarlyBufferBuilder(String shader, EarlyRenderFormat format) {
        this.shader = shader;
        this.format = format;
        this.buffer = MemoryUtil.memAlloc(64 * format.stride);
    }
    
    public EarlyBufferBuilder put(byte v) {
        this.buffer.put(v);
        return this;
    }
    
    public EarlyBufferBuilder put(int v) {
        this.buffer.putInt(v);
        return this;
    }
    
    public EarlyBufferBuilder put(long v) {
        this.buffer.putLong(v);
        return this;
    }
    
    public EarlyBufferBuilder put(float v) {
        this.buffer.putFloat(v);
        return this;
    }
    
    public EarlyBufferBuilder put(double v) {
        this.buffer.putDouble(v);
        return this;
    }
    
    public EarlyBufferBuilder pos(float x, float y, float z) {
        this.put(x);
        this.put(y);
        this.put(z);
        return this;
    }
    
    public EarlyBufferBuilder color(float r, float g, float b, float a) {
        this.put(r);
        this.put(g);
        this.put(b);
        this.put(a);
        return this;
    }
    
    public EarlyBufferBuilder tex(float u, float v) {
        this.put(u);
        this.put(v);
        return this;
    }
    
    public EarlyBufferBuilder endVertex() {
        int position = this.buffer.position();
        if (position % format.stride != 0) {
            throw new IllegalStateException("Buffer position is not a multiple of stride");
        }
        return this;
    }
    
    public void end(EarlyDrawType drawType) {
        EarlyShader.useShader(shader);
        
        FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(4 * 4);
        EarlyRenderingStates.modelViewMatrix.store(floatBuffer);
        GL30.glUniformMatrix4fv(EarlyShader.getUniform(shader, "ModelView"), false, floatBuffer);
        MemoryUtil.memFree(floatBuffer);
        
        floatBuffer = MemoryUtil.memAllocFloat(4 * 4);
        EarlyRenderingStates.projectionMatrix.store(floatBuffer);
        GL30.glUniformMatrix4fv(EarlyShader.getUniform(shader, "ProjectionView"), false, floatBuffer);
        MemoryUtil.memFree(floatBuffer);
        
        int VAO = GL30.glGenVertexArrays();
        int VBO = GL15.glGenBuffers();
        int EBO = GL15.glGenBuffers();
        
        int position = this.buffer.position();
        int count = (byte) (position / format.stride);
        
        GL30.glBindVertexArray(VAO);
        
        this.buffer.position(0);
        this.buffer.limit(position);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.buffer, GL15.GL_DYNAMIC_DRAW);
        
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, drawType.getIndices(), GL15.GL_DYNAMIC_DRAW);
        
        this.format.setupAttrs();
        
        GL30.glDrawElements(drawType.getGlMode(), count / drawType.getVertexes() * drawType.getLength(), GL11.GL_UNSIGNED_BYTE, 0);
    
        this.format.cleanAttrs();
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        
        GL30.glDeleteVertexArrays(VAO);
        GL15.glDeleteBuffers(VBO);
        GL15.glDeleteBuffers(EBO);
        
        MemoryUtil.memFree(this.buffer);
    }
}
