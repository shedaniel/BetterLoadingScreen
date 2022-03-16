package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.*;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import org.jetbrains.annotations.Nullable;

public class SteppedTaskImpl implements SteppedTask {
    private final MultiTask parent;
    private final StatusIdentifier<SteppedTask> identifier;
    private int currentStep;
    private int totalSteps;
    private String currentStepInfo;
    
    public SteppedTaskImpl(MultiTask parent, StatusIdentifier<SteppedTask> identifier) {
        this.parent = parent;
        this.identifier = identifier;
    }
    
    @Override
    public @Nullable MultiTask<?> getParent() {
        return parent;
    }
    
    @Override
    public StatusIdentifier<SteppedTask> getIdentifier() {
        return identifier;
    }
    
    @Override
    public int getCurrentStep() {
        synchronized (this) {
            return currentStep;
        }
    }
    
    @Override
    public void setCurrentStep(int currentStep) {
        synchronized (this) {
            this.currentStep = currentStep;
        }
        
        for (JobListener listener : JobManager.getInstance().getListeners()) {
            listener.onTaskProgressUpdate(this);
        }
    }
    
    @Override
    @Nullable
    public String getDescription() {
        synchronized (this) {
            return currentStepInfo;
        }
    }
    
    @Override
    public String getCurrentStepInfo() {
        synchronized (this) {
            return currentStepInfo;
        }
    }
    
    @Override
    public void setCurrentStepInfo(String info) {
        synchronized (this) {
            this.currentStepInfo = info;
        }
        
        for (JobListener listener : JobManager.getInstance().getListeners()) {
            listener.onTaskInfoUpdate(this);
        }
    }
    
    @Override
    public int getTotalSteps() {
        synchronized (this) {
            return totalSteps;
        }
    }
    
    @Override
    public void setTotalSteps(int totalSteps) {
        synchronized (this) {
            this.totalSteps = totalSteps;
        }
        
        for (JobListener listener : JobManager.getInstance().getListeners()) {
            listener.onTaskProgressUpdate(this);
        }
    }
    
    @Override
    public double getProgress() {
        if (totalSteps == 0) return 0;
        return MathHelper.clamp((double) currentStep / totalSteps, 0, 1);
    }
    
    @Override
    public boolean isActive() {
        return currentStepInfo != null || currentStep > 0 || totalSteps > 0;
    }
    
    @Override
    public String toString() {
        return getIdentifier().toString() + (isActive() ? " " + Math.min(currentStep, totalSteps) + " / " + totalSteps : "");
    }
}
