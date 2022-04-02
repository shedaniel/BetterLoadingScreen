package me.shedaniel.betterloadingscreen.mixin;

import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StageTask;
import dev.quantumfusion.taski.builtin.StepTask;
import dev.quantumfusion.taski.builtin.WeightedStageTask;
import me.shedaniel.betterloadingscreen.Tasks;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
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
public class MixinModelBakery {

	@Shadow
	@Final
	private static Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS;
	@Shadow
	@Final
	private Map<ResourceLocation, UnbakedModel> topLevelModels;
	private StepTask blockTask;
	private StepTask itemsTask;
	private StepTask texturesTask;


	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Object;<init>()V", shift = At.Shift.AFTER))
	private void start(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
		this.blockTask = new StepTask("Blocks", Registry.BLOCK.size() + STATIC_DEFINITIONS.size());
		this.itemsTask = new StepTask("Items", Registry.ITEM.size());
		this.texturesTask = new StepTask("Textures");
		Tasks.MAIN.setSubTask(new StageTask("Loading Assets", blockTask, itemsTask, texturesTask));
	}

	// Blocks
	@Inject(method = "method_4723", at = @At("RETURN"))
	private void builtinBlockLoaded(ResourceLocation resourceLocation, StateDefinition stateDefinition, CallbackInfo ci) {
		this.blockTask.next();
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
	private void registryBlockLoaded(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
		this.blockTask.next();
	}

	// Items
	@Inject(method = "<init>", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=items"}))
	private void itemsStart(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
		this.blockTask.finish();
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V", ordinal = 1, shift = At.Shift.AFTER))
	private void registryItemLoaded(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
		this.itemsTask.next();
	}

	// Textures
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
	private <R> Stream<R> textureLoad(Stream<R> instance, Function<? super R, ? extends Stream<? extends R>> function) {
		this.itemsTask.finish();
		this.texturesTask.reset(topLevelModels.size());
		return instance.flatMap((r) -> {
			Stream<? extends R> out = function.apply(r);
			this.texturesTask.next();
			return out;
		});
	}

	// =============================================== Compiling Assets ===============================================
	private Map<ResourceLocation, WeightedStageTask.WeightedStage> stitchTaskMap;
	private StepTask bakingTask;
	private WeightedStageTask stitchTask;

	@Inject(method = "<init>", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=stitching"}), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void compilingAssetsStart(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci, Set<?> v0, Set<?> v1, Map<ResourceLocation, List<Material>> atlasMap) {
		this.texturesTask.finish();
		Tasks.MAIN.next();

		this.stitchTaskMap = new HashMap<>();
		List<WeightedStageTask.WeightedStage> stages = new ArrayList<>();
		atlasMap.forEach((identifier, spriteIdentifiers) -> {
			WeightedStageTask.WeightedStage stage = new WeightedStageTask.WeightedStage(spriteIdentifiers.size(), null);
			stages.add(stage);
			stitchTaskMap.put(identifier, stage);
		});
		this.stitchTask = new WeightedStageTask("Stitching", stages);
		this.bakingTask = new StepTask("Baking", topLevelModels.size());
		Tasks.MAIN.setSubTask(new StageTask("Compiling Assets", stitchTask, bakingTask));
	}


	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;prepareToStitch(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/util/profiling/ProfilerFiller;I)Lnet/minecraft/client/renderer/texture/TextureAtlas$Preparations;"))
	private TextureAtlas.Preparations stitchedAtlas(TextureAtlas instance, ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i) {
		WeightedStageTask.WeightedStage task = stitchTaskMap.get(instance.location());
		if (task != null) {
			task.task = (Task) instance;
		}
		return instance.prepareToStitch(resourceManager, stream, profilerFiller, i);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
	private void stitchEnd(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
		this.stitchTask.finish();
	}

	@Inject(method = "uploadTextures", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = {"ldc=baking"}))
	private void bakingStart(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
		((MinecraftStub) Minecraft.getInstance()).moveRenderOut();
		this.bakingTask.reset(topLevelModels.size());
	}

	@Inject(method = "method_4733", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/resources/model/ModelBakery;bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;"))
	private void bakedModel(ResourceLocation resourceLocation, CallbackInfo ci) {
		this.bakingTask.next();
	}

	@Inject(method = "uploadTextures", at = @At("RETURN"))
	private void end(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
		this.bakingTask.finish();
		((MinecraftStub) Minecraft.getInstance()).moveRenderIn();
		Tasks.MAIN.next();
	}
}
