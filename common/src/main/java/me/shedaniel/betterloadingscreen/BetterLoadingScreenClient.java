package me.shedaniel.betterloadingscreen;


import dev.quantumfusion.taski.Task;
import dev.quantumfusion.taski.builtin.StageTask;
import dev.quantumfusion.taski.builtin.StepTask;
import me.shedaniel.betterloadingscreen.launch.early.BackgroundRenderer;

import java.util.ArrayList;
import java.util.List;

public class BetterLoadingScreenClient {
    public static final int PADDING = 6;
    
    private static String hint;
    public static boolean inDev;
    public static BackgroundRenderer renderer;
    public static List<EarlyWindowHook> hooks = new ArrayList<>();
    
    public static void renderOverlay(GraphicsBackend graphics, int mouseX, int mouseY, float delta, float alpha) {
        int scaledWidth = graphics.getScaledWidth();
        int scaledHeight = graphics.getScaledHeight();
        double d = Math.min(scaledWidth * 0.75D, scaledHeight) * 0.25D;
        int progressBarWidth = (int) (d * 4.0D);
        int textColor = ((int) (alpha * 255) << 24) | (0xffffff);
        
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
        
        int y = (int) (scaledHeight * 0.8325D) - 40;
        drawTask(graphics, textColor, alpha, (scaledWidth - progressBarWidth) / 2, progressBarWidth, y, Tasks.MAIN, renderer);
        
        if (hint != null && alpha > 0.02F && BetterLoadingScreen.CONFIG.rendersHint) {
            graphics.drawString(hint, 2, 2, textColor);
        }
        
        if (alpha > 0.02F && inDev) {
            graphics.drawString(graphics.getClass().getSimpleName(), 2, graphics.getScaledHeight() - 9, textColor);
        }
    }
    
    private static void drawTask(GraphicsBackend graphics, int textColor, float alpha, int x, int width, int y, Task task, BackgroundRenderer renderer) {
        if ((y + 10) > graphics.getScaledHeight()) {
            return;
        }
        
        if (alpha > 0.02F) {
            graphics.drawString(task.getName(), x, y - 10, textColor);
        }
        
        _renderProgressBar(graphics, x, width, y, task.getProgress(), alpha, renderer);
        if (task instanceof StepTask stepTask) {
            Task subTask = stepTask.getSubTask();
            if (subTask != null) {
                drawTask(graphics, textColor, alpha, x, width, y + 30, subTask, renderer);
            }
        } else if (task instanceof StageTask stageTask) {
            List<Task> stages = stageTask.getStages();
            int stageBarWidth = (width - (PADDING * (stages.size() - 1))) / stages.size();
            
            
            if (stages.size() > 2) {
                for (Task subTask : stages) {
                    if (!subTask.done()) {
                        drawTask(graphics, textColor, alpha, x, width, y + 30, subTask, renderer);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < stages.size(); i++) {
                    drawTask(graphics, textColor, alpha, x + ((stageBarWidth + PADDING) * i), stageBarWidth, y + 30, stages.get(i), renderer);
                }
            }
        }
    }
    
    private static void renderProgressBar(GraphicsBackend graphics, int centerX, int width, int y, double progress, float alpha, BackgroundRenderer renderer) {
        _renderProgressBar(graphics, centerX - width / 2, width, y, progress, alpha, renderer);
    }
    
    private static void _renderProgressBar(GraphicsBackend graphics, int x, int width, int y, double progress, float alpha, BackgroundRenderer renderer) {
        int progressWidth = (int) Math.round((double) (width - 2) * progress);
        int barColor = ((int) (alpha * 255) << 24) | (renderer.getBarColor() & 0x00ffffff);
        int barBorderColor = ((int) (alpha * 255) << 24) | (renderer.getBarBorderColor() & 0x00ffffff);
        // top horizontal
        graphics.fill(x + 1, y, x + width - 1, y + 1, barBorderColor);
        
        // bottom horizontal
        graphics.fill(x + 1, y + 10, x + width - 1, y + 10 - 1, barBorderColor);
        
        // left vertical
        graphics.fill(x, y, x + 1, y + 10, barBorderColor);
        
        // right vertical
        graphics.fill(x + width, y, x + width - 1, y + 10, barBorderColor);
        
        // progress bar
        if (progressWidth > 0) {
            graphics.fill(x + 2, y + 2, x + progressWidth, y + 10 - 2, barColor);
        }
    }
}
