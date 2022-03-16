package me.shedaniel.betterloadingscreen.mixin.forge;

import com.google.common.collect.BiMap;
import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

@Mixin(GameData.class)
public class MixinGameData {
    @Inject(method = "freezeData", remap = false, at = @At("HEAD"))
    private static void onFreezeData(CallbackInfo ci) {
        MinecraftStub stub = (MinecraftStub) Minecraft.getInstance();
        stub.moveRenderOut();
        ParentTask task = LoadGameSteps.finalizeRegistry();
        try {
            Field field = RegistryManager.class.getDeclaredField("registries");
            field.setAccessible(true);
            BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> frozenRegistries = (BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>>) field.get(RegistryManager.FROZEN);
            BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> registries = (BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>>) field.get(RegistryManager.ACTIVE);
            SteppedTask syncTask = task.stepped(LoadGameSteps.FinalizeRegistry.SYNC);
            syncTask.setTotalSteps(registries.size());
            SteppedTask freezeTask = task.stepped(LoadGameSteps.FinalizeRegistry.FREEZE);
            freezeTask.setTotalSteps(frozenRegistries.size() + registries.size());
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @Inject(method = "freezeData", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/registries/GameData;loadRegistry(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraftforge/registries/RegistryManager;Lnet/minecraftforge/registries/RegistryManager;Ljava/lang/Class;Z)V"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void syncRegistry(CallbackInfo ci, Iterator iterator, Map.Entry<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> entry) {
        ParentTask task = LoadGameSteps.finalizeRegistry();
        SteppedTask syncTask = task.stepped(LoadGameSteps.FinalizeRegistry.SYNC);
        syncTask.setCurrentStepInfo(entry.getValue().getRegistryName().toString());
    }
    
    @Inject(method = "freezeData", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/registries/GameData;loadRegistry(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraftforge/registries/RegistryManager;Lnet/minecraftforge/registries/RegistryManager;Ljava/lang/Class;Z)V",
            shift = At.Shift.AFTER
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void syncRegistryPost(CallbackInfo ci) {
        ParentTask task = LoadGameSteps.finalizeRegistry();
        SteppedTask syncTask = task.stepped(LoadGameSteps.FinalizeRegistry.SYNC);
        syncTask.incrementStep();
    }
    
    @Inject(method = {"lambda$freezeData$4", "lambda$freezeData$5", "lambda$freezeData$8", "lambda$freezeData$9"}, remap = false, at = @At(
            value = "HEAD"
    ))
    private static void freezeData(ResourceLocation id, ForgeRegistry registry, CallbackInfo ci) {
        ParentTask task = LoadGameSteps.finalizeRegistry();
        SteppedTask freezeTask = task.stepped(LoadGameSteps.FinalizeRegistry.FREEZE);
        freezeTask.setCurrentStepInfo(registry.getRegistryName().toString());
    }
    
    @Inject(method = {"lambda$freezeData$4", "lambda$freezeData$5", "lambda$freezeData$8", "lambda$freezeData$9"}, remap = false, at = @At(
            value = "RETURN"
    ))
    private static void freezeDataPost(ResourceLocation id, ForgeRegistry registry, CallbackInfo ci) {
        ParentTask task = LoadGameSteps.finalizeRegistry();
        SteppedTask freezeTask = task.stepped(LoadGameSteps.FinalizeRegistry.FREEZE);
        freezeTask.incrementStep();
    }
    
    @Inject(method = "freezeData", remap = false, at = @At("RETURN"))
    private static void onFreezeDataPost(CallbackInfo ci) {
        MinecraftStub stub = (MinecraftStub) Minecraft.getInstance();
        stub.moveRenderIn();
    }
}
