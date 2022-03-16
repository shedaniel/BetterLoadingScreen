package me.shedaniel.betterloadingscreen.api;

import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.SimpleTask;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.api.step.Task;

import java.util.Collection;

public interface MultiTask<T extends MultiTask<T>> extends Task<T> {
    Collection<Task<?>> getTasks();
    
    ParentTask parent(StatusIdentifier<ParentTask> identifier);
    
    SteppedTask stepped(StatusIdentifier<SteppedTask> identifier);
    
    SimpleTask simple(StatusIdentifier<SimpleTask> identifier);
    
    NestedType getNestedType();
}
