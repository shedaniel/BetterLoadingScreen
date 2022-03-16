package me.shedaniel.betterloadingscreen.launch.early;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Image implements Closeable {
    public static final Logger LOGGER = LogManager.getLogger(Image.class);
    
    public int width;
    public int height;
    public int channels;
    public long pixels;
    
    public Image(int width, int height) {
        this(width, height, 4);
    }
    
    public Image(int width, int height, int channels) {
        this(width, height, channels, MemoryUtil.nmemAlloc(width * height * channels));
    }
    
    public Image(int width, int height, int channels, long pixels) {
        this.width = width;
        this.height = height;
        this.channels = channels;
        this.pixels = pixels;
    }
    
    @Override
    public void close() {
        if (this.pixels != 0L) {
            STBImage.nstbi_image_free(this.pixels);
        }
        
        this.pixels = 0L;
    }
    
    public static void prepareImage(int id, int level, int width, int height, int glFormat) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        
        if (level >= 0) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, level);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, level);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
        }
        
        for (int i = 0; i <= level; ++i) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, glFormat, width >> i, height >> i, 0, 6408, 5121, (IntBuffer) null);
        }
    }
    
    private static void setFilter(boolean blur, boolean mipmap) {
        if (blur) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10241, mipmap ? 9987 : 9729);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10240, 9729);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10241, mipmap ? 9986 : 9728);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10240, 9728);
        }
    }
    
    public void upload(int id, boolean blur, boolean wrap) {
        upload(id, 0, 0, 0, 0, 0, width, height, blur, wrap, false, false);
    }
    
    public void upload(int id, int level, int xOffset, int yOffset, boolean close) {
        this.upload(id, level, xOffset, yOffset, 0, 0, this.width, this.height, false, close);
    }
    
    public void upload(int id, int level, int xOffset, int yOffset, int skipPixels, int skipRows, int width, int height, boolean mipmap, boolean close) {
        this.upload(id, level, xOffset, yOffset, skipPixels, skipRows, width, height, false, false, mipmap, close);
    }
    
    public void upload(int id, int level, int xOffset, int yOffset, int skipPixels, int skipRows, int width, int height, boolean blur, boolean wrap, boolean mipmap, boolean close) {
        prepareImage(id, 0, width, height, getGlFormat());
        setFilter(blur, mipmap);
        if (width == width) {
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        } else {
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, width);
        }
        
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, skipPixels);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, skipRows);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, channels);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, xOffset, yOffset, width, height, getGlFormat(), GL11.GL_UNSIGNED_BYTE, pixels);
        if (wrap) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        
        if (close) {
            close();
        }
    }
    
    public int getGlFormat() {
        return channels == 2 ? GL30.GL_RG : GL11.GL_RGBA;
    }
    
    private boolean writeToChannel(WritableByteChannel writableByteChannel) throws IOException {
        class WriteCallback extends STBIWriteCallback {
            private final WritableByteChannel output;
            @Nullable
            private IOException exception;
            
            WriteCallback(WritableByteChannel writableByteChannel) {
                this.output = writableByteChannel;
            }
            
            @Override
            public void invoke(long l, long m, int i) {
                ByteBuffer byteBuffer = getData(m, i);
                
                try {
                    this.output.write(byteBuffer);
                } catch (IOException var8) {
                    this.exception = var8;
                }
                
            }
            
            public void throwIfException() throws IOException {
                if (this.exception != null) {
                    throw this.exception;
                }
            }
        }
        
        WriteCallback writeCallback = new WriteCallback(writableByteChannel);
        
        boolean var4;
        try {
            int height = Math.min(this.height, 2147483647 / width / channels);
            if (height < this.height) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.height, height);
            }
            
            if (STBImageWrite.nstbi_write_png_to_func(writeCallback.address(), 0L, width, height, channels, this.pixels, 0) == 0) {
                var4 = false;
                return var4;
            }
            
            writeCallback.throwIfException();
            var4 = true;
        } finally {
            writeCallback.free();
        }
        
        return var4;
    }
    
    public void writeToFile(Path path) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            if (!this.writeToChannel(channel)) {
                throw new IOException("Could not write image to the PNG file \"" + path.normalize().toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
        }
    }
    
    public static Image load(InputStream stream) throws IOException {
        ByteBuffer buffer = MemoryUtil.memAlloc(8192);
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            IntBuffer width = memoryStack.mallocInt(1);
            IntBuffer height = memoryStack.mallocInt(1);
            IntBuffer channels = memoryStack.mallocInt(1);
            ReadableByteChannel byteChannel = Channels.newChannel(stream);
            
            while (byteChannel.read(buffer) != -1) {
                if (buffer.remaining() == 0) {
                    buffer = MemoryUtil.memRealloc(buffer, buffer.capacity() * 2);
                }
            }
            
            buffer.rewind();
            
            ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
            if (byteBuffer2 == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            
            return new Image(width.get(0), height.get(0), 4, MemoryUtil.memAddress(byteBuffer2));
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }
    
    public boolean supportsLuminance() {
        return this.channels == 2;
    }
    
    public byte getLuminanceOrAlpha(long x, long y) {
        return supportsLuminance() ? getLuminance(x, y) : getAlpha(x, y);
    }
    
    public byte getAlpha(long x, long y) {
        long offset = (x + y * this.width) * this.channels + (channels == 2 ? 8 : 24) / 8;
        return MemoryUtil.memGetByte(this.pixels + offset);
    }
    
    public byte getLuminance(long x, long y) {
        if (!supportsLuminance()) {
            throw new IllegalStateException("Image does not support luminance: " + this.channels);
        }
        long offset = (x + y * this.width) * this.channels;
        return MemoryUtil.memGetByte(this.pixels + offset);
    }
    
    public int getPixelRGBA(long x, long y) {
        long offset = (x + y * this.width) * this.channels;
        return MemoryUtil.memGetInt(this.pixels + offset);
    }
    
    public void setPixelRGBA(long x, long y, int color) {
        long offset = (x + y * this.width) * this.channels;
        MemoryUtil.memPutInt(this.pixels + offset, color);
    }
}