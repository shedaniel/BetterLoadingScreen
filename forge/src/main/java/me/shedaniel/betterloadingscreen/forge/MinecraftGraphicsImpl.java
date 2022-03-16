package me.shedaniel.betterloadingscreen.forge;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class MinecraftGraphicsImpl {
    public static ResourceManager createResourceManager(VanillaPackResources pack) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.server.packs.resources.MultiPackResourceManager");
            Constructor<?> constructor = clazz.getDeclaredConstructor(PackType.class, List.class);
            return (ResourceManager) constructor.newInstance(PackType.CLIENT_RESOURCES, Collections.singletonList(pack));
        } catch (ClassNotFoundException ignored) {
            try {
                Class<?> clazz = Class.forName("net.minecraft.server.packs.resources.SimpleReloadableResourceManager");
                Constructor<?> constructor = clazz.getDeclaredConstructor(PackType.class);
                ResourceManager manager = (ResourceManager) constructor.newInstance(PackType.CLIENT_RESOURCES);
                Method add = ObfuscationReflectionHelper.findMethod(clazz, "m_10880_", PackResources.class);
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
