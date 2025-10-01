package com.banking.onboarding.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * DTO for workflow request
 */
@Data
@Builder
public class WorkflowRequestDto {
    
    private String inputData;
    private Map<String, String> metadata;
    private String workflowType;
}
