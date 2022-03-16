package me.shedaniel.betterloadingscreen.api.step;

import me.shedaniel.betterloadingscreen.api.MultiTask;
import me.shedaniel.betterloadingscreen.api.NestedType;

public interface ParentTask extends MultiTask<ParentTask> {
    void setNestedType(NestedType nestedType);
}
