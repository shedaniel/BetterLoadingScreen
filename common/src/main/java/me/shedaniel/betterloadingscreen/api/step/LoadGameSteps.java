package me.shedaniel.betterloadingscreen.api.step;

import me.shedaniel.betterloadingscreen.api.*;

public interface LoadGameSteps {
    interface DeepScanMods {
        StatusIdentifier<SteppedTask> TASK = StatusIdentifier.of("Scanning Mods");
    }
    
    interface InitMods {
        StatusIdentifier<SteppedTask> FORGE_TASK = StatusIdentifier.of("Initializing Mods");
        StatusIdentifier<ParentTask> TASK = StatusIdentifier.of("Initializing Mods");
        StatusIdentifier<SteppedTask> COMMON = StatusIdentifier.of("Common");
        StatusIdentifier<SteppedTask> CLIENT = StatusIdentifier.of("Client");
    }
    
    interface RegisterContent {
        StatusIdentifier<SteppedTask> TASK = StatusIdentifier.of("Registering Content");
    }
    
    interface LoadModel {
        StatusIdentifier<ParentTask> TASK = StatusIdentifier.of("Loading Models");
        StatusIdentifier<SteppedTask> BLOCK = StatusIdentifier.of("Block");
        StatusIdentifier<SteppedTask> ITEM = StatusIdentifier.of("Item");
    }
    
    interface PrepareModel {
        StatusIdentifier<SteppedTask> TASK = StatusIdentifier.of("Preparing Materials");
    }
    
    interface StitchTexture {
        StatusIdentifier<ParentTask> TASK = StatusIdentifier.of("Stitching Textures");
        StatusIdentifier<SteppedTask> EXTRACT = StatusIdentifier.of("Extracting Frames");
        StatusIdentifier<SteppedTask> STITCH = StatusIdentifier.of("Stitching Frames");
    }
    
    interface FinalizeRegistry {
        StatusIdentifier<ParentTask> TASK = StatusIdentifier.of("Finalizing Registries");
        StatusIdentifier<SteppedTask> SYNC = StatusIdentifier.of("Syncing Registries");
        StatusIdentifier<SteppedTask> FREEZE = StatusIdentifier.of("Freezing Registries");
    }

    interface BakeModel {
        StatusIdentifier<SteppedTask> TASK = StatusIdentifier.of("Baking Models");
    }
    
    interface FinalizeModel {
        StatusIdentifier<SteppedTask> TASK = StatusIdentifier.of("Finalizing Models");
    }
    
    static Job loadGame() {
        return JobManager.getInstance().get(JobIdentifiers.LOAD_GAME);
    }
    
    static SteppedTask scanningMods() {
        return loadGame().stepped(LoadGameSteps.DeepScanMods.TASK);
    }
    
    static ParentTask initMods() {
        return loadGame().parent(LoadGameSteps.InitMods.TASK);
    }
    
    static SteppedTask initModsForge() {
        return loadGame().stepped(LoadGameSteps.InitMods.FORGE_TASK);
    }
    
    static SteppedTask registeringContent() {
        return loadGame().stepped(LoadGameSteps.RegisterContent.TASK);
    }
    
    static ParentTask loadModel() {
        return loadGame().parent(LoadGameSteps.LoadModel.TASK);
    }
    
    static SteppedTask prepareModel() {
        return loadGame().stepped(LoadGameSteps.PrepareModel.TASK);
    }
    
    static ParentTask finalizeRegistry() {
        return loadGame().parent(FinalizeRegistry.TASK);
    }

    static SteppedTask bakeModel() {
        return loadGame().stepped(LoadGameSteps.BakeModel.TASK);
    }
    
    static SteppedTask finalizeModel() {
        return loadGame().stepped(LoadGameSteps.FinalizeModel.TASK);
    }
    
    static ParentTask stitchTexture() {
        ParentTask task = loadGame().parent(StitchTexture.TASK);
        task.setNestedType(NestedType.SHOW_ACTIVE);
        return task;
    }
}
