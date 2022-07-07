package me.shedaniel.betterloadingscreen.launch;

public class EarlyTimer {
    public float partialTick;
    public float tickDelta;
    private long lastMs;
    public final float msPerTick;
    
    public EarlyTimer(float f, long l) {
        this.msPerTick = 1000.0F / f;
        this.lastMs = l;
    }
    
    public int advanceTime(long l) {
        this.tickDelta = (float) (l - this.lastMs) / this.msPerTick;
        this.lastMs = l;
        this.partialTick += this.tickDelta;
        int i = (int) this.partialTick;
        this.partialTick -= (float) i;
        return i;
    }
}
