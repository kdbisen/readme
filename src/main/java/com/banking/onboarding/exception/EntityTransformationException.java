package com.banking.onboarding.exception;

public class EntityTransformationException extends RuntimeException {
    
    public EntityTransformationException(String message) {
        super(message);
    }
    
    public EntityTransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}
