package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponse {
    private String processId;
    private String correlationId;
    private String requestType;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
