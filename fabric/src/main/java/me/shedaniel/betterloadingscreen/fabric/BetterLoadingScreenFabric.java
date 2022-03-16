package me.shedaniel.betterloadingscreen.fabric;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import net.fabricmc.api.ModInitializer;

public class BetterLoadingScreenFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BetterLoadingScreen.init();
    }
}
