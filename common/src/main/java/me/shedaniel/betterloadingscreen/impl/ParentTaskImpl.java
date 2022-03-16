package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.MathHelper;
import me.shedaniel.betterloadingscreen.api.MultiTask;
import me.shedaniel.betterloadingscreen.api.NestedType;
import me.shedaniel.betterloadingscreen.api.StatusIdentifier;
import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.Task;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParentTaskImpl implements ParentTask, MultiTaskImpl<ParentTask> {
    private final MultiTask<?> parent;
    private final StatusIdentifier<ParentTask> identifier;
    protected Map<StatusIdentifier<?>, Task<?>> steps = new LinkedHashMap<>();
    protected NestedType nestedType = NestedType.SHOW_ALL;
    
    public ParentTaskImpl(MultiTask<?> parent, StatusIdentifier<ParentTask> identifier) {
        this.parent = parent;
        this.identifier = identifier;
    }
    
    @Override
    public @Nullable MultiTask<?> getParent() {
        return parent;
    }
    
    @Override
    public StatusIdentifier<ParentTask> getIdentifier() {
        return identifier;
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
    public boolean isActive() {
        for (Task<?> task : steps.values()) {
            if (task.isActive()) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Map<StatusIdentifier<?>, Task<?>> steps() {
        return steps;
    }
    
    @Override
    public NestedType getNestedType() {
        return nestedType;
    }
    
    @Override
    public void setNestedType(NestedType nestedType) {
        this.nestedType = nestedType;
    }
    
    @Override
    public String toString() {
        int completed = 1;
        for (Task<?> task : steps.values()) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        return getIdentifier().toString() + (isActive() ? " " + Math.min(completed, steps.size()) + " / " + steps.size() : "");
    }
}
