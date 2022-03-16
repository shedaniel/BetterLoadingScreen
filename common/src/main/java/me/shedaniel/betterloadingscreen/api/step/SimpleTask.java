package me.shedaniel.betterloadingscreen.api.step;

public interface SimpleTask extends Task<SimpleTask> {
    default void setCompleted(boolean completed) {
        setProgress(completed ? 1.0 : 0.0);
    }
    
    void setProgress(double progress);
}
