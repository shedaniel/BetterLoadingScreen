package me.shedaniel.betterloadingscreen;

@FunctionalInterface
public interface EarlyWindowHook {
    static void registerHook(EarlyWindowHook hook) {
        BetterLoadingScreenClient.hooks.add(hook);
    }
    
    void render(GraphicsBackend graphics, float delta);
}
