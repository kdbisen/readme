package com.banking.onboarding.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response
 */
@Data
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private HttpStatus status;
    private String correlationId;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, Object> details;
    
    public static ErrorResponse of(String error, String message, HttpStatus status, String correlationId) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}