package com.banking.onboarding.util;

import com.banking.onboarding.step.GenericStepContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Universal Payload Refinement Utility
 * Can be used by every step to refine/transform input payload
 * Handles input from previous step output or initial payload
 */
@Slf4j
@Component
public class PayloadRefinementUtil {

    private final ObjectMapper objectMapper;

    public PayloadRefinementUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Get refined payload for any step
     * Automatically determines input source (previous step or initial payload)
     */
    public RefinedPayload getRefinedPayload(GenericStepContext context, String currentStepName) {
        log.debug("[CORRELATION:{}] Refining payload for step: {}", context.getCorrelationId(), currentStepName);

        // Try to get input from previous step first
        Optional<Object> previousStepOutput = getPreviousStepOutput(context, currentStepName);
        
        if (previousStepOutput.isPresent()) {
            log.debug("[CORRELATION:{}] Using previous step output for step: {}", context.getCorrelationId(), currentStepName);
            return refinePayload(previousStepOutput.get(), context.getCorrelationId(), "PREVIOUS_STEP");
        }

        // Fallback to initial payload
        Object initialPayload = context.getInputData();
        log.debug("[CORRELATION:{}] Using initial payload for step: {}", context.getCorrelationId(), currentStepName);
        return refinePayload(initialPayload, context.getCorrelationId(), "INITIAL_PAYLOAD");
    }

    /**
     * Get refined payload with specific input source
     */
    public RefinedPayload getRefinedPayload(GenericStepContext context, String currentStepName, InputSource inputSource) {
        log.debug("[CORRELATION:{}] Refining payload for step: {} from source: {}", 
                context.getCorrelationId(), currentStepName, inputSource);

        Object inputData = switch (inputSource) {
            case PREVIOUS_STEP -> getPreviousStepOutput(context, currentStepName)
                    .orElseThrow(() -> new IllegalArgumentException("No previous step output found"));
            case INITIAL_PAYLOAD -> context.getInputData();
            case SPECIFIC_STEP -> getSpecificStepOutput(context, currentStepName)
                    .orElseThrow(() -> new IllegalArgumentException("No specific step output found"));
        };

        return refinePayload(inputData, context.getCorrelationId(), inputSource.name());
    }

    /**
     * Get refined payload with custom transformation
     */
    public RefinedPayload getRefinedPayload(GenericStepContext context, String currentStepName, 
                                          PayloadTransformer transformer) {
        RefinedPayload basePayload = getRefinedPayload(context, currentStepName);
        return applyCustomTransformation(basePayload, transformer, context.getCorrelationId());
    }

    /**
     * Refine payload with common transformations
     */
    private RefinedPayload refinePayload(Object inputData, String correlationId, String source) {
        try {
            // Convert to Map for easier manipulation
            Map<String, Object> payloadMap = convertToMap(inputData);
            
            // Apply common refinements
            Map<String, Object> refinedMap = applyCommonRefinements(payloadMap, correlationId);
            
            // Add metadata
            RefinedPayloadMetadata metadata = RefinedPayloadMetadata.builder()
                    .source(source)
                    .originalType(inputData.getClass().getSimpleName())
                    .refinedType(refinedMap.getClass().getSimpleName())
                    .correlationId(correlationId)
                    .refinementTimestamp(java.time.LocalDateTime.now())
                    .build();

            return RefinedPayload.builder()
                    .originalData(inputData)
                    .refinedData(refinedMap)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Error refining payload: {}", correlationId, e.getMessage());
            throw new RuntimeException("Failed to refine payload: " + e.getMessage(), e);
        }
    }

