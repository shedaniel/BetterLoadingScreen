package me.shedaniel.betterloadingscreen.launch.early;

import me.shedaniel.betterloadingscreen.BetterLoadingScreen;
import me.shedaniel.betterloadingscreen.BetterLoadingScreenConfig;
import me.shedaniel.betterloadingscreen.EarlyGraphics;
import me.shedaniel.betterloadingscreen.api.render.ARGB32;
import me.shedaniel.betterloadingscreen.api.render.AbstractGraphics;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public interface BackgroundRenderer {
    @Nullable
    static BackgroundRenderer useKubeJs(Path gameDir) {
        try {
            Path path = gameDir.resolve("kubejs/config/client.properties");
            if (Files.exists(path)) {
                Properties properties = new Properties();
                
                try (InputStream is = Files.newInputStream(path)) {
                    properties.load(is);
                }
                
                int bgColor = BetterLoadingScreenConfig.getColor(properties.getProperty("backgroundColor"), 0x2E3440) | 0xFF000000;
                int barColor = BetterLoadingScreenConfig.getColor(properties.getProperty("barColor"), 0xECEFF4) | 0xFF000000;
                int barBorderColor = BetterLoadingScreenConfig.getColor(properties.getProperty("barBorderColor"), 0xECEFF4) | 0xFF000000;
                
                return new BackgroundRenderer() {
                    @Override
                    public void render(AbstractGraphics graphics) {
                        GL11.glClearColor(ARGB32.red(bgColor) / 255.0F, ARGB32.green(bgColor) / 255.0F, ARGB32.blue(bgColor) / 255.0F, ARGB32.alpha(bgColor));
                    }
                    
                    @Override
                    public int getBarColor() {
                        return barColor;
                    }
                    
                    @Override
                    public int getTextColor() {
                        return barBorderColor;
                    }
                    
                    @Override
                    public int getBarBorderColor() {
                        return barBorderColor;
                    }
                    
                    @Override
                    public void renderLogo(int color) {
                        BackgroundRenderer.renderLogoDefault(color);
                    }
                };
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        
        return null;
    }
    
    BackgroundRenderer DEFAULT = new BackgroundRenderer() {
        @Override
        public void render(AbstractGraphics graphics) {
            int bgColor = BetterLoadingScreenConfig.getColor(BetterLoadingScreen.CONFIG.backgroundColor, 0x2E3440);
            GL11.glClearColor(ARGB32.red(bgColor) / 255.0F, ARGB32.green(bgColor) / 255.0F, ARGB32.blue(bgColor) / 255.0F, 1.0F);
        }
        
        @Override
        public int getBarColor() {
            return BetterLoadingScreenConfig.getColor(BetterLoadingScreen.CONFIG.barColor, 0xFFFFFF) | 0xFF000000;
        }
        
        @Override
        public int getTextColor() {
            return BetterLoadingScreenConfig.getColor(BetterLoadingScreen.CONFIG.textColor, 0xFFFFFF) | 0xFF000000;
        }
        
        @Override
        public int getBarBorderColor() {
            return BetterLoadingScreenConfig.getColor(BetterLoadingScreen.CONFIG.barFrameColor, 0xFFFFFF) | 0xFF000000;
        }
        
        @Override
        public void renderLogo(int color) {
            BackgroundRenderer.renderLogoDefault(color);
        }
    };
    
    static BackgroundRenderer wrapWithBackground(BackgroundRenderer parent, Path path) {
        return new BackgroundRenderer() {
            private Optional<String> background = Optional.empty();
            
            @Override
            public void render(AbstractGraphics graphics) {
                if (!background.isPresent()) {
                    String name = path.toString();
                    if (graphics.bindTextureCustom(name, () -> path)) {
                        background = Optional.of(name);
                    }
                }
                
                if (background.isPresent()) {
                    graphics.bindTexture(background.get());
                    graphics.innerBlit(0, graphics.getScaledWidth(), 0, graphics.getScaledHeight(), 0, 0, 1, 0, 1, 0xFFFFFFFF);
                }
            }
            
            @Override
            public int getBarColor() {
                return parent.getBarColor();
            }
            
            @Override
            public int getTextColor() {
                return parent.getTextColor();
            }
            
            @Override
            public int getBarBorderColor() {
                return parent.getBarBorderColor();
            }
            
            @Override
            public void renderLogo(int color) {
                parent.renderLogo(color);
            }
        };
    }
    
    static BackgroundRenderer wrapWithLogo(BackgroundRenderer parent, Path path, int color) {
        return new BackgroundRenderer() {
            @Override
            public void render(AbstractGraphics graphics) {
                parent.render(graphics);
            }
            
            @Override
            public int getBarColor() {
                return parent.getBarColor();
            }
            
            @Override
            public int getTextColor() {
                return parent.getTextColor();
            }
            
            @Override
            public int getBarBorderColor() {
                return parent.getBarBorderColor();
            }
            
            @Override
            public void renderLogo(int $) {
                BackgroundRenderer.renderLogo(color, path);
            }
        };
    }
    
    void render(AbstractGraphics graphics);
    
    int getBarColor();
    
    int getTextColor();
    
    int getBarBorderColor();
    
    void renderLogo(int color);
    
    static void renderLogoDefault(int color) {
        renderLogo(color, "/assets/minecraft/textures/gui/title/mojangstudios.png");
    }
    
    static void renderLogo(int color, Object name) {
        EarlyGraphics graphics = EarlyGraphics.INSTANCE;
        if (name instanceof String) {
            EarlyGraphics._bindTexture((String) name);
        } else if (name instanceof Path) {
            Path path = (Path) name;
            if (!graphics.bindTextureCustom(path.toString(), () -> path)) {
                throw new RuntimeException("Failed to bind texture: " + path);
            }
        } else {
            throw new IllegalArgumentException("name must be a string or a path");
        }
        int n = (int) (graphics.getScaledWidth() * 0.5D);
        int s = (int) (graphics.getScaledHeight() * 0.5D);
        double d = Math.min(graphics.getScaledWidth() * 0.75D, graphics.getScaledHeight()) * 0.25D;
        int t = (int) (d * 0.5D);
        double e = d * 4.0D;
        int u = (int) (e * 0.5D);
        s -= 20;
        graphics.blit(n - u, s - t, u, (int) d, -0.0625F, 0.0F, 120, 60, 120, 120, color);
        graphics.blit(n, s - t, u, (int) d, 0.0625F, 60.0F, 120, 60, 120, 120, color);
    }
}
