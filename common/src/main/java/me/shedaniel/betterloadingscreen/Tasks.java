package me.shedaniel.betterloadingscreen;

import dev.quantumfusion.taski.builtin.StepTask;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Tasks {
    public static int LAUNCH_COUNT(boolean forge) {
        return !forge ? 5 : 11;
    }
    
    // Forge: Scanning Mods
    // Bootstrap
    // Loading Mods
    // - Common
    // - Client
    // Forge: Registering Content
    // Forge: Common Setup
    // Loading Assets
    // * Blocks
    // * Items
    // * Textures
    // Forge: Sided Setup
    // Forge: Finalize Registry
    // Stitching
    // Baking
    // Forge: Finalize Models
    public static final Lock LOCK = new ReentrantLock();
    public static StepTask MAIN = new StepTask("Loading Game");
    
}
