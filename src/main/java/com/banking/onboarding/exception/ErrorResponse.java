package com.banking.onboarding.exception;

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
public class ErrorResponse {
    
    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private String correlationId;
    private String traceId;
    private List<String> details;
    
    public static ErrorResponse of(String error, String message, int status, String path) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
    
    public static ErrorResponse of(String error, String message, int status, String path, List<String> details) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(details)
                .build();
    }
}
