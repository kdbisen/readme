package com.banking.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEntityResponse {
    
    private String processId;
    private String correlationId;
    private String status;
    private String message;
    private String estimatedCompletionTime;
    
}
