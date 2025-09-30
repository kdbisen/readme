package com.banking.onboarding.service;

import com.banking.onboarding.context.ProcessingContext;
import com.banking.onboarding.function.*;
import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.RequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Functional processing service using Java 8 Functions and Spring @Async
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionalOnboardingService {
    
    private final TransformFunction transformFunction;
    private final ValidateFunction validateFunction;
    private final FenergoFunction fenergoFunction;
    private final CompleteFunction completeFunction;
    private final OnboardingProcessService processService;
    private final CorrelationIdService correlationIdService;
    
    /**
     * Process entity asynchronously using functional pipeline
     */
    @Async
    public void processEntityAsync(String payload, RequestType requestType, String processId) {
        log.info("Starting functional async processing for process: {}", processId);
        
        try {
            // Get correlation ID from request context
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            // Create initial context
            ProcessingContext<EntityData> context = ProcessingContext.createOnboardingContext(payload, requestType, correlationId);
            
            // Build functional pipeline
            Function<ProcessingContext<EntityData>, ProcessingContext<EntityData>> pipeline = buildProcessingPipeline();
            
            // Execute pipeline
            ProcessingContext<EntityData> result = pipeline.apply(context);
            
            // Handle final result
            handleFinalResult(result);
            
        } catch (Exception e) {
            log.error("Functional async processing failed for process: {}", processId, e);
            handleError(processId, e);
        }
    }
    
    /**
     * Build the functional processing pipeline
     */
    private Function<ProcessingContext<EntityData>, ProcessingContext<EntityData>> buildProcessingPipeline() {
        return context -> {
            ProcessingContext<EntityData> result = context;
            
            // Chain of functions: Transform -> Validate -> Fenergo -> Complete
            try {
                // Step 1: Transform
                if (transformFunction.shouldExecute(result)) {
                    log.info("Executing Transform step for process: {}", result.getProcessId());
                    result = transformFunction.apply(result);
                }
                
                // Step 2: Validate
                if (validateFunction.shouldExecute(result)) {
                    log.info("Executing Validate step for process: {}", result.getProcessId());
                    result = validateFunction.apply(result);
                }
                
                // Step 3: Fenergo
                if (fenergoFunction.shouldExecute(result)) {
                    log.info("Executing Fenergo step for process: {}", result.getProcessId());
                    result = fenergoFunction.apply(result);
                }
                
                // Step 4: Complete
                if (completeFunction.shouldExecute(result)) {
                    log.info("Executing Complete step for process: {}", result.getProcessId());
                    result = completeFunction.apply(result);
                }
                
            } catch (Exception e) {
                log.error("Pipeline execution failed for process: {}", result.getProcessId(), e);
                result.setHasError(true);
                result.setErrorMessage(e.getMessage());
                result.addResult("errorTimestamp", LocalDateTime.now());
            }
            
            return result;
        };
    }
    
    /**
     * Alternative: Using ProcessingChain for more complex scenarios
     */
    @Async
    public void processEntityWithChain(String payload, RequestType requestType, String processId) {
        log.info("Starting chain-based async processing for process: {}", processId);
        
        try {
            // Get correlation ID from request context
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            // Create initial context
            ProcessingContext<EntityData> context = ProcessingContext.createOnboardingContext(payload, requestType, correlationId);
            
            // Build processing chain
            ProcessingChain<EntityData> chain = new ProcessingChain<EntityData>()
                    .addFunction(transformFunction)
                    .addFunction(validateFunction)
                    .addFunction(fenergoFunction)
                    .addFunction(completeFunction);
            
            // Execute chain
            ProcessingContext<EntityData> result = chain.execute(context);
            
            // Handle result
            handleFinalResult(result);
            
        } catch (Exception e) {
            log.error("Chain-based processing failed for process: {}", processId, e);
            handleError(processId, e);
        }
    }
    
    /**
     * Create initial input map
     */
    private Map<String, Object> createInitialInput(String payload, RequestType requestType, String processId) {
        Map<String, Object> input = new HashMap<>();
        input.put("processId", processId);
        input.put("payload", payload);
        input.put("requestType", requestType);
        input.put("startTime", LocalDateTime.now());
        input.put("correlationId", UUID.randomUUID().toString());
        return input;
    }
    
    /**
     * Handle final processing result
     */
    private void handleFinalResult(ProcessingContext<EntityData> result) {
        String processId = result.getProcessId();
        
        try {
            // Update process with final result
            OnboardingProcess process = processService.getProcessById(processId)
                    .orElseThrow(() -> new RuntimeException("Process not found: " + processId));
            
            // Update process fields
            if (result.getProcessedData() != null) {
                process.setEntityData(result.getProcessedData());
            }
            
            if (result.getResult("transformedJson") != null) {
                process.setTransformedJson((Map<String, Object>) result.getResult("transformedJson"));
            }
            
            if (result.getResult("fenergoResponse") != null) {
                process.setFenergoResponse((Map<String, Object>) result.getResult("fenergoResponse"));
            }
            
            if (result.isHasError()) {
                process.setErrorMessage(result.getErrorMessage());
                process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
            } else {
                process.setStatus(OnboardingProcess.ProcessStatus.COMPLETED);
            }
            
            process.setCompletedAt(LocalDateTime.now());
            processService.updateProcess(process);
            
            log.info("Process completed successfully: {}", processId);
            
        } catch (Exception e) {
            log.error("Failed to update process: {}", processId, e);
        }
    }
    
    /**
     * Handle processing errors
     */
    private void handleError(String processId, Exception e) {
        try {
            OnboardingProcess process = processService.getProcessById(processId)
                    .orElseThrow(() -> new RuntimeException("Process not found: " + processId));
            
            process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
            process.setErrorMessage(e.getMessage());
            process.setCompletedAt(LocalDateTime.now());
            processService.updateProcess(process);
            
        } catch (Exception ex) {
            log.error("Failed to handle error for process: {}", processId, ex);
        }
    }
}
