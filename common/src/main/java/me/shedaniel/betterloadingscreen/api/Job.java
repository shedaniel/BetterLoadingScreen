package me.shedaniel.betterloadingscreen.api;

import me.shedaniel.betterloadingscreen.api.step.Task;

public interface Job extends Task<Job>, MultiTask<Job> {
}
