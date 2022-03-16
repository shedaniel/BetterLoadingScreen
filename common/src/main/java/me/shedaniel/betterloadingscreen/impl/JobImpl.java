package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.*;
import me.shedaniel.betterloadingscreen.api.step.Task;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class JobImpl implements Job, MultiTaskImpl<Job> {
    private final StatusIdentifier<Job> identifier;
    protected Map<StatusIdentifier<?>, Task<?>> steps = new LinkedHashMap<>();
    
    public JobImpl(StatusIdentifier<Job> identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public @Nullable MultiTask<?> getParent() {
        return null;
    }
    
    @Override
    public StatusIdentifier<Job> getIdentifier() {
        return identifier;
    }
    
    @Override
    public boolean isActive() {
        for (Task<?> task : steps.values()) {
            if (task.isActive()) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public double getProgress() {
        if (steps.isEmpty()) return 0;
        double sum = 0;
        for (Task<?> task : steps.values()) {
            sum += task.getProgress();
        }
        return MathHelper.clamp(sum / steps.size(), 0, 1);
    }
    
    @Override
    public Map<StatusIdentifier<?>, Task<?>> steps() {
        return steps;
    }
    
    @Override
    public NestedType getNestedType() {
        return NestedType.SHOW_ACTIVE;
    }
    
    @Override
    public String toString() {
        int completed = 1;
        for (Task<?> task : steps.values()) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        return getIdentifier().toString() + " " + Math.min(completed, steps.size()) + " / " + steps.size();
    }
}
