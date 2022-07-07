package me.shedaniel.betterloadingscreen.forge;

import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.Tasks;
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
    private static boolean completed = false;
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void register(RegistryEvent.Register register) {
        init();
        System.out.println(register.getRegistry().getRegistryName().toString());
    }
    
    public static void init() {
        if (completed) return;
        try {
            Field field = RegistryManager.class.getDeclaredField("registries");
            field.setAccessible(true);
            Tasks.LOCK.lock();
            if (!(Tasks.MAIN.getSubTask() instanceof StepTask task) || !task.getName().startsWith("Registering Content")) {
                Map o = (Map) field.get(RegistryManager.ACTIVE);
                StepTask task = new StepTask("Registering Content", o.size());
                Tasks.MAIN.setSubTask(task);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            Tasks.LOCK.unlock();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerPost(RegistryEvent.Register register) {
        if (completed) return;
        Tasks.LOCK.lock();
        try {
            StepTask subTask = (StepTask) Tasks.MAIN.getSubTask();
            subTask.next();
            if (subTask.getCurrent() == subTask.getTotal()) {
                Tasks.MAIN.next();
                completed = true;
            }
        } finally {
            Tasks.LOCK.unlock();
        }
    }
}