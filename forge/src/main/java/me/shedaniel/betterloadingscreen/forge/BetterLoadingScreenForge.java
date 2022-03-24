package me.shedaniel.betterloadingscreen.forge;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(BetterLoadingScreen.MOD_ID)
public class BetterLoadingScreenForge {
    public BetterLoadingScreenForge() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY,
                (s, b) -> true));
        if (FMLLoader.getDist() == Dist.CLIENT) {
            BetterLoadingScreen.init();
        }
    }
}
