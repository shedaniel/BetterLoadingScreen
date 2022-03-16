package me.shedaniel.betterloadingscreen.mixin.forge;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.ModelBakeryStub;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(ModelBakery.class)
public class MixinModelBakeryForge {
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    
    @Inject(method = "processLoading", remap = false, at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V",
            ordinal = 0
    ))
    private void init(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        int stateCount = 0, itemCount = ForgeRegistries.ITEMS.getValues().size();
        
        for (Block block : Registry.BLOCK) {
            stateCount += block.getStateDefinition().getPossibleStates().size();
        }
        
        ParentTask task = LoadGameSteps.loadModel();
        SteppedTask blockTask = task.stepped(LoadGameSteps.LoadModel.BLOCK);
        blockTask.setTotalSteps(stateCount);
        SteppedTask itemTask = task.stepped(LoadGameSteps.LoadModel.ITEM);
        itemTask.setTotalSteps(itemCount);
        ((ModelBakeryStub) this).betterloadingscreen$setBlockTask(blockTask);
        ((ModelBakeryStub) this).betterloadingscreen$setItemTask(itemTask);
    }
    
    @Inject(method = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
            at = @At("HEAD"))
    private void generateRefmap(ModelResourceLocation loc, CallbackInfo cir) {}
    
    @Inject(method = "processLoading", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
            ordinal = 1
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void startItem(ProfilerFiller profilerFiller, int i, CallbackInfo ci,
            Iterator iterator, ResourceLocation resourceLocation) {
        ((ModelBakeryStub) this).betterloadingscreen$getItemTask().setCurrentStepInfo(resourceLocation.toString());
    }
    
    @Inject(method = "processLoading", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
            ordinal = 1,
            shift = At.Shift.AFTER
    ))
    private void endItem(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        ((ModelBakeryStub) this).betterloadingscreen$getItemTask().incrementStep();
    }
    
    @Inject(method = "processLoading", remap = false, at = @At(
            value = "INVOKE",
            target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;",
            ordinal = 0
    ))
    private void initPrepare(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        SteppedTask task = LoadGameSteps.prepareModel();
        task.setTotalSteps(topLevelModels.size());
    }
    
    @Inject(method = "lambda$processLoading$9", remap = false, at = @At(
            value = "RETURN"
    ))
    private void endPrepare(Set set, UnbakedModel model, CallbackInfoReturnable ci) {
        SteppedTask task = LoadGameSteps.prepareModel();
        task.incrementStep();
    }
}
