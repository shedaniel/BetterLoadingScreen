package me.shedaniel.betterloadingscreen.impl;

import me.shedaniel.betterloadingscreen.api.StatusIdentifier;

import java.util.Objects;

public class StatusIdentifierImpl<T> implements StatusIdentifier<T> {
    private final String id;
    
    public StatusIdentifierImpl(String id) {
        this.id = Objects.requireNonNull(id);
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusIdentifierImpl)) return false;
        StatusIdentifierImpl<T> that = (StatusIdentifierImpl<T>) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
