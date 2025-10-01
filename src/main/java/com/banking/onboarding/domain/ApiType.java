package com.banking.onboarding.domain;

/**
 * API Type enumeration for different API call types
 */
public enum ApiType {
    DIRECT("direct", "Direct API call without authentication"),
    INTERNAL_APIGEE("internal-apigee", "Internal API call via Apigee with token"),
    EXTERNAL_FENERGO_PROXY("external-fenergo-proxy", "External API call via Fenergo proxy with token"),
    TRANSFORMATION("transformation", "Data transformation API call");
    
    private final String code;
    private final String description;
    
    ApiType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ApiType fromCode(String code) {
        for (ApiType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown API type code: " + code);
    }
}
