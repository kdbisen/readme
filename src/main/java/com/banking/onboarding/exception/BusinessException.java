package com.banking.onboarding.exception;

/**
 * Base exception for all business-related errors
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String correlationId;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.correlationId = null;
    }
    
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = null;
    }
    
    public BusinessException(String message, String errorCode, String correlationId) {
        super(message);
        this.errorCode = errorCode;
        this.correlationId = correlationId;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.correlationId = null;
    }
    
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.correlationId = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
}

