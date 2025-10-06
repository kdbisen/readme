package com.banking.onboarding.exception;

/**
 * Exception for process-related errors
 */
public class ProcessException extends BusinessException {
    
    public ProcessException(String message) {
        super(message, "PROCESS_ERROR");
    }
    
    public ProcessException(String message, String correlationId) {
        super(message, "PROCESS_ERROR", correlationId);
    }
    
    public ProcessException(String message, Throwable cause) {
        super(message, "PROCESS_ERROR", cause);
    }
}






