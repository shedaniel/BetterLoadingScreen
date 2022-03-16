package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.JobListener;
import me.shedaniel.betterloadingscreen.api.JobManager;
import me.shedaniel.betterloadingscreen.api.MultiTask;
import me.shedaniel.betterloadingscreen.api.StatusIdentifier;
import me.shedaniel.betterloadingscreen.api.step.ParentTask;
import me.shedaniel.betterloadingscreen.api.step.SimpleTask;
import me.shedaniel.betterloadingscreen.api.step.SteppedTask;
import me.shedaniel.betterloadingscreen.api.step.Task;

import java.util.Collection;
import java.util.Map;

public interface MultiTaskImpl<T extends MultiTask<T>> extends MultiTask<T> {
    Map<StatusIdentifier<?>, Task<?>> steps();
    
    @Override
    default Collection<Task<?>> getTasks() {
        return this.steps().values();
    }
    
    @Override
    default ParentTask parent(StatusIdentifier<ParentTask> identifier) {
        Task<?> job = this.steps().get(identifier);
        if (job == null) {
            this.steps().put(identifier, job = new ParentTaskImpl(this, identifier));
            for (JobListener listener : JobManager.getInstance().getListeners()) {
                listener.onTaskFirstSeen(job);
            }
        }
        return (ParentTask) job;
    }
    
    @Override
    default SteppedTask stepped(StatusIdentifier<SteppedTask> identifier) {
        Task<?> job = this.steps().get(identifier);
        if (job == null) {
            this.steps().put(identifier, job = new SteppedTaskImpl(this, identifier));
            for (JobListener listener : JobManager.getInstance().getListeners()) {
                listener.onTaskFirstSeen(job);
            }
        }
        return (SteppedTask) job;
    }
    
    @Override
    default SimpleTask simple(StatusIdentifier<SimpleTask> identifier) {
        Task<?> job = this.steps().get(identifier);
        if (job == null) {
            this.steps().put(identifier, job = new SimpleTaskImpl(this, identifier));
            for (JobListener listener : JobManager.getInstance().getListeners()) {
                listener.onTaskFirstSeen(job);
            }
        }
        return (SimpleTask) job;
    }
}
