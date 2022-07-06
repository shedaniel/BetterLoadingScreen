package me.shedaniel.betterloadingscreen.mixin;

import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Range;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(TextureAtlas.class)
public class MixinTextureAtlas implements Task {
    @Unique
    private final StepTask loadTask = new StepTask("Loading Sprites");
    private final StepTask task = new StepTask("Creating Atlas", 2);
    
    @Unique
    private void prepareToStitch(Set<ResourceLocation> set) {
        
        int size = -10;
        
        for (ResourceLocation location : set) {
            if (!MissingTextureAtlasSprite.getLocation().equals(location)) {
                size++;
            }
        }
        
        this.loadTask.reset(size);
        this.task.reset(2);
        this.task.next(this.loadTask);
    }
    
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getBasicSpriteInfos(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void prepareToStitch(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            Set<ResourceLocation> set) {
        prepareToStitch(set);
    }
    
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getBasicSpriteInfos(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void prepareToStitchOptifine(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            int mipmap, Set<ResourceLocation> set) {
        prepareToStitch(set);
    }
    
    //@SuppressWarnings("UnresolvedMixinReference")
    //@Inject(method = {"method_18160", "lambda$getBasicSpriteInfos$2", "m_174717_", "lambda$makeSprites$2"}, at = @At(
    //		value = "HEAD"
    //))
    //private void extractTextureStart(ResourceLocation resourceLocation, ResourceManager resourceManager, Queue queue, CallbackInfo ci) {
    //	if (task != null) {
    //		task.setCurrentStepInfo(resourceLocation.toString());
    //	}
    //}
    
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = {"method_18160", "lambda$getBasicSpriteInfos$2", "m_174717_", "lambda$makeSprites$2"}, at = @At(
            value = "RETURN"
    ))
    private void extractTextureEnd(ResourceLocation resourceLocation, ResourceManager resourceManager, Queue queue, CallbackInfo ci) {
        loadTask.next();
    }
    
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;stitch()V"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void stitch(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            Set<ResourceLocation> set, int j, Stitcher stitcher) {
        this.task.next((Task) stitcher);
    }
    
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;stitch()V"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void stitchOptifine(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            int ii, Set<ResourceLocation> set, Set<ResourceLocation> setEmissive, int j, Stitcher stitcher) {
        this.task.next((Task) stitcher);
    }
    
    @Inject(method = "prepareToStitch", at = @At("RETURN"))
    private void stitchEnd(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir) {
        this.task.finish();
    }
    
    @Override
    public @Range(from = 0L, to = 1L) float getProgress() {
        return task.getProgress();
    }
    
    @Override
    public String getName() {
        return task.getName();
    }
    
    @Override
    public void reset() {
        task.reset();
    }
    
    @Override
    public void finish() {
        task.finish();
    }
    
    @Override
    public boolean done() {
        return task.done();
    }
}
