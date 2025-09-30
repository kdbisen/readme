package com.banking.onboarding.context;

import com.banking.onboarding.model.EntityData;
import com.banking.onboarding.model.RequestType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Simplified generic processing context with essential features only
 * @param <T> The type of data being processed (e.g., EntityData, DocumentData, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProcessingContext<T> {
    
    // Core process information
    private String processId;
    private String correlationId;
    private String traceId;
    private RequestType requestType;
    private LocalDateTime startTime;
    
    // Processing status
    private String currentStep;
    private String status;
    private String errorMessage;
    private boolean hasError;
    
    // Input and processed data
    private String rawPayload;
    private T processedData;
    
    // Simple step data and results
    private Map<String, Object> stepData;
    private Map<String, Object> results;
    
    /**
     * Create a new processing context for onboarding
     */
    public static ProcessingContext<EntityData> createOnboardingContext(String payload, RequestType requestType, String correlationId) {
        return ProcessingContext.<EntityData>builder()
                .processId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .requestType(requestType)
                .startTime(LocalDateTime.now())
                .rawPayload(payload)
                .currentStep("INITIALIZED")
                .status("STARTED")
                .hasError(false)
                .stepData(new HashMap<>())
                .results(new HashMap<>())
                .build();
    }
    
    /**
     * Create a new processing context for any process type
     */
    public static <T> ProcessingContext<T> createContext(String payload, RequestType requestType, Class<T> dataType, String correlationId) {
        return ProcessingContext.<T>builder()
                .processId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .traceId(UUID.randomUUID().toString())
                .requestType(requestType)
                .startTime(LocalDateTime.now())
                .rawPayload(payload)
                .currentStep("INITIALIZED")
                .status("STARTED")
                .hasError(false)
                .stepData(new HashMap<>())
                .results(new HashMap<>())
                .build();
    }
    
    /**
     * Start step execution
     */
    public void startStep(String stepName) {
        this.currentStep = stepName;
        log.info("[TRACE:{}] Starting step: {} for process: {}", traceId, stepName, processId);
    }
    
    /**
     * Complete step execution
     */
    public void completeStep(String stepName) {
        log.info("[TRACE:{}] Completed step: {} for process: {}", traceId, stepName, processId);
    }
    
    /**
     * Fail step execution
     */
    public void failStep(String stepName, String errorMessage) {
        this.hasError = true;
        this.errorMessage = errorMessage;
        log.error("[TRACE:{}] Step failed: {} for process: {} - {}", traceId, stepName, processId, errorMessage);
    }
    
    /**
     * Add step data
     */
    public void addStepData(String key, Object value) {
        if (stepData == null) {
            stepData = new HashMap<>();
        }
        stepData.put(key, value);
    }
    
    /**
     * Get step data
     */
    public Object getStepData(String key) {
        return stepData != null ? stepData.get(key) : null;
    }
    
    /**
     * Add result
     */
    public void addResult(String key, Object value) {
        if (results == null) {
            results = new HashMap<>();
        }
        results.put(key, value);
    }
    
    /**
     * Get result
     */
    public Object getResult(String key) {
        return results != null ? results.get(key) : null;
    }
    
    /**
     * Calculate processing duration
     */
    public long getProcessingDurationMs() {
        if (startTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
    
    /**
     * Convert to Map for backward compatibility
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("processId", processId);
        map.put("correlationId", correlationId);
        map.put("traceId", traceId);
        map.put("requestType", requestType);
        map.put("startTime", startTime);
        map.put("currentStep", currentStep);
        map.put("status", status);
        map.put("errorMessage", errorMessage);
        map.put("hasError", hasError);
        map.put("rawPayload", rawPayload);
        map.put("processedData", processedData);
        map.put("stepData", stepData);
        map.put("results", results);
        return map;
    }
    
    /**
     * Create from Map (for backward compatibility)
     */
    public static <T> ProcessingContext<T> fromMap(Map<String, Object> map) {
        return ProcessingContext.<T>builder()
                .processId((String) map.get("processId"))
                .correlationId((String) map.get("correlationId"))
                .traceId((String) map.get("traceId"))
                .requestType((RequestType) map.get("requestType"))
                .startTime((LocalDateTime) map.get("startTime"))
                .currentStep((String) map.get("currentStep"))
                .status((String) map.get("status"))
                .errorMessage((String) map.get("errorMessage"))
                .hasError(Boolean.TRUE.equals(map.get("hasError")))
                .rawPayload((String) map.get("rawPayload"))
                .processedData((T) map.get("processedData"))
                .stepData((Map<String, Object>) map.get("stepData"))
                .results((Map<String, Object>) map.get("results"))
                .build();
    }
}
