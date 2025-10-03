package com.banking.onboarding.step.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Configuration classes for step properties and metadata
 * Replaces hardcoded HashMap usage with proper typed classes
 */
public class StepConfigurationData {

    // ===========================================
    // STEP PROPERTIES CONFIGURATION
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepProperties {
        private String category;
        private Integer priority;
        private Boolean critical;
        private String description;
        private Map<String, Object> additionalProperties;
        
        public static StepProperties createDefault() {
            return StepProperties.builder()
                    .category("GENERAL")
                    .priority(99)
                    .critical(false)
                    .description("Default step")
                    .build();
        }
        
        public static StepProperties create(String category, Integer priority, Boolean critical, String description) {
            return StepProperties.builder()
                    .category(category)
                    .priority(priority)
                    .critical(critical)
                    .description(description)
                    .build();
        }
    }
    
    // ===========================================
    // STEP METADATA CONFIGURATION
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepMetadata {
        private Integer stepNumber;
        private Integer totalSteps;
        private String stepNumberText;
        private String stepNumberWithDescription;
        private Boolean isFirstStep;
        private Boolean isLastStep;
        private String nextStep;
        private String previousStep;
        private String executionOrder;
        private Map<String, Object> additionalMetadata;
        
        public static StepMetadata create(Integer stepNumber, Integer totalSteps, String stepName, String description) {
            return StepMetadata.builder()
                    .stepNumber(stepNumber)
                    .totalSteps(totalSteps)
                    .stepNumberText(String.format("Step %d", stepNumber))
                    .stepNumberWithDescription(String.format("Step %d: %s", stepNumber, description))
                    .isFirstStep(stepNumber == 1)
                    .isLastStep(stepNumber.equals(totalSteps))
                    .build();
        }
    }
    
    // ===========================================
    // STEP CONTEXT DATA
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepContextData {
        private String correlationId;
        private String processId;
        private Object inputData;
        private Map<String, Object> stepResults;
        private StepMetadata metadata;
        private Map<String, Object> additionalContext;
        
        public static StepContextData create(String correlationId, String processId, Object inputData) {
            return StepContextData.builder()
                    .correlationId(correlationId)
                    .processId(processId)
                    .inputData(inputData)
                    .build();
        }
    }
    
    // ===========================================
    // STEP EXECUTION RESULT DATA
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepExecutionResult {
        private String stepName;
        private Integer stepNumber;
        private Boolean success;
        private Object data;
        private String errorMessage;
        private Long durationMs;
        private String status;
        private Map<String, Object> metadata;
        
        public static StepExecutionResult createSuccess(String stepName, Integer stepNumber, Object data) {
            return StepExecutionResult.builder()
                    .stepName(stepName)
                    .stepNumber(stepNumber)
                    .success(true)
                    .data(data)
                    .status("COMPLETED")
                    .build();
        }
        
        public static StepExecutionResult createFailure(String stepName, Integer stepNumber, String errorMessage) {
            return StepExecutionResult.builder()
                    .stepName(stepName)
                    .stepNumber(stepNumber)
                    .success(false)
                    .errorMessage(errorMessage)
                    .status("FAILED")
                    .build();
        }
    }
    
    // ===========================================
    // STEP DEPENDENCY CONFIGURATION
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDependency {
        private String stepName;
        private String[] dependencies;
        private String[] dependents;
        private Boolean isRequired;
        private String dependencyType;
        
        public static StepDependency create(String stepName, String[] dependencies) {
            return StepDependency.builder()
                    .stepName(stepName)
                    .dependencies(dependencies)
                    .isRequired(true)
                    .dependencyType("REQUIRED")
                    .build();
        }
    }
    
    // ===========================================
    // STEP RETRY CONFIGURATION
    // ===========================================
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepRetryConfig {
        private Boolean retryEnabled;
        private Integer maxRetries;
        private Long retryDelayMs;
        private Double backoffMultiplier;
        private Long timeoutMs;
        private String[] retryableExceptions;
        
        public static StepRetryConfig createDefault() {
            return StepRetryConfig.builder()
                    .retryEnabled(true)
                    .maxRetries(3)
                    .retryDelayMs(1000L)
                    .backoffMultiplier(2.0)
                    .timeoutMs(30000L)
                    .retryableExceptions(new String[]{"java.net.ConnectException", "java.net.SocketTimeoutException"})
                    .build();
        }
    }
}



