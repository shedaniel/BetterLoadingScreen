package me.shedaniel.betterloadingscreen.api.render;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface AbstractGraphics {
    void fill(int x1, int y1, int x2, int y2, int color);
    
    void bindTexture(String textureId);
    
    default boolean bindTextureCustom(String textureId, Supplier<Path> supplier) {
        return bindTextureCustomStream(textureId, () -> {
            byte[] bytes;
            
            try (InputStream inputStream = Files.newInputStream(supplier.get())) {
                bytes = inputStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            return new ByteArrayInputStream(bytes);
        });
    }
    
    boolean bindTextureCustomStream(String textureId, Supplier<InputStream> supplier);
    
    void drawString(String string, int x, int y, int color);
    
    void drawStringWithShadow(String string, int x, int y, int color);
    
    int width(String string);
    
    int getScaledWidth();
    
    int getScaledHeight();
    
    default void blit(int x, int y, int z, int u, int v, int width, int height) {
        blit(x, y, z, (float) u, (float) v, width, height, 256, 256);
    }
    
    default void blit(int x, int y, int z, int u, int v, int width, int height, int color) {
        blit(x, y, z, (float) u, (float) v, width, height, 256, 256, color);
    }
    
    default void blit(int x, int y, int z, float u, float v, int width, int height, int texWidth, int texHeight) {
        blit(x, y, z, u, v, width, height, texWidth, texHeight, 0xffffffff);
    }
    
    default void blit(int x, int y, int z, float u, float v, int width, int height, int texWidth, int texHeight, int color) {
        innerBlit(x, x + width, y, y + height, z, width, height, u, v, texWidth, texHeight, color);
    }
    
    default void innerBlit(int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
        innerBlit(x1, x2, y1, y2, z, u1, u2, v1, v2, 0xffffffff);
    }
    
    default void blit(int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int texWidth, int texHeight) {
        innerBlit(x, x + width, y, y + height, 0, uWidth, vHeight, u, v, texWidth, texHeight);
    }
    
    default void blit(int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int texWidth, int texHeight, int color) {
        innerBlit(x, x + width, y, y + height, 0, uWidth, vHeight, u, v, texWidth, texHeight, color);
    }
    
    default void blit(int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        blit(x, y, width, height, u, v, width, height, texWidth, texHeight);
    }
    
    default void blit(int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color) {
        blit(x, y, width, height, u, v, width, height, texWidth, texHeight, color);
    }
    
    default void innerBlit(int x1, int x2, int y1, int y2, int z, int uWidth, int vHeight, float u, float v, int texWidth, int texHeight) {
        innerBlit(x1, x2, y1, y2, z, u / texWidth, (u + uWidth) / texWidth, v / texHeight, (v + vHeight) / texHeight);
    }
    
    default void innerBlit(int x1, int x2, int y1, int y2, int z, int uWidth, int vHeight, float u, float v, int texWidth, int texHeight, int color) {
        innerBlit(x1, x2, y1, y2, z, u / texWidth, (u + uWidth) / texWidth, v / texHeight, (v + vHeight) / texHeight, color);
    }
    
    void innerBlit(int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, int color);
}
