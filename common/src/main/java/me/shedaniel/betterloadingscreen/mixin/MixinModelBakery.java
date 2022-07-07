package me.shedaniel.betterloadingscreen.mixin;

import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.ModelBakeryStub;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Map;

@Mixin(ModelBakery.class)
public class MixinModelBakery implements ModelBakeryStub {
    
    @Shadow
    @Final
    private static Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS;
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Unique
    private StepTask blockTask;
    @Unique
    private StepTask itemsTask;
    @Unique
    private StepTask texturesTask;
    
    @Override
    public void betterloadingscreen$setBlockTask(StepTask task) {
        this.blockTask = task;
    }
    
    @Override
    public void betterloadingscreen$setItemTask(StepTask task) {
        this.itemsTask = task;
    }
    
    @Override
    public void betterloadingscreen$setTextureTask(StepTask task) {
        this.texturesTask = task;
    }
    
    @Override
    public StepTask betterloadingscreen$getBlockTask() {
        return blockTask;
    }
    
    @Override
    public StepTask betterloadingscreen$getItemTask() {
        return itemsTask;
    }
    
    @Override
    public StepTask betterloadingscreen$getTexturesTask() {
        return texturesTask;
    }
    
    // Blocks
    @Inject(method = {"method_4723", "lambda$processLoading$7"}, at = @At("RETURN"))
    private void builtinBlockLoaded(ResourceLocation resourceLocation, StateDefinition stateDefinition, CallbackInfo ci) {
        this.blockTask.next();
    }
}
