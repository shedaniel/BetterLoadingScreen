package me.shedaniel.betterloadingscreen.mixin.forge;

import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StageTask;
import dev.quantumfusion.taski.builtin.StepTask;
import dev.quantumfusion.taski.builtin.WeightedStageTask;
import me.shedaniel.betterloadingscreen.Tasks;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import me.shedaniel.betterloadingscreen.impl.mixinstub.ModelBakeryStub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(ModelBakery.class)
public class MixinModelBakeryForge {
    @Shadow @Final private static Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS;
    
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Unique
    private StageTask stageTask;
    
    @Inject(method = "processLoading", at = @At(value = "HEAD"), remap = false)
    private void start(ProfilerFiller arg, int i, CallbackInfo ci) {
        new RuntimeException("Start of processLoading").printStackTrace();
        ModelBakeryStub stub = (ModelBakeryStub) this;
        stub.betterloadingscreen$setBlockTask(new StepTask("Blocks", Registry.BLOCK.size() + STATIC_DEFINITIONS.size()));
        stub.betterloadingscreen$setItemTask(new StepTask("Items", Registry.ITEM.size()));
        stub.betterloadingscreen$setTextureTask(new StepTask("Textures"));
        stageTask = new StageTask("Loading Assets", stub.betterloadingscreen$getBlockTask(),
                stub.betterloadingscreen$getItemTask(), stub.betterloadingscreen$getTexturesTask());
    }
    
