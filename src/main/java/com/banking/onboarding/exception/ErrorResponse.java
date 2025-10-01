package com.banking.onboarding.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enhanced standardized error response with detailed information
 */
@Data
@Builder
public class ErrorResponse {
    private String error;
    private String errorCode;
    private String message;
    private HttpStatus status;
    private String correlationId;
    private String traceId;
    private LocalDateTime timestamp;
    private String path;
    private String method;
    private Map<String, Object> details;
    private Map<String, Object> metadata;
    private String suggestion;
    
    public static ErrorResponse of(String error, String message, HttpStatus status, String correlationId) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String error, String errorCode, String message, HttpStatus status, String correlationId) {
        return ErrorResponse.builder()
                .error(error)
                .errorCode(errorCode)
                .message(message)
                .status(status)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String error, String errorCode, String message, HttpStatus status, 
                                 String correlationId, String suggestion) {
        return ErrorResponse.builder()
                .error(error)
                .errorCode(errorCode)
                .message(message)
                .status(status)
                .correlationId(correlationId)
                .suggestion(suggestion)
                .timestamp(LocalDateTime.now())
                .build();
    }
}