package me.shedaniel.betterloadingscreen.launch.early;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FontLoader {
    public static final Logger LOGGER = LogManager.getLogger(FontLoader.class);
    
    private Image image;
    private Int2ObjectMap<GlyphImpl> glyphs;
    private final String texture;
    private final List<int[]> chars;
    private final int height;
    private final int ascent;
    
    public FontLoader(String texture, int height, int ascent, List<int[]> chars, ResourceResolver resolver) {
        this.texture = texture;
        this.chars = chars;
        this.height = height;
        this.ascent = ascent;
        create(resolver.resolve(texture));
    }
    
    @Nullable
    public Glyph getGlyph(int i) {
        return this.glyphs.get(i);
    }
    
    public static FontLoader fromJson(JsonObject json, ResourceResolver resolver) {
        int height = getAsInt(json, "height", 8);
        int ascent = getAsInt(json, "ascent");
        if (ascent > height) {
            throw new JsonParseException("Ascent " + ascent + " higher than height " + height);
        } else {
            List<int[]> chars = Lists.newArrayList();
            JsonArray charsJson = getAsJsonArray(json, "chars");
            
            for (int index = 0; index < charsJson.size(); ++index) {
                int[] lineChars = convertToString(charsJson.get(index), "chars[" + index + "]").codePoints().toArray();
                if (index > 0) {
                    int firstLineChars = chars.get(0).length;
                    if (lineChars.length != firstLineChars) {
                        throw new JsonParseException("Elements of chars have to be the same length (found: " + lineChars.length +
                                                     ", expected: " + firstLineChars + "), pad with space or \\u0000");
                    }
                }
                
                chars.add(lineChars);
            }
            
            if (!chars.isEmpty() && chars.get(0).length != 0) {
                return new FontLoader(getAsString(json, "file"), height, ascent, chars, resolver);
            } else {
                throw new JsonParseException("Expected to find data in chars, found none.");
            }
        }
    }
    
    private static String convertToString(JsonElement json, String name) {
        if (json.isJsonPrimitive()) {
            return json.getAsString();
        } else {
            throw new JsonParseException("Expected " + name + " to be a string");
        }
    }
    
    private static String getAsString(JsonObject json, String name) {
        if (json.has(name)) {
            return json.getAsJsonPrimitive(name).getAsString();
        }
        
        throw new JsonParseException("Expected " + name + " to be present");
    }
    
    private static int getAsInt(JsonObject json, String name, int defaultValue) {
        if (json.has(name)) {
            return json.getAsJsonPrimitive(name).getAsInt();
        }
        
        return defaultValue;
    }
    
    private static int getAsInt(JsonObject json, String name) {
        if (json.has(name)) {
            return json.getAsJsonPrimitive(name).getAsInt();
        }
        
        throw new JsonParseException("Expected " + name + " to be present");
    }
    
    private static JsonArray getAsJsonArray(JsonObject json, String name) {
        if (json.has(name)) {
            return json.getAsJsonArray(name);
        }
        
        throw new JsonParseException("Expected " + name + " to be present");
    }
    
    public void create(InputStream stream) {
        try {
            Image image = Image.load(stream);
            int imageWidth = image.width;
            int imageHeight = image.height;
            int charWidth = imageWidth / this.chars.get(0).length;
            int charHeight = imageHeight / this.chars.size();
            float scale = (float) this.height / (float) charHeight;
            Int2ObjectMap<GlyphImpl> map = new Int2ObjectOpenHashMap<>();
            int m = 0;
            
            while (true) {
                if (m >= this.chars.size()) {
                    this.image = image;
                    this.glyphs = map;
                    return;
                }
                
                int n = 0;
                
                for (int o : this.chars.get(m)) {
                    int p = n++;
                    if (o != 0 && o != 32) {
                        int glyphWidth = this.getActualGlyphWidth(image, charWidth, charHeight, p, m);
                        GlyphImpl glyph = map.put(o, new GlyphImpl(scale, image, p * charWidth, m * charHeight, charWidth, charHeight, (int) (0.5D + (double) ((float) glyphWidth * scale)) + 1, this.ascent));
                        if (glyph != null) {
                            LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(o), this.texture);
                        }
                    }
                }
                
                ++m;
            }
        } catch (IOException var21) {
            throw new RuntimeException(var21.getMessage());
        }
    }
    
    private int getActualGlyphWidth(Image image, int i, int j, int k, int l) {
        int m;
        for (m = i - 1; m >= 0; --m) {
            int n = k * i + m;
            
            for (int o = 0; o < j; ++o) {
                int p = l * j + o;
                if (image.getAlpha(n, p) != 0) {
                    return m + 1;
                }
            }
        }
        
        return m + 1;
    }
    
    public static final class GlyphImpl implements Glyph {
        public final float scale;
        public final Image image;
        public final int offsetX;
        public final int offsetY;
        public final int width;
        public final int height;
        public final int advance;
        public final int ascent;
        
        private GlyphImpl(float scale, Image image, int offsetX, int offsetY, int width, int height, int advance, int ascent) {
            this.scale = scale;
            this.image = image;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            this.height = height;
            this.advance = advance;
            this.ascent = ascent;
        }
        
        @Override
        public float getOversample() {
            return 1.0F / this.scale;
        }
        
        @Override
        public int getPixelWidth() {
            return this.width;
        }
        
        @Override
        public int getPixelHeight() {
            return this.height;
        }
        
        @Override
        public float getAdvance() {
            return this.advance;
        }
        
        @Override
        public void upload(int id, int xOffset, int yOffset, Image out) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    out.setPixelRGBA(x + xOffset, y + yOffset, this.image.getPixelRGBA(x + offsetX, y + offsetY));
                }
            }
        }
        
        @Override
        public boolean isColored() {
            return this.image.channels > 1;
        }
        
        @Override
        public float getBearingY() {
            return 3.0F + 7.0F - (float) this.ascent;
        }
    }
}
