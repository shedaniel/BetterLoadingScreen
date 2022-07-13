package me.shedaniel.betterloadingscreen.mixin;

import me.shedaniel.betterloadingscreen.BetterLoadingScreenClient;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {

    @Inject(method = "bootStrap()V", at = @At("HEAD"))
    private static void initStart(CallbackInfo ci) {
        BetterLoadingScreenClient.bootstrapTime = System.currentTimeMillis();
    }

}
