package me.shedaniel.betterloadingscreen.forge;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;

@Mod(BetterLoadingScreen.MOD_ID)
public class BetterLoadingScreenForge {
    public BetterLoadingScreenForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(
                () -> NetworkConstants.IGNORESERVERONLY,
                (s, b) -> true));
        if (FMLLoader.getDist() == Dist.CLIENT) {
            BetterLoadingScreen.init();
        }
    }
}
