package me.shedaniel.betterloadingscreen.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import me.shedaniel.betterloadingscreen.MinecraftGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mixin(targets = "net/minecraft/client/gui/font/FontManager$1")
public class MixinFont {
    @Inject(method = "apply*", at = @At("RETURN"))
    private void init(Map<ResourceLocation, List<GlyphProvider>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
        MinecraftGraphics.vanillaFont = Minecraft.getInstance().font;
        if (MinecraftGraphics.closable != null) {
            try {
                MinecraftGraphics.closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
