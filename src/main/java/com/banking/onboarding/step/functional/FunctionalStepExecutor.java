package com.banking.onboarding.step.functional;

import com.banking.onboarding.step.GenericStepContext;
import com.banking.onboarding.step.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.function.*;

/**
 * Functional Step Executor - Enhanced with Java 8 Functional Interfaces
 * Provides powerful functional programming patterns for step execution
 */
@Slf4j
public class FunctionalStepExecutor {

    /**
     * Execute step with comprehensive functional interfaces
     */
    public static <T> StepResult<Object> executeStep(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,                    // Pure business logic
            Function<T, String> successMessageCreator,   // Success message creation
            Predicate<GenericStepContext> preCondition,  // Pre-execution condition
            Consumer<T> postExecutionAction,             // Post-execution action
            Function<Exception, String> errorTransformer // Error message transformation
    ) {
        
        return executeStep(
                stepName, correlationId, context,
                businessLogic, successMessageCreator,
                preCondition, postExecutionAction,
                errorTransformer, null
        );
    }

    /**
     * Execute step with optional custom error message
     */
    public static <T> StepResult<Object> executeStep(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,
            Function<T, String> successMessageCreator,
            Predicate<GenericStepContext> preCondition,
            Consumer<T> postExecutionAction,
            Function<Exception, String> errorTransformer,
            String customErrorMessage
    ) {
        
        // Pre-execution validation using Predicate
        if (!preCondition.test(context)) {
            String errorMsg = customErrorMessage != null ? 
                customErrorMessage : "Pre-condition validation failed";
            log.warn("[CORRELATION:{}] {} pre-condition failed", correlationId, stepName);
            return StepResult.failure(errorMsg, stepName, correlationId);
        }

        try {
            log.info("[CORRELATION:{}] Executing {} Step", correlationId, stepName);
            
            // Execute business logic using Supplier
            T result = businessLogic.get();
            
            // Create success message using Function
            String successMessage = successMessageCreator.apply(result);
            
            // Execute post-action using Consumer
            postExecutionAction.accept(result);
            
            log.info("[CORRELATION:{}] {} completed successfully: {}", correlationId, stepName, successMessage);
            
            return StepResult.success(result, stepName, correlationId);
            
        } catch (Exception e) {
            // Transform error using Function
            String errorMessage = errorTransformer.apply(e);
            log.error("[CORRELATION:{}] {} failed: {}", correlationId, stepName, errorMessage);
            return StepResult.failure(errorMessage, stepName, correlationId);
        }
    }

    /**
     * Simplified execution with default functional interfaces
     */
    public static <T> StepResult<Object> executeStepSimple(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,
            Function<T, String> successMessageCreator
    ) {
        return executeStep(
                stepName, correlationId, context,
                businessLogic, successMessageCreator,
                ctx -> true,                    // Always allow execution
                result -> {},                   // No post-action
                ex -> stepName + " failed: " + ex.getMessage()  // Default error transformation
        );
    }

    /**
     * Execute step with validation chain using multiple Predicates
     */
    public static <T> StepResult<Object> executeStepWithValidationChain(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,
            Function<T, String> successMessageCreator,
            Predicate<GenericStepContext>... validators
    ) {
        
        // Chain all validators using Predicate.and()
        Predicate<GenericStepContext> combinedValidator = Arrays.stream(validators)
                .reduce(Predicate::and)
                .orElse(ctx -> true);
        
        return executeStep(
                stepName, correlationId, context,
                businessLogic, successMessageCreator,
                combinedValidator,
                result -> {},  // No post-action
                ex -> stepName + " failed: " + ex.getMessage()
        );
    }

    /**
     * Execute step with transformation pipeline using Function composition
     */
    public static <T, R> StepResult<Object> executeStepWithTransformation(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> inputSupplier,
            Function<T, R> transformation,
            Function<R, String> successMessageCreator
    ) {
        
        Supplier<R> businessLogic = () -> transformation.apply(inputSupplier.get());
        
        return executeStepSimple(
                stepName, correlationId, context,
                businessLogic, successMessageCreator
        );
    }

    /**
     * Execute step with retry mechanism using functional retry logic
     */
    public static <T> StepResult<Object> executeStepWithRetry(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,
            Function<T, String> successMessageCreator,
            Predicate<Exception> retryCondition,
            int maxRetries
    ) {
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("[CORRELATION:{}] {} attempt {}/{}", correlationId, stepName, attempt, maxRetries);
                
                T result = businessLogic.get();
                String successMessage = successMessageCreator.apply(result);
                
                log.info("[CORRELATION:{}] {} succeeded on attempt {}", correlationId, stepName, attempt);
                return StepResult.success(result, stepName, correlationId);
                
            } catch (Exception e) {
                lastException = e;
                
                if (!retryCondition.test(e) || attempt == maxRetries) {
                    break;
                }
                
                log.warn("[CORRELATION:{}] {} attempt {} failed, retrying: {}", 
                        correlationId, stepName, attempt, e.getMessage());
            }
        }
        
        String errorMessage = stepName + " failed after " + maxRetries + " attempts: " + lastException.getMessage();
        log.error("[CORRELATION:{}] {}", correlationId, errorMessage);
        return StepResult.failure(errorMessage, stepName, correlationId);
    }

    /**
     * Execute step with conditional execution using BiPredicate
     */
    public static <T> StepResult<Object> executeStepConditionally(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,
            Function<T, String> successMessageCreator,
            BiPredicate<GenericStepContext, T> executionCondition
    ) {
        
        try {
            T result = businessLogic.get();
            
            if (executionCondition.test(context, result)) {
                String successMessage = successMessageCreator.apply(result);
                log.info("[CORRELATION:{}] {} executed conditionally: {}", correlationId, stepName, successMessage);
                return StepResult.success(result, stepName, correlationId);
            } else {
                log.info("[CORRELATION:{}] {} skipped due to condition", correlationId, stepName);
                return StepResult.success(result, stepName, correlationId);
            }
            
        } catch (Exception e) {
            String errorMessage = stepName + " failed: " + e.getMessage();
            log.error("[CORRELATION:{}] {}", correlationId, errorMessage);
            return StepResult.failure(errorMessage, stepName, correlationId);
        }
    }

    /**
     * Execute step with side effects using BiConsumer
     */
    public static <T> StepResult<Object> executeStepWithSideEffects(
            String stepName,
            String correlationId,
            GenericStepContext context,
            Supplier<T> businessLogic,
            Function<T, String> successMessageCreator,
            BiConsumer<GenericStepContext, T> sideEffect
    ) {
        
        try {
            T result = businessLogic.get();
            
            // Apply side effect
            sideEffect.accept(context, result);
            
            String successMessage = successMessageCreator.apply(result);
            log.info("[CORRELATION:{}] {} completed with side effects: {}", correlationId, stepName, successMessage);
            
            return StepResult.success(result, stepName, correlationId);
            
        } catch (Exception e) {
            String errorMessage = stepName + " failed: " + e.getMessage();
            log.error("[CORRELATION:{}] {}", correlationId, errorMessage);
            return StepResult.failure(errorMessage, stepName, correlationId);
        }
    }
}
