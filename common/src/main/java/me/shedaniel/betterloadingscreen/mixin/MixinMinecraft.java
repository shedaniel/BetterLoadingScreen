package me.shedaniel.betterloadingscreen.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import me.shedaniel.betterloadingscreen.launch.EarlyWindow;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public class MixinMinecraft implements MinecraftStub {
    @Shadow private Thread gameThread;
    
    @Override
    public void moveRenderOut() {
        if (!BetterLoadingScreen.isFurtherLoadingEnabled()) return;
        Runnable task = () -> {
            RenderSystem.assertOnRenderThread();
            EarlyWindow.LOGGER.info("Moving render out");
            EarlyWindow.lock.lock();
            EarlyWindow.LOGGER.info("Acquired lock for render out");
            
            try {
                GLFW.glfwMakeContextCurrent(0L);
                if (GLFW.glfwGetCurrentContext() != 0L) {
                    throw new IllegalStateException("Failed to release GLFW context");
                }
                Window window = Minecraft.getInstance().getWindow();
                EarlyWindow.width = window.width;
                EarlyWindow.height = window.height;
                EarlyWindow.framebufferWidth = window.framebufferWidth;
                EarlyWindow.framebufferHeight = window.framebufferHeight;
                EarlyWindow.fullscreen = window.fullscreen;
                EarlyWindow.scale = EarlyWindow.calculateScale(false);
                EarlyWindow.setRender(true, true);
            } finally {
                EarlyWindow.lock.unlock();
            }
        };
        if (RenderSystem.isOnRenderThread()) {
            task.run();
        } else {
            Minecraft.getInstance().execute(task);
        }
    }
    
    @Override
    public void moveRenderIn() {
        if (!BetterLoadingScreen.isFurtherLoadingEnabled()) return;
        Runnable task = () -> {
            RenderSystem.assertOnRenderThread();
            EarlyWindow.LOGGER.info("Moving render in");
            EarlyWindow.lock.lock();
            int t = 0x10008;
            EarlyWindow.LOGGER.info("Acquired lock for render in");
            
            try {
                EarlyWindow.setRender(false, true);
                GLFW.glfwMakeContextCurrent(EarlyWindow.window);
                GL.createCapabilities();
                Window window = Minecraft.getInstance().getWindow();
                int[] ax = new int[1];
                int[] ay = new int[1];
                GLFW.glfwGetWindowPos(window.getWindow(), ax, ay);
                window.windowedX = window.x = ax[0];
                window.windowedY = window.y = ay[0];
                window.windowedWidth = window.width = EarlyWindow.width;
                window.windowedHeight = window.height = EarlyWindow.height;
                window.framebufferWidth = EarlyWindow.framebufferWidth;
                window.framebufferHeight = EarlyWindow.framebufferHeight;
                window.fullscreen = EarlyWindow.fullscreen;
            } finally {
                EarlyWindow.lock.unlock();
            }
        };
        if (RenderSystem.isOnRenderThread()) {
            task.run();
        } else {
            Minecraft.getInstance().execute(task);
        }
    }
}
