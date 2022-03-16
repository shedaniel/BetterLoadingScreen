package me.shedaniel.betterloadingscreen.api.render;

import me.shedaniel.betterloadingscreen.BetterLoadingScreenClient;

@FunctionalInterface
public interface EarlyWindowHook {
    static void registerHook(EarlyWindowHook hook) {
        BetterLoadingScreenClient.hooks.add(hook);
    }
    
    void render(AbstractGraphics graphics, float delta);
}
