package com.banking.onboarding.step.config;

import com.banking.onboarding.step.StepConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple Step Configuration Loader - Minimal Code
 */
@Slf4j
@Component
public class StepConfigurationLoader {

    @Value("${onboarding.steps.definition:XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH}")
    private String stepDefinitions;

    @Value("${onboarding.steps.execution.order:PRIORITY}")
    private String executionOrder;

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
     * Simple Step Definitions - Minimal Code
     */
    private static final Map<String, StepInfo> STEPS = Map.of(
        "XML_TO_JSON_TRANSFORMATION", new StepInfo(1, "Transform XML to JSON", new String[]{}),
        "FENERGO_ENTITY_CREATION", new StepInfo(2, "Create Fenergo Entity", new String[]{"XML_TO_JSON_TRANSFORMATION"}),
        "FENERGO_JOURNEY_SCHEMA_EVALUATION", new StepInfo(3, "Evaluate Journey Schema", new String[]{"FENERGO_ENTITY_CREATION"}),
        "FENERGO_JOURNEY_LAUNCH", new StepInfo(4, "Launch Journey", new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"})
    );

    /**
     * Load step configurations
     */
    public List<StepConfig> loadStepConfigurations() {
        return Arrays.stream(stepDefinitions.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::createStepConfig)
                .collect(Collectors.toList());
    }

    /**
     * Create StepConfig
     */
    private StepConfig createStepConfig(String stepName) {
        StepInfo info = STEPS.get(stepName);
        
        if (info == null) {
            return createDefaultConfig(stepName);
        }
        
        return StepConfig.builder()
                .stepName(stepName)
                .description(info.description)
                .retryEnabled(retryEnabled)
                .maxRetries(maxRetries)
                .retryDelayMs(retryDelayMs)
                .backoffMultiplier(backoffMultiplier)
                .asyncEnabled(false)
                .timeoutMs(timeoutMs)
                .dependencies(info.dependencies)
                .properties(Map.of("priority", info.priority))
                .build();
    }

    /**
     * Create default config
     */
    private StepConfig createDefaultConfig(String stepName) {
        return StepConfig.builder()
                .stepName(stepName)
                .description("Step: " + stepName)
                .retryEnabled(retryEnabled)
                .maxRetries(maxRetries)
                .retryDelayMs(retryDelayMs)
                .backoffMultiplier(backoffMultiplier)
                .asyncEnabled(false)
                .timeoutMs(timeoutMs)
                .dependencies(new String[0])
                .properties(Map.of("priority", 99))
                .build();
    }

    /**
     * Check if priority-based execution is enabled
     */
    public boolean isPriorityBasedExecution() {
        return "PRIORITY".equalsIgnoreCase(executionOrder);
    }

    /**
     * Get step definition by name
     */
    public StepInfo getStepDefinition(String stepName) {
        return STEPS.get(stepName);
    }

    /**
     * Simple Step Info - Minimal Class
     */
    public static class StepInfo {
        public final int priority;
        public final String description;
        public final String[] dependencies;

        public StepInfo(int priority, String description, String[] dependencies) {
            this.priority = priority;
            this.description = description;
            this.dependencies = dependencies;
        }
    }
}