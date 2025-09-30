package com.banking.onboarding.exception;

public class FenergoIntegrationException extends RuntimeException {
    
    public FenergoIntegrationException(String message) {
        super(message);
    }
    
    public FenergoIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