    /**
     * Apply common refinements to payload
     */
    private Map<String, Object> applyCommonRefinements(Map<String, Object> payloadMap, String correlationId) {
        Map<String, Object> refined = new java.util.HashMap<>(payloadMap);

        // Add correlation ID if not present
        if (!refined.containsKey("correlationId")) {
            refined.put("correlationId", correlationId);
        }

        // Add timestamp if not present
        if (!refined.containsKey("timestamp")) {
            refined.put("timestamp", java.time.LocalDateTime.now().toString());
        }

        // Clean up null values
        refined.entrySet().removeIf(entry -> entry.getValue() == null);

        // Normalize string values (trim whitespace)
        refined.replaceAll((key, value) -> {
            if (value instanceof String) {
                return ((String) value).trim();
            }
            return value;
        });

        log.debug("[CORRELATION:{}] Applied common refinements to payload", correlationId);
        return refined;
    }

    /**
     * Apply custom transformation
     */
    private RefinedPayload applyCustomTransformation(RefinedPayload basePayload, 
                                                   PayloadTransformer transformer, 
                                                   String correlationId) {
        try {
            Map<String, Object> transformedData = transformer.transform(basePayload.getRefinedData());
            
            RefinedPayloadMetadata metadata = basePayload.getMetadata().toBuilder()
                    .customTransformation(true)
                    .transformationTimestamp(java.time.LocalDateTime.now())
                    .build();

            return basePayload.toBuilder()
                    .refinedData(transformedData)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("[CORRELATION:{}] Error applying custom transformation: {}", correlationId, e.getMessage());
            throw new RuntimeException("Failed to apply custom transformation: " + e.getMessage(), e);
        }
    }

    /**
     * Get previous step output
     */
    private Optional<Object> getPreviousStepOutput(GenericStepContext context, String currentStepName) {
        // Get all step results and find the most recent one
        Map<String, Object> stepResults = context.getAllStepResults();
        
        return stepResults.entrySet().stream()
                .max(Map.Entry.comparingByKey()) // Get the last executed step
                .map(Map.Entry::getValue);
    }

    /**
     * Get specific step output
     */
    private Optional<Object> getSpecificStepOutput(GenericStepContext context, String stepName) {
        return Optional.ofNullable(context.getStepResult(stepName));
    }

    /**
     * Convert input data to Map
     */
    private Map<String, Object> convertToMap(Object inputData) {
        if (inputData instanceof Map) {
            return (Map<String, Object>) inputData;
        }
        
        if (inputData instanceof String) {
            try {
                return objectMapper.readValue((String) inputData, Map.class);
            } catch (Exception e) {
                // If it's not JSON, wrap it in a map
                return Map.of("data", inputData);
            }
        }
        
        // For other types, wrap in a map
        return Map.of("data", inputData);
    }

    /**
     * Input source enumeration
     */
    public enum InputSource {
        PREVIOUS_STEP,    // Use output from previous step
        INITIAL_PAYLOAD,  // Use initial input payload
        SPECIFIC_STEP     // Use output from specific step
    }

    /**
     * Payload transformer interface
     */
    @FunctionalInterface
    public interface PayloadTransformer {
        Map<String, Object> transform(Map<String, Object> payload);
    }

    /**
     * Refined payload result
     */
    @lombok.Data
    @lombok.Builder(toBuilder = true)
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RefinedPayload {
        private Object originalData;
        private Map<String, Object> refinedData;
        private RefinedPayloadMetadata metadata;

        /**
         * Get refined data as specific type
         */
        public <T> T getRefinedDataAs(Class<T> clazz, ObjectMapper mapper) {
            if (clazz.isInstance(refinedData)) {
                return clazz.cast(refinedData);
            }
            return mapper.convertValue(refinedData, clazz);
        }

        /**
         * Get refined data as JSON string
         */
        public String getRefinedDataAsJson(ObjectMapper mapper) {
            try {
                return mapper.writeValueAsString(refinedData);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert refined data to JSON", e);
            }
        }
    }

    /**
     * Refined payload metadata
     */
    @lombok.Data
    @lombok.Builder(toBuilder = true)
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RefinedPayloadMetadata {
        private String source;
        private String originalType;
        private String refinedType;
        private String correlationId;
        private java.time.LocalDateTime refinementTimestamp;
        private boolean customTransformation;
        private java.time.LocalDateTime transformationTimestamp;
    }
}
