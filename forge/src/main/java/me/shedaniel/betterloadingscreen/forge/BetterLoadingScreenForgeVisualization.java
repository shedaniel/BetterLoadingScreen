package me.shedaniel.betterloadingscreen.forge;

import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class BetterLoadingScreenForgeVisualization {
    public static String[] extractRunArgs() {
        try {
            Field field = Launcher.class.getDeclaredField("argumentHandler");
            field.setAccessible(true);
            Object argumentHandler = field.get(Launcher.INSTANCE);
            field = ArgumentHandler.class.getDeclaredField("args");
            field.setAccessible(true);
            return (String[]) field.get(argumentHandler);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
    
    public static long handOffWindow(final IntSupplier width, final IntSupplier height, final Supplier<String> title, LongSupplier monitorSupplier) {
        return ((LongSupplier) () -> GLFW.glfwCreateWindow(width.getAsInt(), height.getAsInt(), title.get(), monitorSupplier.getAsLong(), 0L)).getAsLong();
    }
    
    public static void updateFBSize(IntConsumer width, IntConsumer height) {
    }
}
