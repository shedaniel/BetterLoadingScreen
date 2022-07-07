package me.shedaniel.betterloadingscreen.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.BetterLoadingScreenClient;
import me.shedaniel.betterloadingscreen.BetterLoadingScreenConfig;
import me.shedaniel.betterloadingscreen.MinecraftGraphics;
import me.shedaniel.betterloadingscreen.launch.render.EarlyBufferBuilder;
import me.shedaniel.betterloadingscreen.launch.render.EarlyDrawType;
import me.shedaniel.betterloadingscreen.launch.render.EarlyRenderFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Mixin(LoadingOverlay.class)
public abstract class MixinLoadingOverlay {
    @Shadow private long fadeOutStart;
    
    @Shadow
    public abstract void render(PoseStack poseStack, int i, int j, float f);
    
    @Shadow @Final private Minecraft minecraft;
    
    @Inject(method = "render", at = @At(value = "INVOKE",
                                        target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                                        ordinal = 0))
    private void renderHead(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        float g = this.fadeOutStart > -1L ? (float) (Util.getMillis() - this.fadeOutStart) / 1000.0F : -1.0F;
        if (g < 1.0F && BetterLoadingScreen.CONFIG.rainbow) {
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            MinecraftGraphics graphics = MinecraftGraphics.INSTANCE;
            float a = 1.0F - Mth.clamp(g, 0.0F, 1.0F);
            builder.vertex(0, graphics.getScaledHeight(), 0.0f).color(1.0f, 0.0f, 0.0f, a).endVertex();
            builder.vertex(graphics.getScaledWidth(), graphics.getScaledHeight(), 0.0f).color(171 / 255f, 74 / 255f, 14 / 255f, a).endVertex();
            builder.vertex(graphics.getScaledWidth(), 0, 0.0f).color(0.0f, 0.0f, 1.0f, a).endVertex();
            builder.vertex(0, 0, 0.0f).color(156 / 255f, 35 / 255f, 217 / 255f, a).endVertex();
            builder.end();
            BufferUploader.end(builder);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }
    
    @Inject(method = "render", at = @At(
            value = "RETURN"
    ))
    private void render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        float g = this.fadeOutStart > -1L ? (float) (Util.getMillis() - this.fadeOutStart) / 1000.0F : -1.0F;
        if (g < 1.0F) {
            MinecraftGraphics graphics = MinecraftGraphics.INSTANCE;
            BetterLoadingScreenClient.renderOverlay(graphics, i, j, f, 1.0F - Mth.clamp(g, 0.0F, 1.0F));
        }
    }
    
    @Redirect(method = "render", at = @At(value = "INVOKE",
                                          target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIF)V"))
    private void drawProgressBar(LoadingOverlay instance, PoseStack poseStack, int i, int j, int k, int l, float f) {
    }
    
    @ModifyArg(method = "render", at = @At(value = "INVOKE",
                                           target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"),
               index = 2)
    private int drawProgressBar(int y) {
        return y - 20;
    }
    
    @Inject(method = {"method_35733", "lambda$static$0", "m_169327_"}, at = @At(value = "HEAD"), cancellable = true)
    private static void getBrandColor(CallbackInfoReturnable<Integer> cir) {
        int bgColor = BetterLoadingScreenClient.renderer.getBackgroundColor() | 0xFF000000;
        cir.setReturnValue(bgColor);
    }
    
    @Redirect(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"
    ))
    private void blit(PoseStack poseStack, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int texWidth, int texHeight) {
        if (BetterLoadingScreen.CONFIG.rendersLogo) {
            int logoColor = BetterLoadingScreenConfig.getColor(BetterLoadingScreen.CONFIG.logoColor, 0xFFFFFF) | 0xFF000000;
            MinecraftGraphics.INSTANCE.blit(x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight, logoColor);
        }
    }
    
    @Unique
    private static final ResourceLocation BACKGROUND_PATH = new ResourceLocation(BetterLoadingScreen.MOD_ID, "background.png");
    @Unique
    private static Boolean hasCustomBackground;
    
    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/Window;getGuiScaledWidth()I",
            ordinal = 1
    ))
    private void renderBackground(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        if (hasCustomBackground == null) {
            hasCustomBackground = false;
            
            if (Files.exists(BetterLoadingScreen.BACKGROUND_PATH)) {
                TextureManager manager = Minecraft.getInstance().getTextureManager();
                AbstractTexture texture = manager.getTexture(BACKGROUND_PATH, null);
                
                if (texture == null) {
                    byte[] bytes;
                    
                    try (InputStream inputStream = Files.newInputStream(BetterLoadingScreen.BACKGROUND_PATH)) {
                        bytes = inputStream.readAllBytes();
                        texture = new DynamicTexture(NativeImage.read(new ByteArrayInputStream(bytes)));
                        manager.register(BACKGROUND_PATH, texture);
                        hasCustomBackground = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    hasCustomBackground = true;
                }
            }
        }
        
        if (hasCustomBackground) {
            RenderSystem.setShaderTexture(0, BACKGROUND_PATH);
            MinecraftGraphics.INSTANCE.innerBlit(0, minecraft.getWindow().getGuiScaledWidth(), 0, minecraft.getWindow().getGuiScaledHeight(),
                    0, 0, 1, 0, 1, 0xffffffff);
        }
    }
}
