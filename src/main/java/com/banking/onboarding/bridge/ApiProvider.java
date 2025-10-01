package com.banking.onboarding.bridge;

/**
 * API Provider enumeration for different external API services
 */
public enum ApiProvider {
    
    APIGEE("apigee", "Internal Company APIs", "Apigee Token Service"),
    FENERGO("fenergo", "External Company APIs", "Fenergo Token Service");
    
    private final String code;
    private final String description;
    private final String tokenServiceName;
    
    ApiProvider(String code, String description, String tokenServiceName) {
        this.code = code;
        this.description = description;
        this.tokenServiceName = tokenServiceName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getTokenServiceName() {
        return tokenServiceName;
    }
    
    /**
     * Get API provider by code
     */
    public static ApiProvider fromCode(String code) {
        for (ApiProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown API provider code: " + code);
    }
}
