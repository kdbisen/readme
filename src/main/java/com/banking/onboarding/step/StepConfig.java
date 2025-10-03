package com.banking.onboarding.step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Step Configuration - Defines how a step should be executed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepConfig {
    
    private String stepName;
    private String description;
    private String[] dependencies;
    private Map<String, Object> properties;
    
    /**
     * Default configuration
     */
    public static StepConfig defaultConfig(String stepName) {
        return StepConfig.builder()
                .stepName(stepName)
                .dependencies(new String[0])
                .build();
    }
}





