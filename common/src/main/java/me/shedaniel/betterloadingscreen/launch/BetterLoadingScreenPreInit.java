package me.shedaniel.betterloadingscreen.launch;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.impl.Internals;
import me.shedaniel.betterloadingscreen.impl.JobManagerImpl;

public class BetterLoadingScreenPreInit {
    public static void init(boolean fabric) {
        Internals.manager = new JobManagerImpl();
        LoadGameSteps.loadGame();
        if (!fabric) {
            LoadGameSteps.scanningMods();
            LoadGameSteps.initModsForge();
            LoadGameSteps.registeringContent();
        } else {
            LoadGameSteps.initMods();
        }
        LoadGameSteps.loadModel();
        LoadGameSteps.prepareModel();
        LoadGameSteps.stitchTexture();
        if (!fabric) {
            LoadGameSteps.finalizeRegistry();
        }
        LoadGameSteps.bakeModel();
        if (!fabric) {
            LoadGameSteps.finalizeModel();
        }
        Internals.manager.freeze();
    }
}
