package me.shedaniel.betterloadingscreen.mixin.fabric;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import me.shedaniel.betterloadingscreen.api.StatusIdentifier;
import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow public abstract void clearLevel();
    
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V"
    ), method = "<init>")
    private void init(File runDir, Object gameInstance) {
        if (runDir == null) {
            runDir = new File(".");
        }
        
        FabricLoaderImpl.INSTANCE.prepareModInit(runDir.toPath(), gameInstance);
        SteppedTask common = LoadGameSteps.initMods().stepped(LoadGameSteps.InitMods.COMMON);
        SteppedTask client = LoadGameSteps.initMods().stepped(LoadGameSteps.InitMods.CLIENT);
        Supplier<RuntimeException> commonRun = invoke("main", ModInitializer.class, LoadGameSteps.InitMods.COMMON, count -> {
            if (count == 0) {
                common.setTotalSteps(1);
                common.setCurrentStep(1);
            } else {
                common.setTotalSteps(count);
            }
        }, (mod, init) -> {
            init.onInitialize();
        });
        Supplier<RuntimeException> clientRun = invoke("client", ClientModInitializer.class, LoadGameSteps.InitMods.CLIENT, count -> {
            if (count == 0) {
                client.setTotalSteps(1);
                client.setCurrentStep(1);
            } else {
                client.setTotalSteps(count);
            }
        }, (mod, init) -> {
            init.onInitializeClient();
        });
        RuntimeException throwable = commonRun.get();
        if (throwable != null) {
            throw throwable;
        }
        throwable = clientRun.get();
        if (throwable != null) {
            throw throwable;
        }
    }
    
    @Unique
    private static <T> Supplier<RuntimeException> invoke(String name, Class<T> type, StatusIdentifier<SteppedTask> taskId, IntConsumer total, BiConsumer<ModContainer, ? super T> invoker) {
        ParentTask initMods = LoadGameSteps.initMods();
        Collection<EntrypointContainer<T>> entrypoints = FabricLoader.getInstance().getEntrypointContainers(name, type);
        Multimap<String, EntrypointContainer<T>> map = LinkedHashMultimap.create();
        SteppedTask task = initMods.stepped(taskId);
        
        for (EntrypointContainer<T> container : entrypoints) {
            ModMetadata metadata = container.getProvider().getMetadata();
            map.put(metadata.getId(), container);
        }
        
        total.accept(map.keySet().size());
        
        return () -> {
            RuntimeException exception = null;
            
            for (Map.Entry<String, Collection<EntrypointContainer<T>>> entry : map.asMap().entrySet()) {
                task.setCurrentStepInfo(Iterables.getFirst(entry.getValue(), null).getProvider().getMetadata().getName());
                
                for (EntrypointContainer<T> container : entry.getValue()) {
                    try {
                        invoker.accept(container.getProvider(), container.getEntrypoint());
                    } catch (Throwable t) {
                        exception = ExceptionUtil.gatherExceptions(t,
                                exception,
                                exc -> new RuntimeException(String.format("Could not execute entrypoint stage '%s' due to errors, provided by '%s'!",
                                        name, container.getProvider().getMetadata().getId()),
                                        exc));
                    }
                }
                
                task.incrementStep();
            }
            
            return exception;
        };
    }
}