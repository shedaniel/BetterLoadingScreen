package me.shedaniel.betterloadingscreen.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class BetterLoadingScreenImpl {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
