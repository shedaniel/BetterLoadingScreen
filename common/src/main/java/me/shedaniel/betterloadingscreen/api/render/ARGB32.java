package me.shedaniel.betterloadingscreen.api.render;

public class ARGB32 {
    public static int alpha(int argb) {
        return argb >>> 24;
    }
    
    public static int red(int argb) {
        return argb >> 16 & 255;
    }
    
    public static int green(int argb) {
        return argb >> 8 & 255;
    }
    
    public static int blue(int argb) {
        return argb & 255;
    }
    
    public static int color(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    public static int multiply(int argb1, int argb2) {
        return color(alpha(argb1) * alpha(argb2) / 255, red(argb1) * red(argb2) / 255, green(argb1) * green(argb2) / 255, blue(argb1) * blue(argb2) / 255);
    }
}
