package me.shedaniel.betterloadingscreen.launch.early;

public interface Texture {
    int getId();
    
    void upload(boolean blur, boolean wrap);
}