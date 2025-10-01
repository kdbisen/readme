package com.banking.onboarding.step.config;

import com.banking.onboarding.step.StepConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Step Configuration Loader - Loads step definitions from configuration
 */
@Slf4j
@Component
public class StepConfigurationLoader {
    
    @Value("${onboarding.steps.definition:XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_INFO,FENERGO_JOURNEY_INITIATE,FENERGO_JOURNEY_DETAILS}")
    private String stepDefinitions;
    
    @Value("${onboarding.steps.retry.enabled:true}")
    private boolean retryEnabled;
    
    @Value("${onboarding.steps.retry.max-attempts:3}")
    private int maxRetries;
    
    @Value("${onboarding.steps.retry.delay-ms:1000}")
    private long retryDelayMs;
    
    @Value("${onboarding.steps.retry.backoff-multiplier:2.0}")
    private double backoffMultiplier;
    
    @Value("${onboarding.steps.timeout-ms:30000}")
    private int timeoutMs;
    
    /**
     * Load step configurations
     */
    public List<StepConfig> loadStepConfigurations() {
        List<StepConfig> configs = new ArrayList<>();
        String[] stepNames = stepDefinitions.split(",");
        
        for (String stepName : stepNames) {
            stepName = stepName.trim();
            StepConfig config = createStepConfig(stepName);
            configs.add(config);
            log.info("Loaded step configuration: {}", config);
        }
        
        return configs;
    }
    
    /**
     * Create step configuration for a specific step
     */
    private StepConfig createStepConfig(String stepName) {
        return StepConfig.builder()
                .stepName(stepName)
                .description(getStepDescription(stepName))
                .retryEnabled(retryEnabled)
                .maxRetries(maxRetries)
                .retryDelayMs(retryDelayMs)
                .backoffMultiplier(backoffMultiplier)
                .asyncEnabled(true)
                .timeoutMs(timeoutMs)
                .dependencies(getStepDependencies(stepName))
                .properties(getStepProperties(stepName))
                .build();
    }
    
    /**
     * Get step description
     */
    private String getStepDescription(String stepName) {
        return switch (stepName) {
            case "XML_TO_JSON_TRANSFORMATION" -> "Transform XML data to JSON format via Apigee";
            case "FENERGO_ENTITY_CREATION" -> "Create entity in Fenergo via proxy";
            case "FENERGO_JOURNEY_INFO" -> "Get journey information from Fenergo";
            case "FENERGO_JOURNEY_INITIATE" -> "Initiate journey in Fenergo";
            case "FENERGO_JOURNEY_DETAILS" -> "Get journey details from Fenergo";
            default -> "Step: " + stepName;
        };
    }
    
    /**
     * Get step dependencies
     */
    private String[] getStepDependencies(String stepName) {
        return switch (stepName) {
            case "FENERGO_ENTITY_CREATION" -> new String[]{"XML_TO_JSON_TRANSFORMATION"};
            case "FENERGO_JOURNEY_INFO" -> new String[]{"FENERGO_ENTITY_CREATION"};
            case "FENERGO_JOURNEY_INITIATE" -> new String[]{"FENERGO_JOURNEY_INFO"};
            case "FENERGO_JOURNEY_DETAILS" -> new String[]{"FENERGO_JOURNEY_INITIATE"};
            default -> new String[0];
        };
    }
    
    /**
     * Get step properties
     */
    private Map<String, Object> getStepProperties(String stepName) {
        return Map.of(
                "category", getStepCategory(stepName),
                "priority", getStepPriority(stepName),
                "critical", isStepCritical(stepName)
        );
    }
    
    /**
     * Get step category
     */
    private String getStepCategory(String stepName) {
        if (stepName.startsWith("XML_TO_JSON")) return "TRANSFORMATION";
        if (stepName.startsWith("FENERGO_ENTITY")) return "ENTITY_MANAGEMENT";
        if (stepName.startsWith("FENERGO_JOURNEY")) return "JOURNEY_MANAGEMENT";
        return "GENERAL";
    }
    
    /**
     * Get step priority
     */
    private int getStepPriority(String stepName) {
        return switch (stepName) {
            case "XML_TO_JSON_TRANSFORMATION" -> 1;
            case "FENERGO_ENTITY_CREATION" -> 2;
            case "FENERGO_JOURNEY_INFO" -> 3;
            case "FENERGO_JOURNEY_INITIATE" -> 4;
            case "FENERGO_JOURNEY_DETAILS" -> 5;
            default -> 99;
        };
    }
    
    /**
     * Check if step is critical
     */
    private boolean isStepCritical(String stepName) {
        return !stepName.equals("FENERGO_JOURNEY_DETAILS"); // All steps except the last one are critical
    }
}
