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
 * Ultra-Clean Step Configuration Loader with consolidated step definitions
 * All step information (name, priority, description, dependencies) in one place
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
     * 🎯 CONSOLIDATED STEP DEFINITIONS - All step info in one place!
     * 
     * Format: StepName -> StepDefinition(priority, description, dependencies, category, critical)
     * 
     * Benefits:
     * ✅ Easy to read and understand
     * ✅ All step info in one location
     * ✅ Easy to modify priorities and dependencies
     * ✅ Clear dependency chain visualization
     * ✅ Easy to add/remove steps
     */
    private static final Map<String, StepDefinition> STEP_DEFINITIONS = Map.of(
        
        // Step 1: XML Transformation (No dependencies)
        "XML_TO_JSON_TRANSFORMATION", 
        new StepDefinition(
            "XML_TO_JSON_TRANSFORMATION",
            1, // Priority
            "Transform XML data to JSON format via internal Apigee service",
            new String[]{}, // No dependencies
            "TRANSFORMATION",
            true // Critical
        ),
        
        // Step 2: Entity Creation (Depends on XML transformation)
        "FENERGO_ENTITY_CREATION", 
        new StepDefinition(
            "FENERGO_ENTITY_CREATION",
            2, // Priority
            "Create entity in Fenergo system via Entity API",
            new String[]{"XML_TO_JSON_TRANSFORMATION"}, // Depends on step 1
            "ENTITY_MANAGEMENT",
            true // Critical
        ),
        
        // Step 3: Journey Schema Evaluation (Depends on entity creation)
        "FENERGO_JOURNEY_SCHEMA_EVALUATION", 
        new StepDefinition(
            "FENERGO_JOURNEY_SCHEMA_EVALUATION",
            3, // Priority
            "Evaluate journey schema via Fenergo Logic Engine",
            new String[]{"FENERGO_ENTITY_CREATION"}, // Depends on step 2
            "JOURNEY_MANAGEMENT",
            true // Critical
        ),
        
        // Step 4: Journey Launch (Depends on schema evaluation)
        "FENERGO_JOURNEY_LAUNCH", 
        new StepDefinition(
            "FENERGO_JOURNEY_LAUNCH",
            4, // Priority
            "Launch journey via Fenergo Journey Command API",
            new String[]{"FENERGO_JOURNEY_SCHEMA_EVALUATION"}, // Depends on step 3
            "JOURNEY_MANAGEMENT",
            false // Not critical (last step)
        )
        
        // 🚀 To add new steps, just add them here with their priority and dependencies!
        // Example:
        // "DATA_VALIDATION", 
        // new StepDefinition(
        //     "DATA_VALIDATION",
        //     2, // Priority (between XML and Entity)
        //     "Validate transformed JSON data",
        //     new String[]{"XML_TO_JSON_TRANSFORMATION"}, // Depends on XML transformation
        //     "VALIDATION",
        //     true // Critical
        // ),
    );

    /**
     * Load step configurations from consolidated definitions
     */
    public List<StepConfig> loadStepConfigurations() {
        log.info("Loading step configurations with execution order: {}", executionOrder);
        
        return Arrays.stream(stepDefinitions.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::createStepConfig)
                .collect(Collectors.toList());
    }

    /**
     * Create StepConfig from consolidated definition
     */
    private StepConfig createStepConfig(String stepName) {
        StepDefinition definition = STEP_DEFINITIONS.get(stepName);
        
        if (definition == null) {
            log.warn("No definition found for step: {}, using defaults", stepName);
            return createDefaultStepConfig(stepName);
        }
        
        log.debug("Creating config for step: {} with priority: {} and dependencies: {}", 
                stepName, definition.priority, Arrays.toString(definition.dependencies));
        
        return StepConfig.builder()
                .stepName(definition.stepName)
                .description(definition.description)
                .retryEnabled(retryEnabled)
                .maxRetries(maxRetries)
                .retryDelayMs(retryDelayMs)
                .backoffMultiplier(backoffMultiplier)
                .asyncEnabled(false)
                .timeoutMs(timeoutMs)
                .dependencies(definition.dependencies)
                .properties(Map.of(
                    "category", definition.category,
                    "priority", definition.priority,
                    "critical", definition.critical
                ))
                .build();
    }

    /**
     * Create default config for unknown steps
     */
    private StepConfig createDefaultStepConfig(String stepName) {
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
                .properties(Map.of(
                    "category", "GENERAL",
                    "priority", 99,
                    "critical", false
                ))
                .build();
    }

    /**
     * Get execution order preference
     */
    public String getExecutionOrder() {
        return executionOrder;
    }

    /**
     * Check if priority-based execution is enabled
     */
    public boolean isPriorityBasedExecution() {
        return "PRIORITY".equalsIgnoreCase(executionOrder);
    }

    /**
     * Check if order-based execution is enabled
     */
    public boolean isOrderBasedExecution() {
        return "ORDER".equalsIgnoreCase(executionOrder);
    }

    /**
     * Get all step definitions (for debugging/inspection)
     */
    public Map<String, StepDefinition> getAllStepDefinitions() {
        return Map.copyOf(STEP_DEFINITIONS);
    }

    /**
     * Get step definition by name
     */
    public StepDefinition getStepDefinition(String stepName) {
        return STEP_DEFINITIONS.get(stepName);
    }

    /**
     * Print step definitions in a readable format (for debugging)
     */
    public void printStepDefinitions() {
        log.info("=== STEP DEFINITIONS ===");
        log.info("Execution Order: {}", executionOrder);
        log.info("Step Definitions:");
        
        STEP_DEFINITIONS.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getValue().priority, e2.getValue().priority))
                .forEach(entry -> {
                    StepDefinition def = entry.getValue();
                    log.info("  {} | Priority: {} | Dependencies: {} | Category: {} | Critical: {}", 
                            def.stepName, def.priority, Arrays.toString(def.dependencies), 
                            def.category, def.critical);
                });
        log.info("========================");
    }

    /**
     * 🎯 CONSOLIDATED STEP DEFINITION CLASS
     * 
     * Contains all step information in one place:
     * - Step name
     * - Priority (execution order)
     * - Description
     * - Dependencies
     * - Category
     * - Critical flag
     */
    public static class StepDefinition {
        public final String stepName;
        public final int priority;
        public final String description;
        public final String[] dependencies;
        public final String category;
        public final boolean critical;

        public StepDefinition(String stepName, int priority, String description, 
                            String[] dependencies, String category, boolean critical) {
            this.stepName = stepName;
            this.priority = priority;
            this.description = description;
            this.dependencies = dependencies;
            this.category = category;
            this.critical = critical;
        }

        @Override
        public String toString() {
            return String.format("StepDefinition{name='%s', priority=%d, deps=%s, category='%s', critical=%s}", 
                    stepName, priority, Arrays.toString(dependencies), category, critical);
        }
    }
}