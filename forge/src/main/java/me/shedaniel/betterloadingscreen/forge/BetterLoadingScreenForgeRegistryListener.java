package me.shedaniel.betterloadingscreen.forge;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryManager;

import java.lang.reflect.Field;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BetterLoadingScreen.MOD_ID,
                        value = Dist.CLIENT)
public class BetterLoadingScreenForgeRegistryListener {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void register(RegistryEvent.Register register) {
        SteppedTask content = LoadGameSteps.registeringContent();
        try {
            Field field = RegistryManager.class.getDeclaredField("registries");
            field.setAccessible(true);
            Map o = (Map) field.get(RegistryManager.ACTIVE);
            content.setTotalSteps(o.size());
            content.setCurrentStepInfo(register.getRegistry().getRegistryName().toString());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerPost(RegistryEvent.Register register) {
        SteppedTask content = LoadGameSteps.registeringContent();
        content.incrementStep();
    }
}
