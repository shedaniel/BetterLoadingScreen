package me.shedaniel.betterloadingscreen.mixin.forge;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

@Mixin(ForgeHooksClient.class)
public class MixinForgeHooksClient {
    @Redirect(method = "onModelBake", remap = false, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/ModLoader;postEvent(Lnet/minecraftforge/eventbus/api/Event;)V"
    ))
    private static void postEvent(ModLoader modLoader, Event event) {
        ((MinecraftStub) Minecraft.getInstance()).moveRenderOut();
        if (!ModLoader.isLoadingStateValid()) {
            return;
        }
        SteppedTask task = LoadGameSteps.finalizeModel();
        try {
            Method method = ModContainer.class.getDeclaredMethod("acceptEvent", Event.class);
            method.setAccessible(true);
            ModList list = ModList.get();
            
            int[] count = {0};
            list.forEachModContainer((s, container) -> {
                count[0]++;
            });
            task.setTotalSteps(count[0]);
            
            list.forEachModContainer((id, mc) -> {
                task.setCurrentStepInfo(list.getModContainerById(mc.getModId()).map(ModContainer::getModInfo)
                        .map(IModInfo::getDisplayName)
                        .orElse(id));
                try {
                    method.invoke(mc, event);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    task.incrementStep();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ((MinecraftStub) Minecraft.getInstance()).moveRenderIn();
    }
}
