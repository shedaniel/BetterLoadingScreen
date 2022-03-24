package me.shedaniel.betterloadingscreen;

import me.shedaniel.betterloadingscreen.api.Job;
import me.shedaniel.betterloadingscreen.api.MultiTask;
import me.shedaniel.betterloadingscreen.api.NestedType;
import me.shedaniel.betterloadingscreen.api.render.ARGB32;
import me.shedaniel.betterloadingscreen.api.render.AbstractGraphics;
import me.shedaniel.betterloadingscreen.api.render.EarlyWindowHook;
import me.shedaniel.betterloadingscreen.api.step.LoadGameSteps;
import me.shedaniel.betterloadingscreen.api.step.Task;
import me.shedaniel.betterloadingscreen.launch.early.BackgroundRenderer;

import java.util.ArrayList;
import java.util.List;

public class BetterLoadingScreenClient {
    private static String hint;
    public static boolean inDev;
    public static BackgroundRenderer renderer;
    public static List<EarlyWindowHook> hooks = new ArrayList<>();
    
    public static void renderOverlay(AbstractGraphics graphics, int mouseX, int mouseY, float delta, float alpha) {
        int scaledWidth = graphics.getScaledWidth();
        int scaledHeight = graphics.getScaledHeight();
        double d = Math.min(scaledWidth * 0.75D, scaledHeight) * 0.25D;
        int progressBarWidth = (int) (d * 4.0D);
        int textColor = ARGB32.color(Math.round(alpha * 255.0F), ARGB32.red(renderer.getTextColor()), ARGB32.green(renderer.getTextColor()), ARGB32.blue(renderer.getTextColor()));
        
        // Render Memory Usage
        if (BetterLoadingScreen.CONFIG.rendersMemoryBar) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            if (alpha > 0.02F) {
                graphics.drawString("Memory Usage: " + usedMemory / 1024 / 1024 + "MB / " + runtime.maxMemory() / 1024 / 1024 + "MB", scaledWidth / 2 - progressBarWidth / 2, 15, textColor);
            }
            renderProgressBar(graphics, scaledWidth / 2, progressBarWidth, 25, usedMemory / (double) runtime.maxMemory(), alpha, renderer);
        }
        
        hint = null;
        
        Job job = LoadGameSteps.loadGame();
        int y = (int) (scaledHeight * 0.8325D) - 40;
        if (alpha > 0.02F) {
            graphics.drawString(job.toString(), scaledWidth / 2 - progressBarWidth / 2, y - 10, textColor);
        }
        renderProgressBar(graphics, scaledWidth / 2, progressBarWidth, y, job.getProgress(), alpha, renderer);
        handleMulti(graphics, textColor, alpha, scaledWidth / 2, progressBarWidth, y + 30, job, renderer);
        
        if (hint != null && alpha > 0.02F && BetterLoadingScreen.CONFIG.rendersHint) {
            graphics.drawString(hint, 2, 2, textColor);
        }
        
        if (alpha > 0.02F && inDev) {
            graphics.drawString(graphics.getClass().getSimpleName(), 2, graphics.getScaledHeight() - 9, textColor);
        }
    }
    
    private static void handleMulti(AbstractGraphics graphics, int textColor, float alpha, int centerX, int width, int y, MultiTask<?> job, BackgroundRenderer renderer) {
        boolean showAll = job.getNestedType() == NestedType.SHOW_ALL;
        int taskSize = 0;
        for (Task<?> task : job.getTasks()) {
            if (showAll || (task.isActive() && !task.isCompleted())) {
                taskSize++;
            }
        }
        int i = 0;
        int padding = 6;
        if (taskSize == 0) return;
        for (Task<?> task : job.getTasks()) {
            if (showAll || (task.isActive() && !task.isCompleted())) {
                int x = centerX - width / 2 + (width + 6) / taskSize * i++;
                if (alpha > 0.02F) {
                    graphics.drawString(task.toString(), x, y - 10, textColor);
                }
                int innerWidth = (width + 6) / taskSize - 6;
                _renderProgressBar(graphics, x, innerWidth, y, task.getProgress(), alpha, renderer);
                
                if (task.getDescription() != null && alpha > 0.02F) {
                    hint = task.getIdentifier().getId() + ": " + task.getDescription();
                }
                
                if (task instanceof MultiTask) {
                    handleMulti(graphics, textColor, alpha, x + innerWidth / 2, innerWidth, y + 30, (MultiTask<?>) task, renderer);
                }
            }
        }
    }
    
    private static void renderProgressBar(AbstractGraphics graphics, int centerX, int width, int y, double progress, float alpha, BackgroundRenderer renderer) {
        _renderProgressBar(graphics, centerX - width / 2, width, y, progress, alpha, renderer);
    }
    
    private static void _renderProgressBar(AbstractGraphics graphics, int x, int width, int y, double progress, float alpha, BackgroundRenderer renderer) {
        int progressWidth = (int) Math.round((double) (width - 2) * progress);
        int color = ARGB32.color(Math.round(alpha * ARGB32.alpha(renderer.getBarColor())),
                ARGB32.red(renderer.getBarColor()), ARGB32.green(renderer.getBarColor()), ARGB32.blue(renderer.getBarColor()));
        int borderColor = ARGB32.color(Math.round(alpha * ARGB32.alpha(renderer.getBarBorderColor())),
                ARGB32.red(renderer.getBarBorderColor()), ARGB32.green(renderer.getBarBorderColor()), ARGB32.blue(renderer.getBarBorderColor()));
        // top horizontal
        graphics.fill(x + 1, y, x + width - 1, y + 1, borderColor);
        
        // bottom horizontal
        graphics.fill(x + 1, y + 10, x + width - 1, y + 10 - 1, borderColor);
        
        // left vertical
        graphics.fill(x, y, x + 1, y + 10, borderColor);
        
        // right vertical
        graphics.fill(x + width, y, x + width - 1, y + 10, borderColor);
        
        // progress bar
        if (progressWidth > 0) {
            graphics.fill(x + 2, y + 2, x + progressWidth, y + 10 - 2, color);
        }
    }
}
