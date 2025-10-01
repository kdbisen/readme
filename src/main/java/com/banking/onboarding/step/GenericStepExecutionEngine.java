package com.banking.onboarding.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Step Execution Engine - Orchestrates step execution with generic data sharing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericStepExecutionEngine {
    
    private final Map<String, GenericStepExecutor> stepExecutors = new ConcurrentHashMap<>();
    
    /**
     * Register a step executor
     */
    public void register(GenericStepExecutor executor) {
        String stepName = executor.getStepName();
        stepExecutors.put(stepName, executor);
        log.info("Registered generic step executor: {}", stepName);
    }
    
    /**
     * Execute a single step
     */
    public CompletableFuture<StepResult<Object>> executeStep(String stepName, GenericStepContext context) {
        GenericStepExecutor executor = stepExecutors.get(stepName);
        if (executor == null) {
            return CompletableFuture.completedFuture(
                    StepResult.failure("No step executor found for: " + stepName, stepName, context.getCorrelationId())
            );
        }
        
        StepConfig config = executor.getConfig();
        
        log.info("[CORRELATION:{}] Executing step: {} with config: {}", 
                context.getCorrelationId(), stepName, config);
        
        // Check if step can be executed
        if (!executor.canExecute(context)) {
            return CompletableFuture.completedFuture(
                    StepResult.failure("Step cannot be executed", stepName, context.getCorrelationId())
            );
        }
        
        // Execute step
        return executeOnce(executor, context);
    }
    
    /**
     * Execute multiple steps in sequence with generic data sharing
     */
    public CompletableFuture<StepResult<Map<String, Object>>> executeSteps(List<String> stepNames, GenericStepContext initialContext) {
        GenericStepContext context = initialContext;
        Map<String, Object> results = new ConcurrentHashMap<>();
        
        CompletableFuture<StepResult<Map<String, Object>>> future = CompletableFuture.completedFuture(
                StepResult.success(results, "INITIAL", initialContext.getCorrelationId())
        );
        
        for (String stepName : stepNames) {
            final String currentStepName = stepName;
            future = future.thenCompose(prevResult -> {
                if (!prevResult.isSuccess()) {
                    return CompletableFuture.completedFuture(prevResult);
                }
                
                return executeStep(currentStepName, context)
                        .thenApply(stepResult -> {
                            if (stepResult.isSuccess()) {
                                // Store result in context for next steps
                                context.addStepResult(currentStepName, stepResult.getData());
                                results.put(currentStepName, stepResult.getData());
                                
                                log.info("[CORRELATION:{}] Step {} completed successfully. Data shared: {}", 
                                        context.getCorrelationId(), currentStepName, 
                                        stepResult.getData() != null ? stepResult.getData().getClass().getSimpleName() : "null");
                                
                                return StepResult.success(results, "SEQUENCE", context.getCorrelationId());
                            } else {
                                return StepResult.failure(
                                        "Step " + currentStepName + " failed: " + stepResult.getErrorMessage(),
                                        "SEQUENCE", context.getCorrelationId()
                                );
                            }
                        });
            });
        }
        
        return future;
    }
    
    /**
     * Execute step once
     */
    private CompletableFuture<StepResult<Object>> executeOnce(GenericStepExecutor executor, GenericStepContext context) {
        long startTime = System.currentTimeMillis();
        
        return executor.execute(context)
                .thenApply(result -> {
                    long duration = System.currentTimeMillis() - startTime;
                    result.setDurationMs(duration);
                    result.setCompletedAt(LocalDateTime.now());
                    
                    log.info("[CORRELATION:{}] Step {} completed in {}ms", 
                            context.getCorrelationId(), executor.getStepName(), duration);
                    
                    return result;
                })
                .exceptionally(throwable -> {
                    log.error("[CORRELATION:{}] Step {} failed: {}", 
                            context.getCorrelationId(), executor.getStepName(), throwable.getMessage());
                    return executor.handleFailure(context, throwable).join();
                });
    }
    
    /**
     * Get registered step names
     */
    public String[] getRegisteredSteps() {
        return stepExecutors.keySet().toArray(new String[0]);
    }
    
    /**
     * Check if step is registered
     */
    public boolean hasStep(String stepName) {
        return stepExecutors.containsKey(stepName);
    }
}