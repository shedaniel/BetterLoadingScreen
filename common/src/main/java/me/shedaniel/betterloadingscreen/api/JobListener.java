package me.shedaniel.betterloadingscreen.api;

import me.shedaniel.betterloadingscreen.api.step.Task;

public interface JobListener {
    default void onJobFirstSeen(Job job) {}
    
    default void onTaskFirstSeen(Task<?> task) {}
    
    default void onTaskProgressUpdate(Task<?> task) {}
    
    default void onTaskInfoUpdate(Task<?> task) {}
}
