package me.shedaniel.betterloadingscreen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface GraphicsBackend {
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
    
    int width(String string);
    
    int getScaledWidth();
    
    int getScaledHeight();
    
    void fill(int x1, int y1, int x2, int y2, int color);
    
    default void blit(int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int texWidth, int texHeight, int color) {
        innerBlit(x, x + width, y, y + height, 0, uWidth, vHeight, u, v, texWidth, texHeight, color);
    }
    
    default void innerBlit(int x1, int x2, int y1, int y2, int z, int uWidth, int vHeight, float u, float v, int texWidth, int texHeight, int color) {
        innerBlit(x1, x2, y1, y2, z, u / texWidth, (u + uWidth) / texWidth, v / texHeight, (v + vHeight) / texHeight, color);
    }
    
    void innerBlit(int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, int color);
}
