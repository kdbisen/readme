package com.banking.onboarding.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Step Execution Engine - Synchronous approach for better simplicity and debugging
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
     * Execute a single step - SYNCHRONOUS
     */
    public StepResult<Object> executeStep(String stepName, GenericStepContext context) {
        GenericStepExecutor executor = stepExecutors.get(stepName);
        if (executor == null) {
            return StepResult.failure("No step executor found for: " + stepName, stepName, context.getCorrelationId());
        }
        
        StepConfig config = executor.getConfig();
        
        log.info("[CORRELATION:{}] Executing step: {} with config: {}", 
                context.getCorrelationId(), stepName, config);
        
        // Check if step can be executed
        if (!executor.canExecute(context)) {
            return StepResult.failure("Step cannot be executed", stepName, context.getCorrelationId());
        }
        
        // Execute step synchronously
        return executeOnce(executor, context);
    }
    
    /**
     * Execute multiple steps in sequence with generic data sharing - SYNCHRONOUS
     */
    public StepResult<Map<String, Object>> executeSteps(List<String> stepNames, GenericStepContext initialContext) {
        GenericStepContext context = initialContext;
        Map<String, Object> results = new ConcurrentHashMap<>();
        
        log.info("[CORRELATION:{}] Starting sequential execution of {} steps", 
                context.getCorrelationId(), stepNames.size());
        
        for (String stepName : stepNames) {
            log.info("[CORRELATION:{}] Executing step: {}", context.getCorrelationId(), stepName);
            
            StepResult<Object> stepResult = executeStep(stepName, context);
            
            if (stepResult.isSuccess()) {
                // Store result in context for next steps
                context.addStepResult(stepName, stepResult.getData());
                results.put(stepName, stepResult.getData());
                
                log.info("[CORRELATION:{}] Step {} completed successfully. Data shared: {}", 
                        context.getCorrelationId(), stepName, 
                        stepResult.getData() != null ? stepResult.getData().getClass().getSimpleName() : "null");
            } else {
                log.error("[CORRELATION:{}] Step {} failed: {}", 
                        context.getCorrelationId(), stepName, stepResult.getErrorMessage());
                
                return StepResult.failure(
                        "Step " + stepName + " failed: " + stepResult.getErrorMessage(),
                        "SEQUENCE", context.getCorrelationId()
                );
            }
        }
        
        log.info("[CORRELATION:{}] All {} steps completed successfully", 
                context.getCorrelationId(), stepNames.size());
        
        return StepResult.success(results, "SEQUENCE", context.getCorrelationId());
    }
    
    /**
     * Execute step once - SYNCHRONOUS
     */
    private StepResult<Object> executeOnce(GenericStepExecutor executor, GenericStepContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            StepResult<Object> result = executor.execute(context);
            long duration = System.currentTimeMillis() - startTime;
            
            result.setDurationMs(duration);
            result.setCompletedAt(LocalDateTime.now());
            
            log.info("[CORRELATION:{}] Step {} completed in {}ms", 
                    context.getCorrelationId(), executor.getStepName(), duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("[CORRELATION:{}] Step {} failed after {}ms: {}", 
                    context.getCorrelationId(), executor.getStepName(), duration, e.getMessage());
            
            StepResult<Object> failureResult = executor.handleFailure(context, e);
            failureResult.setDurationMs(duration);
            failureResult.setCompletedAt(LocalDateTime.now());
            
            return failureResult;
        }
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