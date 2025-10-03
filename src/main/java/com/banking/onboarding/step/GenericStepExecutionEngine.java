package com.banking.onboarding.step;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.step.service.DynamicStepNumberingService;
import com.banking.onboarding.step.util.DynamicStepLoggingUtil;
import com.banking.onboarding.util.CompletePayloadStorageUtil;
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
    private final CompletePayloadStorageUtil payloadStorageUtil;
    private final DynamicStepNumberingService stepNumberingService;
    private final DynamicStepLoggingUtil stepLoggingUtil;
    
    /**
     * Register a step executor
     */
    public void register(GenericStepExecutor executor) {
        String stepName = executor.getStepName();
        stepExecutors.put(stepName, executor);
        log.info("Registered generic step executor: {} (Step {})", stepName, stepNumberingService.getStepNumber(stepName));
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
        
        log.info("[CORRELATION:{}] Executing step: {} (Step {}) with config: {}", 
                context.getCorrelationId(), stepName, stepNumberingService.getStepNumber(stepName), config);
        
        // Check if step can be executed
        if (!executor.canExecute(context)) {
            return StepResult.failure(OnboardingConstants.Messages.STEP_CANNOT_BE_EXECUTED, stepName, context.getCorrelationId());
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
        
        // Log execution order summary
        stepLoggingUtil.logExecutionOrderSummary(context.getCorrelationId());
        
        int completedSteps = 0;
        for (String stepName : stepNames) {
            int stepNumber = stepNumberingService.getStepNumber(stepName);
            log.info("[CORRELATION:{}] Executing step: {} (Step {})", context.getCorrelationId(), stepName, stepNumber);
            
            StepResult<Object> stepResult = executeStep(stepName, context);
            
            if (stepResult.isSuccess()) {
                completedSteps++;
                // Store result in context for next steps
                context.addStepResult(stepName, stepResult.getData());
                results.put(stepName, stepResult.getData());
                
                // Log progress
                stepLoggingUtil.logProcessProgress(context.getCorrelationId(), stepName, completedSteps, stepNames.size());
                
                log.info("[CORRELATION:{}] Step {} completed successfully. Data shared: {}", 
                        context.getCorrelationId(), stepNumber, 
                        stepResult.getData() != null ? stepResult.getData().getClass().getSimpleName() : "null");
            } else {
                log.error("[CORRELATION:{}] Step {} failed: {}", 
                        context.getCorrelationId(), stepNumber, stepResult.getErrorMessage());
                
                return StepResult.failure(
                        String.format(OnboardingConstants.FormatStrings.STEP_FAILED_FORMAT, stepName, stepResult.getErrorMessage()),
                        OnboardingConstants.SequenceNames.SEQUENCE, context.getCorrelationId()
                );
            }
        }
        
        log.info("[CORRELATION:{}] All {} steps completed successfully", 
                context.getCorrelationId(), stepNames.size());
        
        return StepResult.success(results, OnboardingConstants.SequenceNames.SEQUENCE, context.getCorrelationId());
    }
    
    /**
     * Execute step once - SYNCHRONOUS with payload/response tracking
     */
    private StepResult<Object> executeOnce(GenericStepExecutor executor, GenericStepContext context) {
        long startTime = System.currentTimeMillis();
        String stepName = executor.getStepName();
        
        // Capture input payload - COMPLETE, NO TRUNCATION
        Object inputPayload = executor.getInputData(context);
        String inputPayloadType = determinePayloadType(inputPayload);
        
        // Store complete input payload data
        CompletePayloadStorageUtil.CompletePayloadData inputPayloadData = 
            payloadStorageUtil.storeCompletePayload(inputPayload, inputPayloadType);
        
        try {
            StepResult<Object> result = executor.execute(context);
            long duration = System.currentTimeMillis() - startTime;
            
            result.setDurationMs(duration);
            result.setCompletedAt(LocalDateTime.now());
            
            // Store complete output response data
            CompletePayloadStorageUtil.CompletePayloadData outputPayloadData = 
                payloadStorageUtil.storeCompletePayload(result.getData(), determinePayloadType(result.getData()));
            
            // Store successful payload and response - COMPLETE DATA AS-IS
            if (result.isSuccess()) {
                context.storeSuccessfulStepPayloadResponse(
                    stepName,
                    inputPayloadData.getOriginalPayload(),      // COMPLETE INPUT
                    outputPayloadData.getOriginalPayload(),    // COMPLETE OUTPUT
                    inputPayloadType,
                    determinePayloadType(result.getData()),
                    duration,
                    Map.of(
                        "stepConfig", executor.getConfig(),
                        "executionTime", duration,
                        "correlationId", context.getCorrelationId(),
                        "inputPayloadSize", inputPayloadData.getAccuratePayloadSize(),
                        "outputPayloadSize", outputPayloadData.getAccuratePayloadSize(),
                        "inputPayloadComplete", inputPayloadData.getPayloadAsCompleteString(),
                        "outputPayloadComplete", outputPayloadData.getPayloadAsCompleteString()
                    )
                );
            } else {
                // Store failed payload and response - COMPLETE DATA AS-IS
                context.storeFailedStepPayloadResponse(
                    stepName,
                    inputPayloadData.getOriginalPayload(),      // COMPLETE INPUT
                    outputPayloadData.getOriginalPayload(),     // COMPLETE OUTPUT
                    inputPayloadType,
                    determinePayloadType(result.getData()),
                    duration,
                    result.getErrorMessage(),
                    Map.of(
                        "stepConfig", executor.getConfig(),
                        "executionTime", duration,
                        "correlationId", context.getCorrelationId(),
                        "errorType", OnboardingConstants.ErrorTypes.STEP_EXECUTION_ERROR,
                        "inputPayloadSize", inputPayloadData.getAccuratePayloadSize(),
                        "outputPayloadSize", outputPayloadData.getAccuratePayloadSize(),
                        "inputPayloadComplete", inputPayloadData.getPayloadAsCompleteString(),
                        "outputPayloadComplete", outputPayloadData.getPayloadAsCompleteString()
                    )
                );
            }
            
            log.info("[CORRELATION:{}] Step {} completed in {}ms. Input: {} bytes, Output: {} bytes", 
                    context.getCorrelationId(), stepName, duration,
                    getPayloadSize(inputPayload), getPayloadSize(result.getData()));
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Store failed payload and response with exception
            context.storeFailedStepPayloadResponse(
                stepName,
                inputPayload,
                null, // No output response due to exception
                inputPayloadType,
                null, // No output type due to exception
                duration,
                e.getMessage(),
                Map.of(
                    "stepConfig", executor.getConfig(),
                    "executionTime", duration,
                    "correlationId", context.getCorrelationId(),
                    "exceptionClass", e.getClass().getSimpleName(),
                    "stackTrace", getStackTrace(e)
                )
            );
            
            log.error("[CORRELATION:{}] Step {} failed after {}ms: {}", 
                    context.getCorrelationId(), stepName, duration, e.getMessage());
            
            StepResult<Object> failureResult = executor.handleFailure(context, e);
            failureResult.setDurationMs(duration);
            failureResult.setCompletedAt(LocalDateTime.now());
            
            return failureResult;
        }
    }
    
    /**
     * Determine payload type based on content
     */
    private String determinePayloadType(Object payload) {
        if (payload == null) return "NULL";
        
        String payloadStr = payload.toString().trim();
        if (payloadStr.startsWith("{") && payloadStr.endsWith("}")) {
            return "JSON";
        } else if (payloadStr.startsWith("<") && payloadStr.endsWith(">")) {
            return "XML";
        } else if (payloadStr.startsWith("[") && payloadStr.endsWith("]")) {
            return "JSON_ARRAY";
        } else if (payload instanceof Map) {
            return "MAP";
        } else if (payload instanceof String) {
            return "STRING";
        } else {
            return payload.getClass().getSimpleName().toUpperCase();
        }
    }
    
    /**
     * Get payload size - ACCURATE SIZE, NO TRUNCATION
     */
    private long getPayloadSize(Object payload) {
        if (payload == null) return 0;
        
        if (payload instanceof String) {
            return ((String) payload).length();
        } else if (payload instanceof byte[]) {
            return ((byte[]) payload).length;
        } else {
            return payload.toString().length();
        }
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
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