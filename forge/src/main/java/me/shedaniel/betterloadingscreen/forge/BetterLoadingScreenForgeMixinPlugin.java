package me.shedaniel.betterloadingscreen.forge;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.BetterLoadingScreenClient;
import me.shedaniel.betterloadingscreen.EarlyGraphics;
import me.shedaniel.betterloadingscreen.launch.BetterLoadingScreenPreInit;
import me.shedaniel.betterloadingscreen.launch.EarlyWindow;
import me.shedaniel.betterloadingscreen.launch.early.BackgroundRenderer;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.shedaniel.betterloadingscreen.BetterLoadingScreenCommonMixinPlugin.hasOptifine;

public class BetterLoadingScreenForgeMixinPlugin implements IMixinConfigPlugin {
    public static final Logger LOGGER = LogManager.getLogger(BetterLoadingScreenForgeMixinPlugin.class);
    
    static {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Client::run);
    }
    
    public static class Client {
        public static void run() {
        BetterLoadingScreenPreInit.init(false);
        EarlyGraphics.resolver = url -> {
            return Objects.requireNonNull(EarlyGraphics.class.getClassLoader().getResourceAsStream(url), "Resource not found: " + url);
        };
        BetterLoadingScreenClient.inDev = !FMLLoader.isProduction();
        
        BackgroundRenderer renderer = BackgroundRenderer.DEFAULT;
        
        if (BetterLoadingScreen.CONFIG.detectKubeJS && LoadingModList.get().getModFileById("kubejs") != null) {
            LOGGER.info("[BetterLoadingScreen] Detected KubeJS, inheriting KubeJS options!");
            renderer = Objects.requireNonNullElse(BackgroundRenderer.useKubeJs(FMLPaths.GAMEDIR.get()), renderer);
        }
        
        if (BetterLoadingScreen.CONFIG.detectAllTheTweaks && LoadingModList.get().getModFileById("allthetweaks") != null) {
            LOGGER.info("[BetterLoadingScreen] Detected All The Tweaks, inheriting colors and background!");
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("allthetweaks-common.toml");
            int backgroundIndex = 0;
            if (Files.exists(configPath)) {
                try {
                    boolean isPackMode = false;
                    for (String line : Files.readAllLines(configPath)) {
                        if (line.startsWith("[packmode]")) {
                            isPackMode = true;
                        } else if (line.startsWith("[")) {
                            isPackMode = false;
                        } else if (isPackMode) {
                            String s = line.trim().replace(" ", "");
                            if (s.startsWith("enable=")) {
                                backgroundIndex = Integer.parseInt(s.substring(7));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (backgroundIndex < 0 || backgroundIndex > 3) {
                backgroundIndex = 0;
            }
            ModFileInfo file = LoadingModList.get().getModFileById("allthetweaks");
            Path backgroundPath = file.getFile().findResource("assets/allthetweaks/textures/gui/title/background." + backgroundIndex + ".png");
            
            if (Files.exists(backgroundPath)) {
                renderer = BackgroundRenderer.wrapWithBackground(renderer, backgroundPath);
                LOGGER.info("[BetterLoadingScreen] Found background image for All The Tweaks: " + backgroundPath);
            } else {
                LOGGER.warn("[BetterLoadingScreen] Could not find background image for All The Tweaks: " + backgroundPath);
            }
            
            Path iconPath = file.getFile().findResource("assets/allthetweaks/textures/gui/title/mojangstudios.png");
            
            if (Files.exists(iconPath)) {
                renderer = BackgroundRenderer.wrapWithLogo(renderer, iconPath, 0xFFFFFFFF);
                LOGGER.info("[BetterLoadingScreen] Found logo image for All The Tweaks: " + iconPath);
            } else {
                LOGGER.warn("[BetterLoadingScreen] Could not find logo image for All The Tweaks: " + iconPath);
            }
        }
        
        if (Files.exists(BetterLoadingScreen.BACKGROUND_PATH)) {
            LOGGER.info("[BetterLoadingScreen] Detected background image, using it!");
            renderer = BackgroundRenderer.wrapWithBackground(renderer, BetterLoadingScreen.BACKGROUND_PATH);
        }
        
        BetterLoadingScreenClient.renderer = renderer;
        
        if (BetterLoadingScreen.isEarlyLoadingEnabled()) {
            EarlyWindow.start(BetterLoadingScreenForgeVisualization.extractRunArgs(), EarlyWindow.getDefaultFullscreen(FMLPaths.GAMEDIR.get()), FMLLoader.versionInfo().mcVersion(), renderer);
        }
        // StartupMessageManager
        Set<String> messages = new HashSet<>();
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    for (Pair<Integer, StartupMessageManager.Message> message : StartupMessageManager.getMessages()) {
                        if (messages.add(message.getValue().getText())) {
                            BetterLoadingScreenForgeMessageHandler.handle(message.getValue().getText());
                        }
                    }
                    Thread.sleep(5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        }
    }
    
    @Override
    public void onLoad(String s) {
        
    }
    
    @Override
    public String getRefMapperConfig() {
        return null;
    }
    
    @Override
    public boolean shouldApplyMixin(String s, String s1) {
        if (s1.endsWith("MixinWindow")) {
            return !hasOptifine();
        } else if (s1.endsWith("MixinWindowOptifine")) {
            return hasOptifine();
        }
        return true;
    }
    
    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {
        
    }
    
    @Override
    public List<String> getMixins() {
        return null;
    }
    
    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }
    
    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
        
    }
}
