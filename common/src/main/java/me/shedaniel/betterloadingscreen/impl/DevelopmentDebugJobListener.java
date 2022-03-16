package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.Job;
import me.shedaniel.betterloadingscreen.api.JobListener;
import me.shedaniel.betterloadingscreen.api.MultiTask;
import me.shedaniel.betterloadingscreen.api.StatusIdentifier;
import me.shedaniel.betterloadingscreen.api.step.Task;

import java.util.ArrayList;
import java.util.List;

public class DevelopmentDebugJobListener implements JobListener {
    @Override
    public void onJobFirstSeen(Job job) {
        System.out.println("First seen job: " + job.getIdentifier());
    }
    
    @Override
    public void onTaskFirstSeen(Task<?> task) {
        List<StatusIdentifier<?>> levels = new ArrayList<>();
        levels.add(task.getIdentifier());
        MultiTask parent = null;
        while ((parent = parent == null ? task.getParent() : ((Task<?>) parent).getParent()) != null) {
            if (parent instanceof Job job) {
                levels.add(job.getIdentifier());
                break;
            } else {
                levels.add(((Task<?>) parent).getIdentifier());
            }
        }
        
        System.out.print("First seen task: ");
        for (int i = levels.size() - 1; i >= 0; i--) {
            System.out.print(levels.get(i).getId());
            if (i > 0) {
                System.out.print(" > ");
            }
        }
        System.out.println();
    }
    
    @Override
    public void onTaskProgressUpdate(Task<?> task) {
        List<StatusIdentifier<?>> levels = new ArrayList<>();
        levels.add(task.getIdentifier());
        MultiTask parent = null;
        while ((parent = parent == null ? task.getParent() : ((Task<?>) parent).getParent()) != null) {
            if (parent instanceof Job job) {
                levels.add(job.getIdentifier());
                break;
            } else {
                levels.add(((Task<?>) parent).getIdentifier());
            }
        }
        
        System.out.print("Task update: ");
        for (int i = levels.size() - 1; i >= 0; i--) {
            System.out.print(levels.get(i).getId());
            if (i > 0) {
                System.out.print(" > ");
            }
        }
        System.out.println(" = " + task);
    }
}
