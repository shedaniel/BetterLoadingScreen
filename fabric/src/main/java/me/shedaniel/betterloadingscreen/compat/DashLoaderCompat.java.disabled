package me.shedaniel.betterloadingscreen.compat;

import dev.quantumfusion.dashloader.def.api.hook.LoadCacheHook;
import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.Tasks;
import me.shedaniel.betterloadingscreen.impl.mixinstub.MinecraftStub;
import net.minecraft.client.Minecraft;

public class DashLoaderCompat implements LoadCacheHook {
	@Override
	public void loadCacheStart() {
		((MinecraftStub) Minecraft.getInstance()).moveRenderOut();
	}

	@Override
	public void loadCacheTask(StepTask task) {
		Tasks.MAIN.setSubTask(task);
	}

	@Override
	public void loadCacheEnd() {
		Tasks.MAIN.next();
		((MinecraftStub) Minecraft.getInstance()).moveRenderIn();
	}
}
