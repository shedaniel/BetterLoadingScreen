package me.shedaniel.betterloadingscreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import dev.architectury.injectables.annotations.ExpectPlatform;
import me.shedaniel.betterloadingscreen.api.render.AbstractGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

public enum MinecraftGraphics implements AbstractGraphics {
    INSTANCE;
    
    public static final Logger LOGGER = LogManager.getLogger(MinecraftGraphics.class);
    public static PoseStack stack = new PoseStack();
    
    public static Font font;
    public static Closeable closable;
    public static Font vanillaFont;
    
    public static Font getFont() {
        if (vanillaFont != null) {
            return vanillaFont;
        }
        
        if (font == null) {
            Minecraft minecraft = Minecraft.getInstance();
            ResourceManager manager = createResourceManager(minecraft.getClientPackSource().getVanillaPack());
            TextureManager textureManager = minecraft.getTextureManager();
            FontSet fontSet = new FontSet(textureManager, new ResourceLocation("default"));
            JsonElement element = JsonParser.parseString("""
                    {"type":"bitmap","file":"minecraft:font/ascii.png","ascent":7,"chars":["\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000","\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000"," !\\"#$%&'()*+,-./","0123456789:;<=>?","@ABCDEFGHIJKLMNO","PQRSTUVWXYZ[\\\\]^_","`abcdefghijklmno","pqrstuvwxyz{|}~\\u0000","\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000","\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000£\\u0000\\u0000ƒ","\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000ªº\\u0000\\u0000¬\\u0000\\u0000\\u0000«»","░▒▓│┤╡╢╖╕╣║╗╝╜╛┐","└┴┬├─┼╞╟╚╔╩╦╠═╬╧","╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀","\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000∅∈\\u0000","≡±≥≤⌠⌡÷≈°∙\\u0000√\\u207f²■\\u0000"]}""");
            fontSet.reload(List.of(
                    GlyphProviderBuilderType.BITMAP.create(element.getAsJsonObject()).create(manager)
            ));
            font = new Font(resourceLocation -> {
                return fontSet;
            });
            closable = () -> {
                fontSet.close();
                if (manager instanceof Closeable closeable) {
                    closeable.close();
                }
            };
        }
        
        return font;
    }
    
    @ExpectPlatform
    private static ResourceManager createResourceManager(VanillaPackResources pack) {
        throw new AssertionError();
    }
    
    @Override
    public void fill(int x1, int y1, int x2, int y2, int color) {
        int tmp;
        if (x1 < x2) {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        
        if (y1 < y2) {
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = stack.last().pose();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, 0.0F).color(r, g, b, a).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
    @Override
    public void bindTexture(String textureId) {
        RenderSystem.setShaderTexture(0, new ResourceLocation(textureId));
    }
    
    @Override
    public boolean bindTextureCustomStream(String textureId, Supplier<InputStream> supplier) {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        ResourceLocation location = new ResourceLocation(textureId);
        if (manager.getTexture(location, null) == null) {
            try {
                NativeImage image = NativeImage.read(supplier.get());
                DynamicTexture texture = new DynamicTexture(image);
                manager.register(location, texture);
            } catch (IOException e) {
                LOGGER.error("Failed to load texture " + textureId, e);
                return false;
            }
        }
        RenderSystem.setShaderTexture(0, location);
        return true;
    }
    
    @Override
    public void drawString(String string, int x, int y, int color) {
        getFont().draw(stack, string, x, y, color);
    }
    
    @Override
    public void drawStringWithShadow(String string, int x, int y, int color) {
        getFont().drawShadow(stack, string, x, y, color);
    }
    
    @Override
    public int width(String string) {
        return getFont().width(string);
    }
    
    @Override
    public int getScaledWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }
    
    @Override
    public int getScaledHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }
    
    @Override
    public void innerBlit(int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix4f = stack.last().pose();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, (float) z).uv(u1, v2).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, (float) z).uv(u2, v2).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, (float) z).uv(u2, v1).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z).uv(u1, v1).color(r, g, b, a).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
