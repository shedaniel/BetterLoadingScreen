package me.shedaniel.betterloadingscreen.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class MinecraftGraphicsImpl {
    public static ResourceManager createResourceManager(VanillaPackResources pack) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        try {
            Class<?> clazz = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_6861"));
            Constructor<?> constructor = clazz.getDeclaredConstructor(PackType.class, List.class);
            return (ResourceManager) constructor.newInstance(PackType.CLIENT_RESOURCES, Collections.singletonList(pack));
        } catch (ClassNotFoundException ignored) {
            try {
                Class<?> clazz = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_3304"));
                Constructor<?> constructor = clazz.getDeclaredConstructor(PackType.class);
                ResourceManager manager = (ResourceManager) constructor.newInstance(PackType.CLIENT_RESOURCES);
                Method add = clazz.getDeclaredMethod(resolver.mapMethodName("intermediary", "net.minecraft.class_3304", "method_14475",
                        "(Lnet/minecraft/class_3262;)V"), PackResources.class);
                add.setAccessible(true);
                add.invoke(manager, pack);
                return manager;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
