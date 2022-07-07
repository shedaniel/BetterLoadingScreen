package me.shedaniel.betterloadingscreen.mixin;

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.betterloadingscreen.launch.EarlyWindow;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {
    @Shadow private double guiScale;
    
    @Inject(method = "onFramebufferResize", at = @At(
            value = "HEAD"
    ), cancellable = true)
    public void onFramebufferResize(long l, int i, int j, CallbackInfo ci) {
        if (EarlyWindow.hasRender) {
            EarlyWindow.framebufferResizeWithScale(guiScale, i, j);
            ci.cancel();
        }
    }
    
    @Inject(method = "onResize", at = @At(
            value = "HEAD"
    ), cancellable = true)
    public void onResize(long l, int i, int j, CallbackInfo ci) {
        if (EarlyWindow.hasRender) {
            EarlyWindow.windowResize(l, i, j);
            ci.cancel();
        }
    }
    
    @Inject(method = "onMove", at = @At(
            value = "HEAD"
    ), cancellable = true)
    public void onMove(long l, int i, int j, CallbackInfo ci) {
        if (EarlyWindow.hasRender) {
            EarlyWindow.windowMove(l, i, j);
            ci.cancel();
        }
    }
    
    @Inject(method = "onFocus", at = @At(
            value = "HEAD"
    ), cancellable = true)
    public void onFocus(long l, boolean bl, CallbackInfo ci) {
        if (EarlyWindow.hasRender) {
            ci.cancel();
        }
    }
    
    @Inject(method = "onEnter", at = @At(
            value = "HEAD"
    ), cancellable = true)
    public void onEnter(long l, boolean bl, CallbackInfo ci) {
        if (EarlyWindow.hasRender) {
            ci.cancel();
        }
    }
    
    @Inject(method = "updateDisplay", at = @At(
            value = "HEAD"
    ), cancellable = true)
    public void updateDisplay(CallbackInfo ci) {
        if (EarlyWindow.hasRender) {
            ci.cancel();
        }
    }
}
