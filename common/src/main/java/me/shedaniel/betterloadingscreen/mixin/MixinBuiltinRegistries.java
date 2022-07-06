package me.shedaniel.betterloadingscreen.mixin;

import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
import net.minecraft.data.BuiltinRegistries;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(BuiltinRegistries.class)
public class MixinBuiltinRegistries {
    @Shadow @Final private static Map<ResourceLocation, Supplier<?>> LOADERS;
    private static StepTask TASK;
    
    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private static void startBootstrap(CallbackInfo ci) {
        TASK = new StepTask("Bootstrapping", LOADERS.size());
        Tasks.MAIN.setSubTask(TASK);
    }
    
    @Inject(method = "method_30566", at = @At("TAIL"))
    private static void endLoad(ResourceLocation resourceLocation, Supplier supplier, CallbackInfo ci) {
        TASK.next();
    }
    
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void endBootstrap(CallbackInfo ci) {
        Tasks.MAIN.next();
    }
}
