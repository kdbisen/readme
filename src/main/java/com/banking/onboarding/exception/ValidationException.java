package com.banking.onboarding.exception;

/**
 * Exception for validation-related errors
 */
public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
    
    public ValidationException(String message, String correlationId) {
        super(message, "VALIDATION_ERROR", correlationId);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", cause);
    }
}





