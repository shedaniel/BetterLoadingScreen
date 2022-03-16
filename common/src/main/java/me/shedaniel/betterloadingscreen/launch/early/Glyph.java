package me.shedaniel.betterloadingscreen.launch.early;

public interface Glyph {
    default float getLeft() {
        return this.getBearingX();
    }
    
    default float getBearingX() {
        return 0.0F;
    }
    
    default float getRight() {
        return this.getLeft() + (float) this.getPixelWidth() / this.getOversample();
    }
    
    default float getUp() {
        return this.getBearingY();
    }
    
    default float getDown() {
        return this.getUp() + (float) this.getPixelHeight() / this.getOversample();
    }
    
    float getOversample();
    
    int getPixelWidth();
    
    int getPixelHeight();
    
    float getAdvance();
    
    void upload(int id, int xOffset, int yOffset, Image out);
    
    boolean isColored();
    
    default float getBearingY() {
        return 3.0F;
    }
}
