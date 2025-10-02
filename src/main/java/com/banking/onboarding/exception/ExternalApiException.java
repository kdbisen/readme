package com.banking.onboarding.exception;

/**
 * Exception for external API call errors (Fenergo, Apigee, etc.)
 */
public class ExternalApiException extends BusinessException {
    private final String apiProvider;
    private final String endpoint;
    private final int httpStatus;
    
    public ExternalApiException(String message, String apiProvider, String endpoint) {
        super(message, "EXTERNAL_API_ERROR");
        this.apiProvider = apiProvider;
        this.endpoint = endpoint;
        this.httpStatus = 0;
    }
    
    public ExternalApiException(String message, String apiProvider, String endpoint, int httpStatus) {
        super(message, "EXTERNAL_API_ERROR");
        this.apiProvider = apiProvider;
        this.endpoint = endpoint;
        this.httpStatus = httpStatus;
    }
    
    public ExternalApiException(String message, String apiProvider, String endpoint, String correlationId) {
        super(message, "EXTERNAL_API_ERROR", correlationId);
        this.apiProvider = apiProvider;
        this.endpoint = endpoint;
        this.httpStatus = 0;
    }
    
    public ExternalApiException(String message, String apiProvider, String endpoint, int httpStatus, String correlationId) {
        super(message, "EXTERNAL_API_ERROR", correlationId);
        this.apiProvider = apiProvider;
        this.endpoint = endpoint;
        this.httpStatus = httpStatus;
    }
    
    public ExternalApiException(String message, String apiProvider, String endpoint, Throwable cause) {
        super(message, "EXTERNAL_API_ERROR", cause);
        this.apiProvider = apiProvider;
        this.endpoint = endpoint;
        this.httpStatus = 0;
    }
    
    public String getApiProvider() {
        return apiProvider;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}

