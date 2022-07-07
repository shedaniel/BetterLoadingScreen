package me.shedaniel.betterloadingscreen.mixin.forge;

import com.google.common.collect.BiMap;
import dev.quantumfusion.taski.builtin.StageTask;
import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

@Mixin(GameData.class)
public class MixinGameData {
    @Unique
    private static StepTask syncTask;
    @Unique
    private static StepTask freezeTask;
    
    @Inject(method = "freezeData", remap = false, at = @At("HEAD"))
    private static void onFreezeData(CallbackInfo ci) {
        MinecraftStub stub = (MinecraftStub) Minecraft.getInstance();
        stub.moveRenderOut();
        try {
            Field field = RegistryManager.class.getDeclaredField("registries");
            field.setAccessible(true);
            BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> frozenRegistries = (BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>>) field.get(RegistryManager.FROZEN);
            BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> registries = (BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>>) field.get(RegistryManager.ACTIVE);
            syncTask = new StepTask("Syncing Registries", registries.size());
            freezeTask = new StepTask("Freezing Registries", registries.size());
            Tasks.MAIN.setSubTask(new StageTask("Finalizing Registries", syncTask, freezeTask));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @Inject(method = "freezeData", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/registries/GameData;loadRegistry(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraftforge/registries/RegistryManager;Lnet/minecraftforge/registries/RegistryManager;Ljava/lang/Class;Z)V"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void syncRegistry(CallbackInfo ci, Iterator iterator, Map.Entry<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> entry) {
        System.out.println(entry.getValue().getRegistryName().toString());
    }
    
    @Inject(method = "freezeData", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/registries/GameData;loadRegistry(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraftforge/registries/RegistryManager;Lnet/minecraftforge/registries/RegistryManager;Ljava/lang/Class;Z)V",
            shift = At.Shift.AFTER
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void syncRegistryPost(CallbackInfo ci) {
        syncTask.next();
    }
    
    @Inject(method = {"lambda$freezeData$4", "lambda$freezeData$5", "lambda$freezeData$8", "lambda$freezeData$9"}, remap = false, at = @At(
            value = "HEAD"
    ))
    private static void freezeData(ResourceLocation id, ForgeRegistry registry, CallbackInfo ci) {
        System.out.println(registry.getRegistryName().toString());
    }
    
    @Inject(method = {"lambda$freezeData$4", "lambda$freezeData$5", "lambda$freezeData$8", "lambda$freezeData$9"}, remap = false, at = @At(
            value = "RETURN"
    ))
    private static void freezeDataPost(ResourceLocation id, ForgeRegistry registry, CallbackInfo ci) {
        freezeTask.next();
    }
    
    @Inject(method = "freezeData", remap = false, at = @At("RETURN"))
    private static void onFreezeDataPost(CallbackInfo ci) {
        Tasks.MAIN.next();
        MinecraftStub stub = (MinecraftStub) Minecraft.getInstance();
        stub.moveRenderIn();
    }
}
