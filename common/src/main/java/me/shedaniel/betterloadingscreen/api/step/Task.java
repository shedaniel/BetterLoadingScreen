package me.shedaniel.betterloadingscreen.api.step;

import me.shedaniel.betterloadingscreen.api.HasProgress;
import me.shedaniel.betterloadingscreen.api.MultiTask;
import me.shedaniel.betterloadingscreen.api.StatusIdentifier;
import org.jetbrains.annotations.Nullable;

public interface Task<T extends Task<T>> extends HasProgress {
    @Nullable
    MultiTask<?> getParent();
    
    StatusIdentifier<T> getIdentifier();
    
    boolean isActive();
    
    default boolean isCompleted() {
        return getProgress() >= 1.0F;
    }
    
    @Nullable
    default String getDescription() {
        return null;
    }
}
