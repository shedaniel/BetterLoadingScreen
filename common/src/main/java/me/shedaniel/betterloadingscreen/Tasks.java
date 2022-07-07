package me.shedaniel.betterloadingscreen;

import dev.quantumfusion.taski.builtin.StepTask;

public class Tasks {
    public static int LAUNCH_COUNT(boolean forge) {
        return !forge ? 4 : 7;
    }
    
    // Forge: Scanning Mods
    // Bootstrap
    // Loading Mods
    // - Common
    // - Client
    // Forge: Registry
    // Forge: Finalize Registry
    // Loading Assets
    // * Blocks
    // * Items
    // * Textures
    // Compiling Assets
    // - Stitching
    // - Baking
    public static StepTask MAIN = new StepTask("Loading Game");
    
}
