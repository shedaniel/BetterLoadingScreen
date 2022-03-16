package me.shedaniel.betterloadingscreen.mixin.forge;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ClientModLoader.class)
public class MixinClientModLoader {
    @Inject(method = "<clinit>", remap = false, at = @At(
            value = "RETURN"
    ))
    private static void init(CallbackInfo info) {
        try {
            Field field = ModLoader.class.getDeclaredField("statusConsumer");
            field.setAccessible(true);
            Optional<Consumer<String>> consumer = (Optional<Consumer<String>>) field.get(ModLoader.get());
            field.set(ModLoader.get(), Optional.<Consumer<String>>of((s) -> {
                if (consumer.isPresent()) {
                    consumer.get().accept(s);
                    if ("Dispatching gathering events".equals(s)) {
                        try {
                            int[] count = {0};
                            SteppedTask modsForge = LoadGameSteps.initModsForge();
                            Field activityMap = ModContainer.class.getDeclaredField("activityMap");
                            activityMap.setAccessible(true);
                            ModList modList = ModList.get();
                            for (IModInfo mod : modList.getMods()) {
                                modList.getModContainerById(mod.getModId()).ifPresent(container -> {
                                    count[0]++;
                                    try {
                                        Map<ModLoadingStage, Runnable> map = (Map<ModLoadingStage, Runnable>) activityMap.get(container);
                                        Runnable runnable = map.getOrDefault(ModLoadingStage.CONSTRUCT, () -> {});
                                        map.put(ModLoadingStage.CONSTRUCT, () -> {
                                            modsForge.setCurrentStepInfo(mod.getDisplayName());
                                            try {
                                                runnable.run();
                                            } finally {
                                                modsForge.incrementStep();
                                            }
                                        });
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });
                            }
                            modsForge.setTotalSteps(count[0]);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }
            }));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    @Inject(method = "begin", remap = false, at = @At(
            value = "HEAD"
    ))
    private static void begin(Minecraft minecraft, PackRepository defaultResourcePacks, ReloadableResourceManager mcResourceManager, ClientPackSource metadataSerializer, CallbackInfo info) {
        MinecraftStub stub = (MinecraftStub) minecraft;
        stub.moveRenderOut();
    }
    
    @Inject(method = "begin", remap = false, at = @At(
            value = "RETURN"
    ))
    private static void endBegin(Minecraft minecraft, PackRepository defaultResourcePacks, ReloadableResourceManager mcResourceManager, ClientPackSource metadataSerializer, CallbackInfo info) {
        MinecraftStub stub = (MinecraftStub) minecraft;
        stub.moveRenderIn();
    }
}
