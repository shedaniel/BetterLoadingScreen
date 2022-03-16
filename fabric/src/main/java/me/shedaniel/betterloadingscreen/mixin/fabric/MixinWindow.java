package me.shedaniel.betterloadingscreen.mixin.fabric;

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.launch.EarlyWindow;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public abstract class MixinWindow {
    @Shadow
    protected abstract void setMode();
    
    @Shadow private boolean fullscreen;
    
    @Shadow public int x;
    
    @Shadow public int y;
    
    @Shadow @Final private long window;
    
    @Shadow public int windowedX;
    
    @Shadow public int windowedY;
    
    @Shadow public int width;
    
    @Shadow public int windowedWidth;
    
    @Shadow public int windowedHeight;
    
    @Shadow public int height;
    
    @Shadow public int framebufferWidth;
    
    @Shadow public int framebufferHeight;
    
    @Redirect(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"
    ))
    private long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        if (BetterLoadingScreen.isEarlyLoadingEnabled()) {
            EarlyWindow.setRender(false, true);
            
            return EarlyWindow.window;
        }
        
        return GLFW.glfwCreateWindow(width, height, title, monitor, share);
    }
    
    @Redirect(method = "defaultErrorCallback", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThread()V"
    ))
    private void defaultErrorCallback() {
        new RuntimeException().printStackTrace();
    }
    
    @Redirect(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/Window;setMode()V"
    ))
    private void setMode(Window window) {
        if (BetterLoadingScreen.isEarlyLoadingEnabled()) {
            int[] ax = new int[1];
            int[] ay = new int[1];
            GLFW.glfwGetWindowPos(this.window, ax, ay);
            this.windowedX = this.x = ax[0];
            this.windowedY = this.y = ay[0];
            this.windowedWidth = this.width = EarlyWindow.width;
            this.windowedHeight = this.height = EarlyWindow.height;
            this.framebufferWidth = EarlyWindow.framebufferWidth;
            this.framebufferHeight = EarlyWindow.framebufferHeight;
            this.fullscreen = EarlyWindow.fullscreen;
            return;
        }
        
        this.setMode();
    }
}