    @Inject(method = "processLoading",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/block/state/StateDefinition;getPossibleStates()Lcom/google/common/collect/ImmutableList;",
                     shift = At.Shift.AFTER))
    private void registryBlockLoaded(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        ModelBakeryStub stub = (ModelBakeryStub) this;
        Tasks.MAIN.setSubTask(stageTask);
        stub.betterloadingscreen$getBlockTask().next();
    }
    
    // Items
    @Inject(method = "processLoading",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=items"}))
    private void itemsStart(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        ModelBakeryStub stub = (ModelBakeryStub) this;
        Tasks.MAIN.setSubTask(stageTask);
        stub.betterloadingscreen$getBlockTask().finish();
    }
    
    @Inject(method = "processLoading",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
                     ordinal = 1, shift = At.Shift.AFTER))
    private void registryItemLoaded(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        ModelBakeryStub stub = (ModelBakeryStub) this;
        Tasks.MAIN.setSubTask(stageTask);
        stub.betterloadingscreen$getItemTask().next();
    }
    
    // Textures
    @Redirect(method = "processLoading", remap = false,
              at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private <R> Stream<R> textureLoad(Stream<R> instance, Function<? super R, ? extends Stream<? extends R>> function) {
        Tasks.MAIN.setSubTask(stageTask);
        ModelBakeryStub stub = (ModelBakeryStub) this;
        stub.betterloadingscreen$getItemTask().finish();
        stub.betterloadingscreen$getTexturesTask().reset(topLevelModels.size());
        return instance.flatMap((r) -> {
            Stream<? extends R> out = function.apply(r);
            stub.betterloadingscreen$getTexturesTask().next();
            return out;
        });
    }
    
    // =============================================== Compiling Assets ===============================================
    @Unique
    private Map<ResourceLocation, WeightedStageTask.WeightedStage> stitchTaskMap;
    @Unique
    private StepTask bakingTask;
    @Unique
    private WeightedStageTask stitchTask;
    
    @Inject(method = "processLoading",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=stitching"}),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void compilingAssetsStart(ProfilerFiller profilerFiller, int i, CallbackInfo ci, Set<?> v0, Set<?> v1, Map<ResourceLocation, List<Material>> atlasMap) {
        ModelBakeryStub stub = (ModelBakeryStub) this;
        stub.betterloadingscreen$getTexturesTask().finish();
        Tasks.MAIN.next();
        
        this.stitchTaskMap = new HashMap<>();
        List<WeightedStageTask.WeightedStage> stages = new ArrayList<>();
        atlasMap.forEach((identifier, spriteIdentifiers) -> {
            WeightedStageTask.WeightedStage stage = new WeightedStageTask.WeightedStage(1, null);
            stages.add(stage);
            stitchTaskMap.put(identifier, stage);
        });
        this.stitchTask = new WeightedStageTask("Stitching Atlases", stages);
        Tasks.MAIN.setSubTask(stitchTask);
    }
    
    @Redirect(method = "processLoading",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;prepareToStitch(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/util/profiling/ProfilerFiller;I)Lnet/minecraft/client/renderer/texture/TextureAtlas$Preparations;"))
    private TextureAtlas.Preparations stitchedAtlas(TextureAtlas instance, ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i) {
        WeightedStageTask.WeightedStage task = stitchTaskMap.get(instance.location());
        if (task != null) {
            task.task = (Task) instance;
        }
        return instance.prepareToStitch(resourceManager, stream, profilerFiller, i);
    }
    
    @Inject(method = "processLoading", remap = false,
            at = @At(value = "RETURN"))
    private void stitchEnd(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        this.stitchTask.finish();
        Tasks.MAIN.next();
    }
    
    @Inject(method = "uploadTextures",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=baking"}))
    private void bakingStart(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
        ((MinecraftStub) Minecraft.getInstance()).moveRenderOut();
        this.bakingTask = new StepTask("Baking Models", topLevelModels.size());
        Tasks.MAIN.setSubTask(bakingTask);
    }
    
    @Inject(method = {"method_4733", "lambda$uploadTextures$12", "m_119368_"},
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/resources/model/ModelBakery;bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;"))
    private void bakedModel(ResourceLocation resourceLocation, CallbackInfo ci) {
        this.bakingTask.next();
    }
    
    @Inject(method = "uploadTextures", at = @At("RETURN"))
    private void end(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
        this.bakingTask.finish();
        ((MinecraftStub) Minecraft.getInstance()).moveRenderIn();
        Tasks.MAIN.next();
    }

//    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
//    
//    @Inject(method = "processLoading", remap = false, at = @At(
//            value = "INVOKE",
//            target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V",
//            ordinal = 0
//    ))
//    private void init(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
//        int stateCount = 0, itemCount = ForgeRegistries.ITEMS.getValues().size();
//        
//        for (Block block : Registry.BLOCK) {
//            stateCount += block.getStateDefinition().getPossibleStates().size();
//        }
//        
//        ParentTask task = LoadGameSteps.loadModel();
//        SteppedTask blockTask = task.stepped(LoadGameSteps.LoadModel.BLOCK);
//        blockTask.setTotalSteps(stateCount);
//        SteppedTask itemTask = task.stepped(LoadGameSteps.LoadModel.ITEM);
//        itemTask.setTotalSteps(itemCount);
//        ((ModelBakeryStub) this).betterloadingscreen$setBlockTask(blockTask);
//        ((ModelBakeryStub) this).betterloadingscreen$setItemTask(itemTask);
//    }
//    
//    @Inject(method = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
//            at = @At("HEAD"))
//    private void generateRefmap(ModelResourceLocation loc, CallbackInfo cir) {}
//    
//    @Inject(method = "processLoading", remap = false, at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
//            ordinal = 1
//    ), locals = LocalCapture.CAPTURE_FAILHARD)
//    private void startItem(ProfilerFiller profilerFiller, int i, CallbackInfo ci,
//            Iterator iterator, ResourceLocation resourceLocation) {
//        ((ModelBakeryStub) this).betterloadingscreen$getItemTask().setCurrentStepInfo(resourceLocation.toString());
//    }
//    
//    @Inject(method = "processLoading", remap = false, at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
//            ordinal = 1,
//            shift = At.Shift.AFTER
//    ))
//    private void endItem(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
//        ((ModelBakeryStub) this).betterloadingscreen$getItemTask().incrementStep();
//    }
//    
//    @Inject(method = "processLoading", remap = false, at = @At(
//            value = "INVOKE",
//            target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;",
//            ordinal = 0
//    ))
//    private void initPrepare(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
//        SteppedTask task = LoadGameSteps.prepareModel();
//        task.setTotalSteps(topLevelModels.size());
//    }
//    
//    @Inject(method = "lambda$processLoading$9", remap = false, at = @At(
//            value = "RETURN"
//    ))
//    private void endPrepare(Set set, UnbakedModel model, CallbackInfoReturnable ci) {
//        SteppedTask task = LoadGameSteps.prepareModel();
//        task.incrementStep();
//    }
}
