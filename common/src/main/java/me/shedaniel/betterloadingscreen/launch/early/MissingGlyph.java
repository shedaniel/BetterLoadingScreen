package me.shedaniel.betterloadingscreen.launch.early;

public enum MissingGlyph implements Glyph {
    INSTANCE;
    
    private static final int MISSING_IMAGE_WIDTH = 5;
    private static final int MISSING_IMAGE_HEIGHT = 8;
    
    MissingGlyph() {
    }
    
    public int getPixelWidth() {
        return MISSING_IMAGE_WIDTH;
    }
    
    public int getPixelHeight() {
        return MISSING_IMAGE_HEIGHT;
    }
    
    public float getAdvance() {
        return 6.0F;
    }
    
    public float getOversample() {
        return 1.0F;
    }
    
    public void upload(int id, int xOffset, int yOffset, Image out) {
        for (int y = 0; y < MISSING_IMAGE_HEIGHT; ++y) {
            for (int x = 0; x < MISSING_IMAGE_WIDTH; ++x) {
                boolean colored = x == 0 || x + 1 == MISSING_IMAGE_WIDTH || y == 0 || y + 1 == MISSING_IMAGE_HEIGHT;
                out.setPixelRGBA(x + xOffset, y + yOffset, colored ? -1 : 0);
            }
        }
    }
    
    public boolean isColored() {
        return true;
    }
}
