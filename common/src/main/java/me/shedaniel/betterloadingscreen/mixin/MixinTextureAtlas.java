package me.shedaniel.betterloadingscreen.mixin;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.StitcherStub;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
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
public class MixinTextureAtlas {
    @Unique private SteppedTask activeTask;
    
    @Unique
    private void prepareToStitch(Set<ResourceLocation> set) {
        if (set.size() < 50) return;
        if (!LoadGameSteps.loadModel().isCompleted()) return;
        ParentTask task = LoadGameSteps.stitchTexture();
        activeTask = task.stepped(LoadGameSteps.StitchTexture.EXTRACT);
        task.stepped(LoadGameSteps.StitchTexture.STITCH);
        
        int size = -10;
        
        for (ResourceLocation location : set) {
            if (!MissingTextureAtlasSprite.getLocation().equals(location)) {
                size++;
            }
        }
        
        activeTask.setTotalSteps(activeTask.getTotalSteps() + size);
    }
    
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getBasicSpriteInfos(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void prepareToStitch(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            Set<ResourceLocation> set) {
        prepareToStitch(set);
    }
    
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getBasicSpriteInfos(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void prepareToStitchOptifine(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            int mipmap, Set<ResourceLocation> set) {
        prepareToStitch(set);
    }
    
    @Inject(method = {"method_18160", "lambda$getBasicSpriteInfos$2", "m_174717_", "lambda$makeSprites$2"}, at = @At(
            value = "HEAD"
    ))
    private void extractTextureStart(ResourceLocation resourceLocation, ResourceManager resourceManager, Queue queue, CallbackInfo ci) {
        if (activeTask != null) {
            activeTask.setCurrentStepInfo(resourceLocation.toString());
        }
    }
    
    @Inject(method = {"method_18160", "lambda$getBasicSpriteInfos$2", "m_174717_", "lambda$makeSprites$2"}, at = @At(
            value = "RETURN"
    ))
    private void extractTextureEnd(ResourceLocation resourceLocation, ResourceManager resourceManager, Queue queue, CallbackInfo ci) {
        if (activeTask != null) {
            activeTask.incrementStep();
        }
    }
    
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;stitch()V"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void stitch(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            Set<ResourceLocation> set, int j, Stitcher stitcher) {
        if (stitcher.texturesToBeStitched.size() < 50) return;
        if (!LoadGameSteps.loadModel().isCompleted()) return;
        ParentTask task = LoadGameSteps.stitchTexture();
        activeTask = task.stepped(LoadGameSteps.StitchTexture.STITCH);
        activeTask.setTotalSteps(stitcher.texturesToBeStitched.size());
        ((StitcherStub) stitcher).betterloadingscreen$setActiveTask(activeTask);
    }
    
    @Inject(method = "prepareToStitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;stitch()V"
    ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void stitchOptifine(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir,
            int ii, Set<ResourceLocation> set, Set<ResourceLocation> setEmissive, int j, Stitcher stitcher) {
        if (stitcher.texturesToBeStitched.size() < 50) return;
        if (!LoadGameSteps.loadModel().isCompleted()) return;
        ParentTask task = LoadGameSteps.stitchTexture();
        activeTask = task.stepped(LoadGameSteps.StitchTexture.STITCH);
        activeTask.setTotalSteps(stitcher.texturesToBeStitched.size());
        ((StitcherStub) stitcher).betterloadingscreen$setActiveTask(activeTask);
    }
}
