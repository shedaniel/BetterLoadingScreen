package me.shedaniel.betterloadingscreen.api;

import me.shedaniel.betterloadingscreen.impl.Internals;

import java.util.List;
import java.util.Objects;

public interface JobManager {
    static JobManager getInstance() {
        return Objects.requireNonNull(Internals.manager);
    }
    
    void addListener(JobListener listener);
    
    List<JobListener> getListeners();
    
    Job get(StatusIdentifier<Job> identifier);
}
