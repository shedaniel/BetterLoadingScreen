package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.Job;
import me.shedaniel.betterloadingscreen.api.JobListener;
import me.shedaniel.betterloadingscreen.api.JobManager;
import me.shedaniel.betterloadingscreen.api.StatusIdentifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JobManagerImpl implements JobManager {
    private final List<JobListener> listeners = new ArrayList<>();
    private final Map<StatusIdentifier<Job>, Job> jobs = new LinkedHashMap<>();
    
    public JobManagerImpl() {
//        addListener(new DevelopmentDebugJobListener());
    }
    
    @Override
    public void addListener(JobListener listener) {
        this.listeners.add(listener);
    }
    
    @Override
    public List<JobListener> getListeners() {
        return listeners;
    }
    
    @Override
    public Job get(StatusIdentifier<Job> identifier) {
        Job job = this.jobs.get(identifier);
        if (job == null) {
            this.jobs.put(identifier, job = new JobImpl(identifier));
            for (JobListener listener : this.listeners) {
                listener.onJobFirstSeen(job);
            }
        }
        return job;
    }
    
    public void freeze() {
    }
}
