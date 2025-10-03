package com.banking.onboarding.step.util;

import com.banking.onboarding.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Environment-aware utility for step implementations
 * Provides environment-specific configurations and behaviors
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnvironmentAwareStepUtil {
    
    private final EnvironmentService environmentService;
    
    /**
     * Get environment-specific timeout for API calls
     */
    public long getApiTimeout() {
        if (environmentService.isProduction()) {
            return 60000; // 60 seconds for production
        } else if (environmentService.isStaging()) {
            return 30000; // 30 seconds for staging
        } else if (environmentService.isTest()) {
            return 20000; // 20 seconds for test
        } else {
            return 15000; // 15 seconds for development
        }
    }
    
    /**
     * Get environment-specific retry count
     */
    public int getRetryCount() {
        if (environmentService.isProduction()) {
            return 5; // More retries for production
        } else if (environmentService.isStaging()) {
            return 3; // Standard retries for staging
        } else if (environmentService.isTest()) {
            return 2; // Fewer retries for test
        } else {
            return 1; // Minimal retries for development
        }
    }
    
    /**
     * Check if detailed logging should be enabled
     */
    public boolean isDetailedLoggingEnabled() {
        return environmentService.isDevelopment() || environmentService.isTest();
    }
    
    /**
     * Check if mock services should be used
     */
    public boolean shouldUseMockServices() {
        return environmentService.isDevelopment() || environmentService.isTest();
    }
    
    /**
     * Get environment-specific Fenergo base URL
     */
    public String getFenergoBaseUrl() {
        return environmentService.getFenergoConfig().getEntityApiUrl()
                .replace("/entity", "");
    }
    
    /**
     * Get environment-specific Apigee base URL
     */
    public String getApigeeBaseUrl() {
        return environmentService.getApigeeConfig().getTransformationEndpoint()
                .replace("/transform", "");
    }
    
    /**
     * Log environment-specific step information
     */
    public void logStepEnvironmentInfo(String stepName, String correlationId) {
        if (isDetailedLoggingEnabled()) {
            log.info("[CORRELATION:{}] Step: {} - Environment: {} - Profile: {}", 
                    correlationId, stepName, 
                    environmentService.getActiveProfile(),
                    environmentService.getActiveProfile());
        }
    }
    
    /**
     * Get environment-specific error message
     */
    public String getEnvironmentSpecificErrorMessage(String baseMessage) {
        if (environmentService.isProduction()) {
            return "Service temporarily unavailable. Please try again later.";
        } else {
            return baseMessage + " (Environment: " + environmentService.getActiveProfile() + ")";
        }
    }
}

