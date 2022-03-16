package me.shedaniel.betterloadingscreen.launch.early;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class FontTexture implements Texture {
    private static final int SIZE = 256;
    private final String name;
    private final boolean colored;
    private final Node root;
    private int id;
    private Image image;
    
    public FontTexture(String name, boolean colored) {
        this.name = name;
        this.colored = colored;
        this.root = new Node(0, 0, 256, 256);
        this.id = GL11.glGenTextures();
        this.image = new Image(256, 256);
    }
    
    public void close() {
        GL11.glDeleteTextures(this.id);
        id = -1;
    }
    
    @Nullable
    public BakedGlyph add(Glyph rawGlyph) {
        if (rawGlyph.isColored() != this.colored) {
            return null;
        } else {
            FontTexture.Node node = this.root.insert(rawGlyph);
            if (node != null) {
                rawGlyph.upload(getId(), node.x, node.y, image);
                upload(false, true);
                return new BakedGlyph(name, ((float) node.x + 0.01F) / 256.0F,
                        ((float) node.x - 0.01F + (float) rawGlyph.getPixelWidth()) / 256.0F, ((float) node.y + 0.01F) / 256.0F, ((float) node.y - 0.01F + (float) rawGlyph.getPixelHeight()) / 256.0F, rawGlyph.getLeft(), rawGlyph.getRight(), rawGlyph.getUp(), rawGlyph.getDown());
            } else {
                return null;
            }
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void upload(boolean blur, boolean wrap) {
        image.upload(id, blur, wrap);
    }
    
    @Environment(EnvType.CLIENT)
    static class Node {
        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private FontTexture.Node left;
        @Nullable
        private FontTexture.Node right;
        private boolean occupied;
        
        Node(int i, int j, int k, int l) {
            this.x = i;
            this.y = j;
            this.width = k;
            this.height = l;
        }
        
        @Nullable
        FontTexture.Node insert(Glyph rawGlyph) {
            if (this.left != null && this.right != null) {
                FontTexture.Node node = this.left.insert(rawGlyph);
                if (node == null) {
                    node = this.right.insert(rawGlyph);
                }
                
                return node;
            } else if (this.occupied) {
                return null;
            } else {
                int i = rawGlyph.getPixelWidth();
                int j = rawGlyph.getPixelHeight();
                if (i <= this.width && j <= this.height) {
                    if (i == this.width && j == this.height) {
                        this.occupied = true;
                        return this;
                    } else {
                        int k = this.width - i;
                        int l = this.height - j;
                        if (k > l) {
                            this.left = new FontTexture.Node(this.x, this.y, i, this.height);
                            this.right = new FontTexture.Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
                        } else {
                            this.left = new FontTexture.Node(this.x, this.y, this.width, j);
                            this.right = new FontTexture.Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
                        }
                        
                        return this.left.insert(rawGlyph);
                    }
                } else {
                    return null;
                }
            }
        }
    }
}
