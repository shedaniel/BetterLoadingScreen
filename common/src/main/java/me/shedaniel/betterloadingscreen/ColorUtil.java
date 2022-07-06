package me.shedaniel.betterloadingscreen;

public class ColorUtil {
    public static int a(final int i) {
        return (i >>> 24);
    }
    
    public static int r(final int i) {
        return (i >> 16) & 0xFF;
    }
    
    public static int g(final int i) {
        return (i >> 8) & 0xFF;
    }
    
    public static int b(final int i) {
        return (i) & 0xFF;
    }
    
    public static float aF(final int i) {
        return a(i) / 255f;
    }
    
    public static float rF(final int i) {
        return r(i) / 255f;
    }
    
    public static float gF(final int i) {
        return g(i) / 255f;
    }
    
    public static float bF(final int i) {
        return b(i) / 255f;
    }
}
