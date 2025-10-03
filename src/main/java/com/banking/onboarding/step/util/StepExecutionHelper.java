package com.banking.onboarding.step.util;

import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Step Execution Helper - Eliminates if-else logic in step executors
 * Provides clean, functional approach to step execution
 */
@Slf4j
public class StepExecutionHelper {

    /**
     * Execute step with clean error handling - no if-else chains
     */
    public static <T> StepResult<Object> executeStep(
            String stepName,
            String correlationId,
            Supplier<T> businessLogic,
            Function<T, String> successMessage) {
        
        return executeStep(stepName, correlationId, businessLogic, successMessage, null);
    }

    /**
     * Execute step with custom error message
     */
    public static <T> StepResult<Object> executeStep(
            String stepName,
            String correlationId,
            Supplier<T> businessLogic,
            Function<T, String> successMessage,
            String customErrorMessage) {
        
        try {
            log.info("[CORRELATION:{}] Executing {} Step", correlationId, stepName);
            
            T result = businessLogic.get();
            String message = successMessage.apply(result);
            
            log.info("[CORRELATION:{}] {} completed successfully: {}", correlationId, stepName, message);
            
            return StepResult.success(result, stepName, correlationId);
            
        } catch (Exception e) {
            String errorMessage = customErrorMessage != null ? 
                customErrorMessage + ": " + e.getMessage() : 
                stepName + " failed: " + e.getMessage();
                
            log.error("[CORRELATION:{}] {} failed: {}", correlationId, stepName, e.getMessage());
            return StepResult.failure(errorMessage, stepName, correlationId);
        }
    }

    /**
     * Validate step prerequisites - clean validation without if-else
     */
    public static boolean validatePrerequisites(GenericStepContext context, String[] dependencies) {
        return validateInputData(context) && validateDependencies(context, dependencies);
    }

    /**
     * Validate input data exists
     */
    public static boolean validateInputData(GenericStepContext context) {
        return context.getInputData() != null;
    }

    /**
     * Validate all dependencies are satisfied
     */
    public static boolean validateDependencies(GenericStepContext context, String[] dependencies) {
        for (String dependency : dependencies) {
            if (context.getStepResult(dependency) == null) {
                log.warn("[CORRELATION:{}] Dependency {} not satisfied", 
                        context.getCorrelationId(), dependency);
                return false;
            }
        }
        return true;
    }

    /**
     * Create success result with automatic context storage
     */
    public static <T> StepResult<T> createSuccessResult(
            String stepName,
            String correlationId,
            T data,
            GenericStepContext context) {
        
        context.addStepResult(stepName, data);
        return StepResult.success(data, stepName, correlationId);
    }

    /**
     * Create failure result with detailed error info
     */
    public static <T> StepResult<T> createFailureResult(
            String stepName,
            String correlationId,
            String errorMessage) {
        
        return StepResult.failure(errorMessage, stepName, correlationId);
    }
}
