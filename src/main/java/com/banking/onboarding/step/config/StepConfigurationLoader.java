package com.banking.onboarding.step.config;

import com.banking.onboarding.constants.OnboardingConstants.*;
import com.banking.onboarding.constants.OnboardingConstants.StepNames;
import com.banking.onboarding.enums.OnboardingEnums;
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

    @Value("${onboarding.steps.definition:" + ConfigValues.STEP_DEFINITIONS_DEFAULT + "}")
    private String stepDefinitions;

    @Value("${onboarding.steps.execution.order:" +  ConfigValues.EXECUTION_ORDER_DEFAULT + "}")
    private String executionOrder;

    @Value("${onboarding.steps.retry.enabled:" +  ConfigValues.RETRY_ENABLED_DEFAULT + "}")
    private boolean retryEnabled;
    
    @Value("${onboarding.steps.retry.max-attempts:" +ConfigValues.MAX_RETRIES_DEFAULT + "}")
    private int maxRetries;
    
    @Value("${onboarding.steps.retry.delay-ms:" +ConfigValues.RETRY_DELAY_MS_DEFAULT + "}")
    private long retryDelayMs;
    
    @Value("${onboarding.steps.retry.backoff-multiplier:" +ConfigValues.BACKOFF_MULTIPLIER_DEFAULT + "}")
    private double backoffMultiplier;
    
    @Value("${onboarding.steps.timeout-ms:" +ConfigValues.TIMEOUT_MS_DEFAULT + "}")
    private int timeoutMs;

    /**
     * Simple Step Definitions - Minimal Code
     */
    private static final Map<String, StepInfo> STEPS = Map.of(
       StepNames.XML_TO_JSON_TRANSFORMATION,
            new StepInfo(StepPriorities.XML_TO_JSON_TRANSFORMATION,
                       StepDescriptions.XML_TO_JSON_TRANSFORMATION,
                       StepDependencies.XML_TO_JSON_TRANSFORMATION),

       StepNames.FENERGO_ENTITY_CREATION,
            new StepInfo(StepPriorities.FENERGO_ENTITY_CREATION,
                       StepDescriptions.FENERGO_ENTITY_CREATION,
                       StepDependencies.FENERGO_ENTITY_CREATION),

       StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION,
            new StepInfo(StepPriorities.FENERGO_JOURNEY_SCHEMA_EVALUATION,
                       StepDescriptions.FENERGO_JOURNEY_SCHEMA_EVALUATION,
                       StepDependencies.FENERGO_JOURNEY_SCHEMA_EVALUATION),

       StepNames.FENERGO_JOURNEY_LAUNCH,
            new StepInfo(StepPriorities.FENERGO_JOURNEY_LAUNCH,
                       StepDescriptions.FENERGO_JOURNEY_LAUNCH,
                       StepDependencies.FENERGO_JOURNEY_LAUNCH)
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
                .dependencies(new String[0])
                .properties(Map.of("priority", 99))
                .build();
    }

    /**
     * Check if priority-based execution is enabled
     */
    public boolean isPriorityBasedExecution() {
        return ExecutionOrders.PRIORITY.equalsIgnoreCase(executionOrder);
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