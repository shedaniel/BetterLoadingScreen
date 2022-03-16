package me.shedaniel.betterloadingscreen.api.step;

public interface SteppedTask extends Task<SteppedTask> {
    int getCurrentStep();
    
    void setCurrentStep(int currentStep);
    
    String getCurrentStepInfo();
    
    void setCurrentStepInfo(String info);
    
    int getTotalSteps();
    
    void setTotalSteps(int totalSteps);
    
    default void incrementStep() {
        this.incrementStep(1);
    }
    
    default void incrementStep(int amount) {
        this.setCurrentStep(this.getCurrentStep() + amount);
    }
}
