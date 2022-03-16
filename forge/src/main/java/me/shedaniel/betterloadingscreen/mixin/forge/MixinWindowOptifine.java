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

@Mixin(Window.class)
public abstract class MixinWindowOptifine {
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
            target = "Lnet/optifine/reflect/Reflector;callLong(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)J"
    ))
    private long createWindow(Object visualization, @Coerce Object method, Object[] args) {
        if (BetterLoadingScreen.isEarlyLoadingEnabled()) {
            EarlyWindow.setRender(false, true);
            
            return EarlyWindow.window;
        }
        
        try {
            return (long) Class.forName("net.optifine.reflect.Reflector")
                    .getMethod("callLong", Object.class, Class.class, Object[].class)
                    .invoke(null, visualization, method, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
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
