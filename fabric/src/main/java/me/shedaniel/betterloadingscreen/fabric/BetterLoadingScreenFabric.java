package me.shedaniel.betterloadingscreen.fabric;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import net.fabricmc.api.ClientModInitializer;

public class BetterLoadingScreenFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterLoadingScreen.init();
    }
}
