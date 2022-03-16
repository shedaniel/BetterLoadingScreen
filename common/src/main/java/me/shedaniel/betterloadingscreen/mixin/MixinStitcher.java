package me.shedaniel.betterloadingscreen.mixin;

import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.StitcherStub;
import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(Stitcher.class)
public class MixinStitcher implements StitcherStub {
    @Unique private SteppedTask activeTask;
    
    @Inject(method = "stitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;addToStorage(Lnet/minecraft/client/renderer/texture/Stitcher$Holder;)Z"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void preStitch(CallbackInfo ci, List<Stitcher.Holder> holders, Iterator iterator, Stitcher.Holder holder) {
        if (activeTask != null) {
            activeTask.setCurrentStepInfo(holder.spriteInfo.name().toString());
        }
    }
    
    @Inject(method = "stitch", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/Stitcher;addToStorage(Lnet/minecraft/client/renderer/texture/Stitcher$Holder;)Z",
            shift = At.Shift.AFTER
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void postStitch(CallbackInfo ci, List<Stitcher.Holder> holders, Iterator iterator, Stitcher.Holder holder) {
        if (activeTask != null) {
            activeTask.incrementStep();
        }
    }
    
    @Override
    public void betterloadingscreen$setActiveTask(SteppedTask activeTask) {
        this.activeTask = activeTask;
    }
}
