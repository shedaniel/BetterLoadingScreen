package me.shedaniel.betterloadingscreen.mixin;

import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.renderer.texture.Stitcher;
import org.jetbrains.annotations.Range;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Stitcher.class)
public class MixinStitcher implements Task {
    @Shadow
    @Final
    public Set<Stitcher.Holder> texturesToBeStitched;
    @Unique
    private final StepTask task = new StepTask("Stitching");
    
    @Inject(method = "stitch", at = @At("HEAD"))
    private void startStitch(CallbackInfo ci) {
        task.reset(texturesToBeStitched.size());
    }
    
    //@Inject(method = "stitch", at = @At(
    //		value = "INVOKE",
    //		target = "Lnet/minecraft/client/renderer/texture/Stitcher;addToStorage(Lnet/minecraft/client/renderer/texture/Stitcher$Holder;)Z"
    //), locals = LocalCapture.CAPTURE_FAILHARD)
    //private void preStitch(CallbackInfo ci, List<Stitcher.Holder> holders, Iterator iterator, Stitcher.Holder holder) {
    //	//task.setCurrentStepInfo(holder.spriteInfo.name().toString());
    //}
    
    @Inject(method = "stitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;addToStorage(Lnet/minecraft/client/renderer/texture/Stitcher$Holder;)Z",
            shift = At.Shift.AFTER
    ))
    private void postStitch(CallbackInfo ci) {
        task.next();
    }
    
    @Inject(method = "stitch", at = @At("RETURN"))
    private void endStitch(CallbackInfo ci) {
        task.finish();
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
