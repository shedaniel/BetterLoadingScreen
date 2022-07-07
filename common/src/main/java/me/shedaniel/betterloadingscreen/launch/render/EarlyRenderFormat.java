package me.shedaniel.betterloadingscreen.launch.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;

public class EarlyRenderFormat {
    public static final EarlyRenderFormat POSITION_COLOR = new EarlyRenderFormat()
            .addPart(PartType.FLOAT, 3)
            .addPart(PartType.FLOAT, 4);
    public static final EarlyRenderFormat POSITION_COLOR_TEX = new EarlyRenderFormat()
            .addPart(PartType.FLOAT, 3)
            .addPart(PartType.FLOAT, 4)
            .addPart(PartType.FLOAT, 2);
    public final List<Part> parts = new ArrayList<>();
    public int stride;
    
    public EarlyRenderFormat addPart(PartType type, int count) {
        this.parts.add(new Part(type, count));
        this.updateStride();
        return this;
    }
    
    private void updateStride() {
        this.stride = 0;
        for (Part part : this.parts) {
            this.stride += part.count * part.type.size;
        }
    }
    
    public void setupAttrs() {
        int offset = 0;
        
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            GL20.glVertexAttribPointer(i, part.count, part.type.glType, false, stride, offset);
            GL20.glEnableVertexAttribArray(i);
            offset += part.count * part.type.size;
        }
    }
    
    public void cleanAttrs() {
        for (int i = 0; i < parts.size(); i++) {
            GL20.glDisableVertexAttribArray(i);
        }
    }
    
    private record Part(PartType type, int count) {}
    
    public enum PartType {
        FLOAT(GL11.GL_FLOAT, 4),
        ;
        
        private final int glType;
        private final int size;
        
        PartType(int glType, int size) {
            this.glType = glType;
            this.size = size;
        }
    }
}
