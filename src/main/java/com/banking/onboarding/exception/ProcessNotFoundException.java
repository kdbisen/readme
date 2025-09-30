package com.banking.onboarding.exception;

public class ProcessNotFoundException extends RuntimeException {
    
    public ProcessNotFoundException(String message) {
        super(message);
    }
    
    public ProcessNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
