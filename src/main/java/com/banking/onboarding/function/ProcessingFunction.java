package com.banking.onboarding.function;

import com.banking.onboarding.context.ProcessingContext;
import java.util.function.Function;

/**
 * Simple function interface for processing steps
 * @param <T> The type of data being processed
 */
@FunctionalInterface
public interface ProcessingFunction<T> extends Function<ProcessingContext<T>, ProcessingContext<T>> {
    
    /**
     * Get the step name for logging
     */
    default String getStepName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Check if this function should be executed
     */
    default boolean shouldExecute(ProcessingContext<T> context) {
        return true;
    }
    
    /**
     * Handle errors
     */
    default ProcessingContext<T> handleError(ProcessingContext<T> context, Exception e) {
        context.failStep(getStepName(), e.getMessage());
        return context;
    }
}
