package me.shedaniel.betterloadingscreen.mixin.fabric;

import dev.quantumfusion.taski.builtin.StageTask;
import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "InvalidInjectorMethodSignature"})
    @Redirect(at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V"
    ), method = "<init>")
    private void init(File runDir, Object gameInstance) {
        if (runDir == null) {
            runDir = new File(".");
        }
        List<EntrypointContainer<ModInitializer>> commonContainers = FabricLoader.getInstance().getEntrypointContainers("main", ModInitializer.class);
        List<EntrypointContainer<ClientModInitializer>> clientContainers = FabricLoader.getInstance().getEntrypointContainers("client", ClientModInitializer.class);
        
        FabricLoaderImpl.INSTANCE.prepareModInit(runDir.toPath(), gameInstance);
        StepTask common = new StepTask("Common", commonContainers.size());
        StepTask client = new StepTask("Client", clientContainers.size());
        Tasks.MAIN.setSubTask(new StageTask("Loading Mods", common, client));
        invoke("main", commonContainers, common, ModInitializer::onInitialize);
        invoke("client", clientContainers, client, ClientModInitializer::onInitializeClient);
        Tasks.MAIN.next();
    }
    
    @Unique
    private static <T> void invoke(String name, List<EntrypointContainer<T>> containers, StepTask task, Consumer<T> invoker) {
        RuntimeException exception = null;
        task.reset(containers.size());
        for (EntrypointContainer<T> container : containers) {
            try {
                invoker.accept(container.getEntrypoint());
            } catch (Throwable t) {
                exception = ExceptionUtil.gatherExceptions(t,
                        exception,
                        exc -> new RuntimeException(String.format("Could not execute entrypoint stage '%s' due to errors, provided by '%s'!",
                                name, container.getProvider().getMetadata().getId()),
                                exc));
            }
            
            task.next();
        }
        
        if (exception != null) {
            throw exception;
        }
    }
}