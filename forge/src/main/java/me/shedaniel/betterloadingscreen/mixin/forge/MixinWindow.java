package me.shedaniel.betterloadingscreen.mixin.forge;

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.launch.EarlyWindow;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@Mixin(Window.class)
public abstract class MixinWindow {
    @Shadow
    protected abstract void setMode();
    
    @Shadow private boolean fullscreen;
    
    @Shadow private int x;
    
    @Shadow private int y;
    
    @Shadow @Final private long window;
    
    @Shadow private int windowedX;
    
    @Shadow private int windowedY;
    
    @Shadow private int width;
    
    @Shadow private int windowedWidth;
    
    @Shadow private int windowedHeight;
    
    @Shadow private int height;
    
    @Shadow private int framebufferWidth;
    
    @Shadow private int framebufferHeight;
    
    @Redirect(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/loading/progress/EarlyProgressVisualization;handOffWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J"
    ))
    private long createWindow(@Coerce Object visualization, IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        if (BetterLoadingScreen.isEarlyLoadingEnabled()) {
            EarlyWindow.setRender(false, true);
            
            return EarlyWindow.window;
        }
        
        return GLFW.glfwCreateWindow(width.getAsInt(), height.getAsInt(), title.get(), monitor.getAsLong(), 0L);
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
