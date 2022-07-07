package me.shedaniel.betterloadingscreen.forge;

import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class BetterLoadingScreenForgeMessageHandler {
    public static final Logger LOGGER = LogManager.getLogger(BetterLoadingScreenForgeMessageHandler.class);
    private static boolean completed = false;
    
    public static void handle(String message) {
        LOGGER.info(message);
        if (completed) return;
        String scan = "Completed deep scan of ";
        if (message.startsWith(scan)) {
            LoadingModList modList = LoadingModList.get();
            if (!(Tasks.MAIN.getSubTask() instanceof StepTask task) || !task.getName().startsWith("Scanning Mods")) {
                StepTask task = new StepTask("Scanning Mods", modList.getModFiles().size());
                Tasks.MAIN.setSubTask(task);
            }
            StepTask task = (StepTask) Tasks.MAIN.getSubTask();
            System.out.println(message.substring(message.indexOf(scan) + scan.length()));
            try {
                int scanned = 0;
                Field field = ModFile.class.getDeclaredField("fileModFileScanData");
                field.setAccessible(true);
                for (ModFileInfo file : modList.getModFiles()) {
                    ModFile modFile = file.getFile();
                    Object scanData = field.get(modFile);
                    if (scanData != null) {
                        scanned++;
                    }
                }
                task.setCurrent(scanned);
            } catch (Exception e) {
                task.next();
            }
            if (task.getCurrent() == task.getTotal()) {
                Tasks.MAIN.next();
                completed = true;
            }
        }
    }
}
