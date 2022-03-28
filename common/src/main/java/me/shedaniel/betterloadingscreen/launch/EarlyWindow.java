package me.shedaniel.betterloadingscreen.launch;

import ca.weblite.objc.NSObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sun.jna.Pointer;
import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.BetterLoadingScreenClient;
import me.shedaniel.betterloadingscreen.BetterLoadingScreenConfig;
import me.shedaniel.betterloadingscreen.EarlyGraphics;
import me.shedaniel.betterloadingscreen.api.render.EarlyWindowHook;
import me.shedaniel.betterloadingscreen.launch.early.BackgroundRenderer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

public class EarlyWindow {
    public static final Logger LOGGER = LogManager.getLogger(EarlyWindow.class);
    public static long window;
    public static int width;
    public static int height;
    public static int framebufferWidth, framebufferHeight;
    private static int x, y;
    public static int scale;
    public static boolean fullscreen;
    public static boolean running = true;
    public static Lock lock = new ReentrantLock();
    public static boolean hasRender = true;
    public static Thread thread;
    public static Timer timer = new Timer(20.0F, 0L);
    private static final Queue<Runnable> tasks = new ConcurrentLinkedDeque<>();
    public static Executor executor = tasks::add;
    
    private static void initDimensions(@Nullable Boolean fullscreen, String[] args) {
        EarlyWindow.width = 854;
        EarlyWindow.height = 480;
        EarlyWindow.fullscreen = BooleanUtils.isTrue(fullscreen);
        try {
            int i = 0;
            for (String s : args) {
                if (i == 1) {
                    try {
                        width = Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                    }
                } else if (i == 2) {
                    try {
                        height = Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                    }
                }
                i = 0;
                if ("--width".equals(s)) {
                    i = 1;
                } else if ("--height".equals(s)) {
                    i = 2;
                } else if ("--fullscreen".equals(s)) {
                    EarlyWindow.fullscreen = true;
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public static Boolean getDefaultFullscreen(Path gameDir) {
        Path path = gameDir.resolve("options.txt");
        
        if (Files.exists(path)) {
            try {
                for (String line : Files.readAllLines(path)) {
                    if (line.trim().startsWith("fullscreen:")) {
                        return Boolean.parseBoolean(line.substring(line.indexOf(':') + 1).trim());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    public static void start(String[] args, @Nullable Boolean defaultFullscreen, @Nullable String mcVersion, BackgroundRenderer renderer) {
        List<String> list = Lists.newArrayList();
        GLFWErrorCallback errorCallback = GLFW.glfwSetErrorCallback((i, l) -> {
            list.add(String.format("GLFW error during init: [0x%X]%s", i, l));
        });
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        } else {
            for (String string : list) {
                LOGGER.error("GLFW error collected during initialization: {}", string);
            }
            
            if (errorCallback != null) {
                errorCallback.close();
            }
        }
        
        GLFWErrorCallback.createPrint(System.err).set();
        
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_ANY_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        initDimensions(defaultFullscreen, args);
        if (fullscreen) {
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
            width = vidMode.width();
            height = vidMode.height();
        }
        window = GLFW.glfwCreateWindow(width, height, "Minecraft* " + mcVersion, fullscreen ? monitor : 0L, 0L);
        if (window == 0L) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }
        if (!fullscreen) {
            if (monitor == 0L) {
                int[] ax = new int[1];
                int[] ay = new int[1];
                GLFW.glfwGetWindowPos(window, ax, ay);
                x = ax[0];
                y = ay[0];
            } else {
                int[] monitorXA = new int[1];
                int[] monitorYA = new int[1];
                GLFW.glfwGetMonitorPos(monitor, monitorXA, monitorYA);
                GLFWVidMode videomode = GLFW.glfwGetVideoMode(monitor);
                int monitorX = monitorXA[0];
                int monitorY = monitorYA[0];
                x = monitorX + videomode.width() / 2 - width / 2;
                y = monitorY + videomode.height() / 2 - height / 2;
            }
        }
        
        GLFW.glfwMakeContextCurrent(window);
        setMode(monitor);
        refreshFramebufferSize();
        scale = calculateScale(false);
        GLFW.glfwSwapInterval(0);
        
        GLFW.glfwSetFramebufferSizeCallback(window, EarlyWindow::framebufferResize);
        GLFW.glfwSetWindowPosCallback(window, EarlyWindow::windowMove);
        GLFW.glfwSetWindowSizeCallback(window, EarlyWindow::windowResize);
        GLFW.glfwSetWindowFocusCallback(window, (win, focused) -> {});
        GLFW.glfwSetCursorEnterCallback(window, (win, entered) -> {});
        
        GLFW.glfwShowWindow(window);
        GLFW.glfwMakeContextCurrent(0L);
        
        thread = new Thread(() -> {
            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();
            GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            while (running) {
                try {
                    Runnable task;
                    while ((task = tasks.poll()) != null) {
                        task.run();
                    }
                    if (hasRender) {
                        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                        GL11.glViewport(0, 0, width, height);
                        GL11.glMatrixMode(GL11.GL_PROJECTION);
                        GL11.glLoadIdentity();
                        GL11.glOrtho(0.0F, (float) width / scale, 0.0F, (float) height / scale, 1000.0F, 3000.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GL11.glLoadIdentity();
                        GL11.glPushMatrix();
                        GL11.glTranslated(0, 0, -2000F);
                        render(renderer);
                        GL11.glPopMatrix();
                        GLFW.glfwSwapBuffers(window);
                        GLFW.glfwPollEvents();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
            GLFW.glfwMakeContextCurrent(0L);
        }, "Early Visualization");
        thread.setDaemon(true);
        thread.start();
    }
    
    private static void render(BackgroundRenderer renderer) {
        EarlyGraphics graphics = EarlyGraphics.INSTANCE;
        timer.advanceTime(System.currentTimeMillis());
        renderer.render(graphics);
        if (BetterLoadingScreen.CONFIG.rendersLogo) {
            renderer.renderLogo(BetterLoadingScreenConfig.getColor(BetterLoadingScreen.CONFIG.logoColor, 0xFFFFFF) | 0xFF000000);
        }
        BetterLoadingScreenClient.renderOverlay(graphics, 0, 0, timer.tickDelta, 1.0F);
        for (EarlyWindowHook hook : BetterLoadingScreenClient.hooks) {
            hook.render(graphics, timer.tickDelta);
        }
    }
    
    public static class Timer {
        public float partialTick;
        public float tickDelta;
        private long lastMs;
        private final float msPerTick;
        
        public Timer(float f, long l) {
            this.msPerTick = 1000.0F / f;
            this.lastMs = l;
        }
        
        public int advanceTime(long l) {
            this.tickDelta = (float) (l - this.lastMs) / this.msPerTick;
            this.lastMs = l;
            this.partialTick += this.tickDelta;
            int i = (int) this.partialTick;
            this.partialTick -= (float) i;
            return i;
        }
    }
    
    public static void setMode(long monitor) {
        if (monitor == 0L) {
            monitor = GLFW.glfwGetWindowMonitor(window);
        }
        boolean valid = monitor != 0L;
        if (fullscreen) {
            if (!valid) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                fullscreen = false;
            } else {
                if (BetterLoadingScreen.isMac()) {
                    toggleMacOSFullscreen(window);
                }
                
                GLFWVidMode videoMode = GLFW.glfwGetVideoMode(monitor);
                
                x = 0;
                y = 0;
                width = videoMode.width();
                height = videoMode.height();
                GLFW.glfwSetWindowMonitor(window, monitor, x, y, width, height, videoMode.refreshRate());
            }
        } else {
            GLFW.glfwSetWindowMonitor(window, 0L, x, y, width, height, -1);
        }
    }
    
    public static void refreshFramebufferSize() {
        int[] aw = new int[1];
        int[] ah = new int[1];
        GLFW.glfwGetFramebufferSize(window, aw, ah);
        framebufferWidth = aw[0] > 0 ? aw[0] : 1;
        framebufferHeight = ah[0] > 0 ? ah[0] : 1;
    }
    
    public static int calculateScale(boolean bl) {
        int j;
        for (j = 1; j < framebufferWidth && j < framebufferHeight && framebufferWidth / (j + 1) >= 320 && framebufferHeight / (j + 1) >= 240; ++j) {
        }
        
        if (bl && j % 2 != 0) {
            ++j;
        }
        
        return j;
    }
    
    public static void toggleMacOSFullscreen(long l) {
        getNsWindow(l).filter(EarlyWindow::isInMacOSKioskMode).ifPresent(EarlyWindow::toggleMacOSFullscreen);
    }
    
    private static boolean isInMacOSKioskMode(NSObject nSObject) {
        return ((Long) nSObject.sendRaw("styleMask", new Object[0]) & 16384L) == 16384L;
    }
    
    private static void toggleMacOSFullscreen(NSObject nSObject) {
        nSObject.send("toggleFullScreen:");
    }
    
    private static Optional<NSObject> getNsWindow(long l) {
        long m = GLFWNativeCocoa.glfwGetCocoaWindow(l);
        return m != 0L ? Optional.of(new NSObject(new Pointer(m))) : Optional.empty();
    }
    
    public static void updateFBSize(IntConsumer width, IntConsumer height) {
    }
    
    public static void setRender(boolean render, boolean wait) {
        EarlyWindow.tasks.clear();
        CompletableFuture<Void> async = CompletableFuture.runAsync(() -> {
            GLFW.glfwMakeContextCurrent(render ? EarlyWindow.window : 0L);
            if (!render && GLFW.glfwGetCurrentContext() != 0L) {
                throw new IllegalStateException("Failed to release GLFW context");
            }
            EarlyWindow.LOGGER.info("Set render to " + render);
            if (render) {
                GL.createCapabilities();
                refreshFramebufferSize();
                scale = calculateScale(false);
            }
            EarlyWindow.hasRender = render;
        }, EarlyWindow.executor);
        if (wait) {
            async.join();
        }
    }
    
    public static void framebufferResize(long win, int w, int h) {
        framebufferWidth = w;
        framebufferHeight = h;
        scale = calculateScale(false);
    }
    
    public static void windowMove(long win, int xpos, int ypos) {
        x = xpos;
        y = ypos;
    }
    
    public static void windowResize(long win, int w, int h) {
        width = w;
        height = h;
    }
}
