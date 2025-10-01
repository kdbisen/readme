package com.banking.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEntityResponse {
    private String processId;
    private String correlationId;
    private String status;
    private String message;
    private RequestType requestType;
    private LocalDateTime timestamp;
}
