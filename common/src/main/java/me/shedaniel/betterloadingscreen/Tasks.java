package me.shedaniel.betterloadingscreen;

import dev.quantumfusion.taski.builtin.StepTask;

public class Tasks {
	public static int CLIENT_TASK_COUNT = 2;
	public static int LAUNCH_COUNT = CLIENT_TASK_COUNT + 2;

	// Bootstrap
	// ModLoading
	// - Common
	// - Client
	// Loading Assets
	// * Blocks
	// * Items
	// * Textures
	// Compiling Assets
	// - Stitching
	// - Baking
	public static StepTask MAIN = new StepTask("Loading Game");

}
