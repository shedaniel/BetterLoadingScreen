package me.shedaniel.betterloadingscreen.forge;

import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class BetterLoadingScreenForgeMessageHandler {
    public static final Logger LOGGER = LogManager.getLogger(BetterLoadingScreenForgeMessageHandler.class);
    
    public static void handle(String message) {
        LOGGER.info(message);
        String scan = "Completed deep scan of ";
        if (message.startsWith(scan)) {
            SteppedTask task = LoadGameSteps.scanningMods();
            LoadingModList modList = LoadingModList.get();
            task.setTotalSteps(modList.getModFiles().size());
            task.setCurrentStepInfo(message.substring(message.indexOf(scan) + scan.length()));
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
                task.setCurrentStep(scanned);
            } catch (Exception e) {
                task.incrementStep();
            }
        }
    }
}
