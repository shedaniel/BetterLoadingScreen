package me.shedaniel.betterloadingscreen.launch.early;

import me.shedaniel.betterloadingscreen.EarlyGraphics;
import org.lwjgl.opengl.GL11;

public class BakedGlyph {
    public final String texture;
    public final float u0;
    public final float u1;
    public final float v0;
    public final float v1;
    public final float left;
    public final float right;
    public final float up;
    public final float down;
    
    public BakedGlyph(String texture, float u0, float u1, float v0, float v1, float left, float right, float up, float down) {
        this.texture = texture;
        this.u0 = u0;
        this.u1 = u1;
        this.v0 = v0;
        this.v1 = v1;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }
    
    public void render(boolean italic, float x, float y, float r, float g, float b, float a) {
        float x1 = x + this.left;
        float x2 = x + this.right;
        float p = this.up - 3.0F;
        float q = this.down - 3.0F;
        float y1 = y + p;
        float y2 = y + q;
        float xo1 = italic ? 1.0F - 0.25F * p : 0.0F;
        float xo2 = italic ? 1.0F - 0.25F * q : 0.0F;
    
        EarlyGraphics._bindTexture(texture);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(u0, v0);
        GL11.glVertex3f(x1 + xo1, y2, 0.0F);

        GL11.glTexCoord2f(u0, v1);
        GL11.glVertex3f(x1 + xo2, y1, 0.0F);

        GL11.glTexCoord2f(u1, v1);
        GL11.glVertex3f(x2 + xo2, y1, 0.0F);

        GL11.glTexCoord2f(u1, v0);
        GL11.glVertex3f(x2 + xo1, y2, 0.0F);

        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
}