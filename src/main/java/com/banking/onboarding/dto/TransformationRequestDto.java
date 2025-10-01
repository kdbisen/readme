package com.banking.onboarding.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * DTO for transformation request
 */
@Data
@Builder
public class TransformationRequestDto {
    
    private String inputData;
    private String inputFormat;
    private String outputFormat;
    private Map<String, String> additionalHeaders;
}
