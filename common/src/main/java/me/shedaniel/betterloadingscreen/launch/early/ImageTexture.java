package me.shedaniel.betterloadingscreen.launch.early;

import org.lwjgl.opengl.GL11;

public class ImageTexture implements Texture {
    private final int id;
    private final Image image;
    
    public ImageTexture(Image image) {
        this.id = GL11.glGenTextures();
        this.image = image;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void upload(boolean blur, boolean wrap) {
        image.upload(id, blur, wrap);
    }
}