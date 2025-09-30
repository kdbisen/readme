package com.banking.onboarding.proxy;

/**
 * Exception for proxy-related errors
 */
public class ProxyException extends RuntimeException {
    
    public ProxyException(String message) {
        super(message);
    }
    
    public ProxyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProxyException(Throwable cause) {
        super(cause);
    }
}
