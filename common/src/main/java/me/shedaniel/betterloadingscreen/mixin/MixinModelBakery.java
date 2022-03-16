package me.shedaniel.betterloadingscreen.mixin;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import me.shedaniel.betterloadingscreen.impl.mixinstub.ModelBakeryStub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelBakery.class)
public class MixinModelBakery implements ModelBakeryStub {
    @Shadow @Final private static Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Unique private SteppedTask blockTask;
    @Unique private SteppedTask itemTask;
    
    @Override
    public void betterloadingscreen$setBlockTask(SteppedTask task) {
        this.blockTask = task;
    }
    
    @Override
    public SteppedTask betterloadingscreen$getBlockTask() {
        return blockTask;
    }
    
    @Override
    public void betterloadingscreen$setItemTask(SteppedTask task) {
        this.itemTask = task;
    }
    
    @Override
    public SteppedTask betterloadingscreen$getItemTask() {
        return itemTask;
    }
    
    @Inject(method = {"method_4716", "lambda$processLoading$8"}, at = @At("HEAD"))
    private void startBlock(BlockState blockState, CallbackInfo ci) {
        blockTask.setCurrentStepInfo(Registry.BLOCK.getKey(blockState.getBlock()).toString());
    }
    
    @Inject(method = {"method_4716", "lambda$processLoading$8"}, at = @At("RETURN"))
    private void endBlock(BlockState blockState, CallbackInfo ci) {
        blockTask.incrementStep();
    }
    
    @Inject(method = "uploadTextures", at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;keySet()Ljava/util/Set;",
            ordinal = 0
    ))
    private void preBaking(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
        ((MinecraftStub) Minecraft.getInstance()).moveRenderOut();
        LoadGameSteps.bakeModel().setTotalSteps(topLevelModels.size());
    }
    
    @Inject(method = {"method_4733", "lambda$uploadTextures$12", "m_119368_"}, at = @At("HEAD"))
    private void startBaking(ResourceLocation resourceLocation, CallbackInfo ci) {
        LoadGameSteps.bakeModel().setCurrentStepInfo(resourceLocation.toString());
    }
    
    @Inject(method = {"method_4733", "lambda$uploadTextures$12", "m_119368_"}, at = @At("RETURN"))
    private void endBaking(ResourceLocation resourceLocation, CallbackInfo ci) {
        LoadGameSteps.bakeModel().incrementStep();
    }
    
    @Inject(method = "uploadTextures", at = @At(
            value = "RETURN"
    ))
    private void postBaking(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
        ((MinecraftStub) Minecraft.getInstance()).moveRenderIn();
    }
}
