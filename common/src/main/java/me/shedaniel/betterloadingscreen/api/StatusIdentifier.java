package me.shedaniel.betterloadingscreen.api;

import me.shedaniel.betterloadingscreen.impl.StatusIdentifierImpl;

public interface StatusIdentifier<T> {
    static <T> StatusIdentifier<T> of(String id) {
        return new StatusIdentifierImpl<>(id);
    }
    
    String getId();
}
