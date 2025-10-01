package com.banking.onboarding.step;

/**
 * Generic Step Executor Interface - Synchronous approach for better simplicity and debugging
 */
public interface GenericStepExecutor {
    
    /**
     * Execute the step with generic context - SYNCHRONOUS
     */
    StepResult<Object> execute(GenericStepContext context);
    
    /**
     * Get step configuration
     */
    StepConfig getConfig();
    
    /**
     * Get step name
     */
    String getStepName();
    
    /**
     * Check if step can be executed
     */
    default boolean canExecute(GenericStepContext context) {
        return true;
    }
    
    /**
     * Handle step failure - SYNCHRONOUS
     */
    default StepResult<Object> handleFailure(GenericStepContext context, Exception error) {
        return StepResult.<Object>builder()
                .success(false)
                .errorMessage(error.getMessage())
                .stepName(getStepName())
                .correlationId(context.getCorrelationId())
                .completedAt(java.time.LocalDateTime.now())
                .build();
    }
    
    /**
     * Get input data for this step from context
     */
    default Object getInputData(GenericStepContext context) {
        // Try to get from previous step result first
        String previousStep = getConfig().getDependencies().length > 0 ? 
                getConfig().getDependencies()[getConfig().getDependencies().length - 1] : null;
        
        if (previousStep != null && context.hasStepResult(previousStep)) {
            return context.getStepResult(previousStep);
        }
        
        // Fallback to initial input data
        return context.getInputData();
    }
}
