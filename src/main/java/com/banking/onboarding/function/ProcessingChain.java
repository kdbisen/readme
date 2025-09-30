package com.banking.onboarding.function;

import com.banking.onboarding.context.ProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Simple Chain of Responsibility implementation
 * @param <T> The type of data being processed
 */
@Slf4j
@Component
public class ProcessingChain<T> {
    
    private final List<ProcessingFunction<T>> functions = new ArrayList<>();
    
    /**
     * Add a function to the chain
     */
    public ProcessingChain<T> addFunction(ProcessingFunction<T> function) {
        functions.add(function);
        log.info("Added function to chain: {}", function.getStepName());
        return this;
    }
    
    /**
     * Execute the entire chain
     */
    public ProcessingContext<T> execute(ProcessingContext<T> context) {
        log.info("[TRACE:{}] Starting processing chain for process: {} with {} functions", 
                context.getTraceId(), context.getProcessId(), functions.size());
        
        ProcessingContext<T> result = context;
        
        for (ProcessingFunction<T> function : functions) {
            String stepName = function.getStepName();
            
            try {
                // Check if function should execute
                if (!function.shouldExecute(result)) {
                    log.info("[TRACE:{}] Skipping function: {} for process: {}", 
                            context.getTraceId(), stepName, result.getProcessId());
                    continue;
                }
                
                log.info("[TRACE:{}] Executing function: {} for process: {}", 
                        context.getTraceId(), stepName, result.getProcessId());
                
                // Execute function
                result = function.apply(result);
                
                log.info("[TRACE:{}] Function completed: {} for process: {}", 
                        context.getTraceId(), stepName, result.getProcessId());
                
            } catch (Exception e) {
                log.error("[TRACE:{}] Function failed: {} for process: {} - Error: {}", 
                        context.getTraceId(), stepName, result.getProcessId(), e.getMessage(), e);
                
                // Handle error using function's error handler
                result = function.handleError(result, e);
                break; // Stop chain execution on error
            }
        }
        
        log.info("[TRACE:{}] Processing chain completed for process: {}", 
                context.getTraceId(), result.getProcessId());
        
        return result;
    }
    
    /**
     * Create a functional pipeline using method chaining
     */
    public Function<ProcessingContext<T>, ProcessingContext<T>> buildPipeline() {
        return context -> {
            ProcessingContext<T> result = context;
            
            for (ProcessingFunction<T> function : functions) {
                if (function.shouldExecute(result)) {
                    try {
                        result = function.apply(result);
                    } catch (Exception e) {
                        result = function.handleError(result, e);
                        break;
                    }
                }
            }
            
            return result;
        };
    }
    
    /**
     * Get the number of functions in the chain
     */
    public int size() {
        return functions.size();
    }
    
    /**
     * Clear all functions from the chain
     */
    public void clear() {
        functions.clear();
    }
    
    /**
     * Create a new chain instance
     */
    public static <T> ProcessingChain<T> create() {
        return new ProcessingChain<>();
    }
}
