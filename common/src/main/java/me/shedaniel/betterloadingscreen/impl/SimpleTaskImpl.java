package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.*;
import me.shedaniel.betterloadingscreen.api.step.SimpleTask;
import org.jetbrains.annotations.Nullable;

public class SimpleTaskImpl implements SimpleTask {
    private final MultiTask parent;
    private final StatusIdentifier<SimpleTask> identifier;
    private double progress;
    
    public SimpleTaskImpl(MultiTask parent, StatusIdentifier<SimpleTask> identifier) {
        this.parent = parent;
        this.identifier = identifier;
    }
    
    @Override
    public @Nullable MultiTask<?> getParent() {
        return parent;
    }
    
    @Override
    public StatusIdentifier<SimpleTask> getIdentifier() {
        return identifier;
    }
    
    @Override
    public void setProgress(double progress) {
        this.progress = progress;
        
        for (JobListener listener : JobManager.getInstance().getListeners()) {
            listener.onTaskProgressUpdate(this);
        }
    }
    
    @Override
    public double getProgress() {
        return MathHelper.clamp(progress, 0, 1);
    }
    
    @Override
    public boolean isActive() {
        return progress > 0;
    }
    
    @Override
    public String toString() {
        return getIdentifier().toString() + (progress > 0 && progress < 1 ? ": " + (int) (progress * 100) + "%" : "");
    }
}
