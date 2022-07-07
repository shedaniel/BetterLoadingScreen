package me.shedaniel.betterloadingscreen.mixin.forge;

import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
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
        StepTask task = new StepTask("Finalizing Models");
        try {
            Method method = ModContainer.class.getDeclaredMethod("acceptEvent", Event.class);
            method.setAccessible(true);
            ModList list = ModList.get();
            
            int[] count = {0};
            list.forEachModContainer((s, container) -> {
                count[0]++;
            });
            task.reset(count[0]);
            Tasks.MAIN.setSubTask(task);
            
            list.forEachModContainer((id, mc) -> {
                System.out.println(list.getModContainerById(mc.getModId()).map(ModContainer::getModInfo)
                        .map(IModInfo::getDisplayName)
                        .orElse(id));
                try {
                    method.invoke(mc, event);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    task.next();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Tasks.MAIN.next();
            ((MinecraftStub) Minecraft.getInstance()).moveRenderIn();
        }
    }
}
