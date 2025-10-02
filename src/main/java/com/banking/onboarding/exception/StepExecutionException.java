package com.banking.onboarding.exception;

/**
 * Exception for step execution errors
 */
public class StepExecutionException extends BusinessException {
    private final String stepName;
    private final String processId;
    
    public StepExecutionException(String message, String stepName) {
        super(message, "STEP_EXECUTION_ERROR");
        this.stepName = stepName;
        this.processId = null;
    }
    
    public StepExecutionException(String message, String stepName, String processId) {
        super(message, "STEP_EXECUTION_ERROR");
        this.stepName = stepName;
        this.processId = processId;
    }
    
    public StepExecutionException(String message, String stepName, String processId, String correlationId) {
        super(message, "STEP_EXECUTION_ERROR", correlationId);
        this.stepName = stepName;
        this.processId = processId;
    }
    
    public StepExecutionException(String message, String stepName, Throwable cause) {
        super(message, "STEP_EXECUTION_ERROR", cause);
        this.stepName = stepName;
        this.processId = null;
    }
    
    public String getStepName() {
        return stepName;
    }
    
    public String getProcessId() {
        return processId;
    }
}

