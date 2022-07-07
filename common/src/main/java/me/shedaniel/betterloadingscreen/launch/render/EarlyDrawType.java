package me.shedaniel.betterloadingscreen.launch.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class EarlyDrawType {
    public static final EarlyDrawType TRIANGLE = new EarlyDrawType(3, GL11.GL_TRIANGLES, 0, 1, 2);
    public static final EarlyDrawType QUAD = new EarlyDrawType(3, GL11.GL_TRIANGLES, 1, 2, 0, 2, 3, 0);
    private final int vertexes;
    private final int glMode;
    private final int length;
    private final ByteBuffer indices;
    
    public EarlyDrawType(int vertexes, int glMode, int... indices) {
        this.vertexes = vertexes;
        this.glMode = glMode;
        this.length = indices.length;
        this.indices = MemoryUtil.memAlloc(indices.length);
        int offset = 0;
        for (int i : indices) {
            this.indices.put(offset, (byte) i);
            offset++;
        }
    }
    
    public int getVertexes() {
        return vertexes;
    }
    
    public int getGlMode() {
        return glMode;
    }
    
    public int getLength() {
        return length;
    }
    
    public ByteBuffer getIndices() {
        return indices;
    }
}
