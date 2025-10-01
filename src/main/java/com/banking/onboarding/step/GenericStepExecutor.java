package com.banking.onboarding.step;

import java.util.concurrent.CompletableFuture;

/**
 * Generic Step Executor Interface - Works with any data type
 */
public interface GenericStepExecutor {
    
    /**
     * Execute the step with generic context
     */
    CompletableFuture<StepResult<Object>> execute(GenericStepContext context);
    
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
     * Handle step failure
     */
    default CompletableFuture<StepResult<Object>> handleFailure(GenericStepContext context, Throwable error) {
        return CompletableFuture.completedFuture(
                StepResult.<Object>builder()
                        .success(false)
                        .errorMessage(error.getMessage())
                        .stepName(getStepName())
                        .correlationId(context.getCorrelationId())
                        .build()
        );
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
