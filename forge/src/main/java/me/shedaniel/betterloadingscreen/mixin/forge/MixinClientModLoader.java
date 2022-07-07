package me.shedaniel.betterloadingscreen.mixin.forge;

import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(ClientModLoader.class)
public class MixinClientModLoader {
    @Inject(method = "<clinit>", remap = false, at = @At(
            value = "RETURN"
    ))
    private static void init(CallbackInfo info) {
        try {
            Field field = ModLoader.class.getDeclaredField("statusConsumer");
            field.setAccessible(true);
            Optional<Consumer<String>> consumer = (Optional<Consumer<String>>) field.get(ModLoader.get());
            field.set(ModLoader.get(), Optional.<Consumer<String>>of((s) -> {
                if (consumer.isPresent()) {
                    consumer.get().accept(s);
                    if ("Dispatching gathering events".equals(s)) {
                        constructionTasks();
                    } else if (s.startsWith("Processing transition COMMON_SETUP")) {
                        loadTasks();
                    } else if (s.startsWith("Processing transition SIDED_SETUP")) {
                        loadSidedTasks();
                    }
                }
            }));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    @Unique
    private static void constructionTasks() {
        try {
            int[] count = {0};
            StepTask constructionTask = new StepTask("Loading Mods");
            Tasks.MAIN.setSubTask(constructionTask);
            Field activityMap = ModContainer.class.getDeclaredField("activityMap");
            activityMap.setAccessible(true);
            ModList modList = ModList.get();
            for (IModInfo mod : modList.getMods()) {
                modList.getModContainerById(mod.getModId()).ifPresent(container -> {
                    count[0]++;
                    try {
                        Map<ModLoadingStage, Runnable> map = (Map<ModLoadingStage, Runnable>) activityMap.get(container);
                        Runnable construct = map.getOrDefault(ModLoadingStage.CONSTRUCT, () -> {});
                        Runnable commonSetup = map.getOrDefault(ModLoadingStage.COMMON_SETUP, () -> {});
                        Runnable sidedSetup = map.getOrDefault(ModLoadingStage.SIDED_SETUP, () -> {});
                        map.put(ModLoadingStage.CONSTRUCT, () -> {
                            System.out.println(Thread.currentThread().getName());
                            System.out.println("Constructing: " + mod.getDisplayName());
                            try {
                                construct.run();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                throw throwable;
                            } finally {
                                constructionTask.next();
                                if (constructionTask.getCurrent() == constructionTask.getTotal()) {
                                    Tasks.MAIN.next();
                                    System.out.println("Done with mods loading!");
                                }
                            }
                        });
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }
            constructionTask.reset(count[0]);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    @Unique
    private static void loadTasks() {
        try {
            int[] count = {0};
            Set<String> commonMod = new HashSet<>();
            StepTask commonInitTask = new StepTask("Initialising Common Mods");
            Field activityMap = ModContainer.class.getDeclaredField("activityMap");
            activityMap.setAccessible(true);
            ModList modList = ModList.get();
            for (IModInfo mod : modList.getMods()) {
                modList.getModContainerById(mod.getModId()).ifPresent(container -> {
                    count[0]++;
                    try {
                        Map<ModLoadingStage, Runnable> map = (Map<ModLoadingStage, Runnable>) activityMap.get(container);
                        Runnable commonSetup = map.getOrDefault(ModLoadingStage.COMMON_SETUP, () -> {});
                        map.put(ModLoadingStage.COMMON_SETUP, () -> {
                            if (!commonMod.contains(mod.getModId())) {
                                System.out.println(Thread.currentThread().getName());
                                System.out.println("Common Setup: " + mod.getDisplayName());
                                Tasks.MAIN.setSubTask(commonInitTask);
                            }
                            try {
                                commonSetup.run();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                throw throwable;
                            } finally {
                                if (commonMod.add(mod.getModId())) {
                                    commonInitTask.next();
                                    
                                    if (commonInitTask.getCurrent() == commonInitTask.getTotal()) {
                                        System.out.println("Done with mods common setup!");
                                        Tasks.MAIN.next();
                                    }
                                }
                            }
                        });
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }
            commonInitTask.reset(count[0]);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    @Unique
    private static void loadSidedTasks() {
        try {
            int[] count = {0};
            Set<String> sidedMod = new HashSet<>();
            StepTask sidedInitTask = new StepTask("Initialising Sided Mods");
            Field activityMap = ModContainer.class.getDeclaredField("activityMap");
            activityMap.setAccessible(true);
            ModList modList = ModList.get();
            for (IModInfo mod : modList.getMods()) {
                modList.getModContainerById(mod.getModId()).ifPresent(container -> {
                    count[0]++;
                    try {
                        Map<ModLoadingStage, Runnable> map = (Map<ModLoadingStage, Runnable>) activityMap.get(container);
                        Runnable sidedSetup = map.getOrDefault(ModLoadingStage.SIDED_SETUP, () -> {});
                        map.put(ModLoadingStage.SIDED_SETUP, () -> {
                            if (!sidedMod.contains(mod.getModId())) {
                                System.out.println(Thread.currentThread().getName());
                                System.out.println("Sided Setup: " + mod.getDisplayName());
                                Tasks.MAIN.setSubTask(sidedInitTask);
                            }
                            try {
                                sidedSetup.run();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                throw throwable;
                            } finally {
                                if (sidedMod.add(mod.getModId())) {
                                    sidedInitTask.next();
                                    
                                    if (sidedInitTask.getCurrent() == sidedInitTask.getTotal()) {
                                        System.out.println("Done with mods sided setup!");
                                        Tasks.MAIN.next();
                                    }
                                }
                            }
                        });
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }
            sidedInitTask.reset(count[0]);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    @Inject(method = "begin", remap = false, at = @At(
            value = "HEAD"
    ))
    private static void begin(Minecraft minecraft, PackRepository defaultResourcePacks, ReloadableResourceManager mcResourceManager, ClientPackSource metadataSerializer, CallbackInfo info) {
        MinecraftStub stub = (MinecraftStub) minecraft;
        stub.moveRenderOut();
    }
    
    @Inject(method = "begin", remap = false, at = @At(
            value = "RETURN"
    ))
    private static void endBegin(Minecraft minecraft, PackRepository defaultResourcePacks, ReloadableResourceManager mcResourceManager, ClientPackSource metadataSerializer, CallbackInfo info) {
        MinecraftStub stub = (MinecraftStub) minecraft;
        stub.moveRenderIn();
    }
}
