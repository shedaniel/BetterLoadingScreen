package me.shedaniel.betterloadingscreen.api;

public class MathHelper {
    public static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }
    
    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }
    
    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }
}
