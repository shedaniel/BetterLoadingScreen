package me.shedaniel.betterloadingscreen.mixin.forge;

import net.minecraftforge.fml.client.EarlyLoaderGUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EarlyLoaderGUI.class)
public class MixinEarlyLoaderGUI {
    @Inject(method = {"renderFromGUI", "renderTickets"}, remap = false, at = @At("HEAD"), cancellable = true)
    private void render(CallbackInfo ci) {
        ci.cancel();
    }
}
