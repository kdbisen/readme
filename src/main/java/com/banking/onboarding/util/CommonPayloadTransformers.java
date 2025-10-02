package com.banking.onboarding.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Common Payload Transformers
 * Reusable transformers for common payload refinement scenarios
 */
@Slf4j
@Component
public class CommonPayloadTransformers {

    /**
     * XML to JSON transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer xmlToJsonTransformer() {
        return payload -> {
            log.debug("Applying XML to JSON transformation");
            // This would typically call an XML to JSON service
            // For now, we'll just add a transformation marker
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            transformed.put("transformationType", "XML_TO_JSON");
            transformed.put("transformationTimestamp", java.time.LocalDateTime.now().toString());
            return transformed;
        };
    }

    /**
     * Data validation transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer dataValidationTransformer() {
        return payload -> {
            log.debug("Applying data validation transformation");
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            
            // Add validation markers
            transformed.put("validationStatus", "VALIDATED");
            transformed.put("validationTimestamp", java.time.LocalDateTime.now().toString());
            
            // Remove invalid fields
            transformed.entrySet().removeIf(entry -> 
                entry.getValue() instanceof String && 
                ((String) entry.getValue()).trim().isEmpty()
            );
            
            return transformed;
        };
    }

    /**
     * Fenergo entity preparation transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer fenergoEntityTransformer() {
        return payload -> {
            log.debug("Applying Fenergo entity preparation transformation");
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            
            // Add Fenergo-specific fields
            transformed.put("entityType", "CLIENT");
            transformed.put("entityStatus", "ACTIVE");
            transformed.put("fenergoPreparationTimestamp", java.time.LocalDateTime.now().toString());
            
            // Ensure required fields exist
            if (!transformed.containsKey("entityId")) {
                transformed.put("entityId", "TEMP-" + System.currentTimeMillis());
            }
            
            return transformed;
        };
    }

    /**
     * Journey preparation transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer journeyPreparationTransformer() {
        return payload -> {
            log.debug("Applying journey preparation transformation");
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            
            // Add journey-specific fields
            transformed.put("journeyType", "CLIENT_ONBOARDING");
            transformed.put("journeyStatus", "INITIATED");
            transformed.put("journeyPreparationTimestamp", java.time.LocalDateTime.now().toString());
            
            return transformed;
        };
    }

    /**
     * Generic field mapping transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer fieldMappingTransformer(Map<String, String> fieldMappings) {
        return payload -> {
            log.debug("Applying field mapping transformation with {} mappings", fieldMappings.size());
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            
            // Apply field mappings
            fieldMappings.forEach((oldField, newField) -> {
                if (transformed.containsKey(oldField)) {
                    Object value = transformed.remove(oldField);
                    transformed.put(newField, value);
                }
            });
            
            return transformed;
        };
    }

    /**
     * Data enrichment transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer dataEnrichmentTransformer(Map<String, Object> enrichmentData) {
        return payload -> {
            log.debug("Applying data enrichment transformation");
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            
            // Add enrichment data
            transformed.putAll(enrichmentData);
            transformed.put("enrichmentTimestamp", java.time.LocalDateTime.now().toString());
            
            return transformed;
        };
    }

    /**
     * Data filtering transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer dataFilteringTransformer(java.util.Set<String> allowedFields) {
        return payload -> {
            log.debug("Applying data filtering transformation, keeping {} fields", allowedFields.size());
            Map<String, Object> transformed = new java.util.HashMap<>();
            
            // Keep only allowed fields
            payload.entrySet().stream()
                    .filter(entry -> allowedFields.contains(entry.getKey()))
                    .forEach(entry -> transformed.put(entry.getKey(), entry.getValue()));
            
            return transformed;
        };
    }

    /**
     * Data formatting transformer
     */
    public static PayloadRefinementUtil.PayloadTransformer dataFormattingTransformer(Map<String, Function<Object, Object>> formatters) {
        return payload -> {
            log.debug("Applying data formatting transformation");
            Map<String, Object> transformed = new java.util.HashMap<>(payload);
            
            // Apply formatters
            formatters.forEach((field, formatter) -> {
                if (transformed.containsKey(field)) {
                    Object originalValue = transformed.get(field);
                    Object formattedValue = formatter.apply(originalValue);
                    transformed.put(field, formattedValue);
                }
            });
            
            return transformed;
        };
    }

    /**
     * Conditional transformer - applies transformation based on condition
     */
    public static PayloadRefinementUtil.PayloadTransformer conditionalTransformer(
            Function<Map<String, Object>, Boolean> condition,
            PayloadRefinementUtil.PayloadTransformer trueTransformer,
            PayloadRefinementUtil.PayloadTransformer falseTransformer) {
        
        return payload -> {
            log.debug("Applying conditional transformation");
            
            if (condition.apply(payload)) {
                return trueTransformer.transform(payload);
            } else {
                return falseTransformer.transform(payload);
            }
        };
    }

    /**
     * Chain multiple transformers
     */
    public static PayloadRefinementUtil.PayloadTransformer chainTransformers(
            PayloadRefinementUtil.PayloadTransformer... transformers) {
        
        return payload -> {
            log.debug("Applying chained transformations");
            Map<String, Object> result = payload;
            
            for (PayloadRefinementUtil.PayloadTransformer transformer : transformers) {
                result = transformer.transform(result);
            }
            
            return result;
        };
    }
}
