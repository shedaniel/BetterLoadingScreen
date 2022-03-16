package me.shedaniel.betterloadingscreen.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class BetterLoadingScreenImpl {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
