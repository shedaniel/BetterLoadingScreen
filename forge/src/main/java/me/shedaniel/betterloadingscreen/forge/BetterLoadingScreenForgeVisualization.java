package me.shedaniel.betterloadingscreen.forge;

import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import me.shedaniel.betterloadingscreen.launch.early.BackgroundRenderer;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

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
        return FMLLoader.getGameLayer().findModule("forge")
                .map(l -> Class.forName(l, "net.minecraftforge.client.loading.NoVizFallback"))
                .map(LamdbaExceptionUtils.rethrowFunction(c -> c.getMethod("fallback", IntSupplier.class, IntSupplier.class, Supplier.class, LongSupplier.class)))
                .map(LamdbaExceptionUtils.rethrowFunction(m -> (LongSupplier) m.invoke(null, width, height, title, monitorSupplier)))
                .map(LongSupplier::getAsLong)
                .orElseThrow(() -> new IllegalStateException("Why are you here?"));
    }
    
    public static void updateFBSize(IntConsumer width, IntConsumer height) {
    }
}
