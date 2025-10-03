package com.banking.onboarding.service;

import com.banking.onboarding.config.EnvironmentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Environment Service - Manages environment-specific operations
 * Provides utilities for environment detection and configuration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentService {
    
    private final Environment environment;
    private final EnvironmentConfig environmentConfig;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    /**
     * Get current active profile
     */
    public String getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Check if running in development environment
     */
    public boolean isDevelopment() {
        return "dev".equals(activeProfile) || "development".equals(activeProfile);
    }
    
    /**
     * Check if running in test environment
     */
    public boolean isTest() {
        return "test".equals(activeProfile) || "testing".equals(activeProfile);
    }
    
    /**
     * Check if running in staging environment
     */
    public boolean isStaging() {
        return "staging".equals(activeProfile);
    }
    
    /**
     * Check if running in production environment
     */
    public boolean isProduction() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }
    
    /**
     * Get environment-specific database configuration
     */
    public EnvironmentConfig.DatabaseConfig getDatabaseConfig() {
        return environmentConfig.getDatabase();
    }
    
    /**
     * Get environment-specific Fenergo configuration
     */
    public EnvironmentConfig.ExternalServicesConfig.FenergoConfig getFenergoConfig() {
        return environmentConfig.getExternalServices().getFenergo();
    }
    
    /**
     * Get environment-specific Apigee configuration
     */
    public EnvironmentConfig.ExternalServicesConfig.ApigeeConfig getApigeeConfig() {
        return environmentConfig.getExternalServices().getApigee();
    }
    
    /**
     * Get environment-specific security configuration
     */
    public EnvironmentConfig.SecurityConfig getSecurityConfig() {
        return environmentConfig.getSecurity();
    }
    
    /**
     * Get environment-specific monitoring configuration
     */
    public EnvironmentConfig.MonitoringConfig getMonitoringConfig() {
        return environmentConfig.getMonitoring();
    }
    
    /**
     * Get all active profiles
     */
    public List<String> getActiveProfiles() {
        return Arrays.asList(environment.getActiveProfiles());
    }
    
    /**
     * Check if a specific profile is active
     */
    public boolean hasProfile(String profile) {
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }
    
    /**
     * Get environment-specific property value
     */
    public String getProperty(String key) {
        return environment.getProperty(key);
    }
    
    /**
     * Get environment-specific property value with default
     */
    public String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }
    
    /**
     * Get environment-specific boolean property
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return environment.getProperty(key, Boolean.class, defaultValue);
    }
    
    /**
     * Get environment-specific integer property
     */
    public int getIntProperty(String key, int defaultValue) {
        return environment.getProperty(key, Integer.class, defaultValue);
    }
    
    /**
     * Get environment-specific long property
     */
    public long getLongProperty(String key, long defaultValue) {
        return environment.getProperty(key, Long.class, defaultValue);
    }
    
    /**
     * Log environment information
     */
    public void logEnvironmentInfo() {
        log.info("=== Environment Information ===");
        log.info("Active Profile: {}", activeProfile);
        log.info("Active Profiles: {}", getActiveProfiles());
        log.info("Environment: {}", environmentConfig.getEnvironment());
        log.info("App Version: {}", environmentConfig.getAppVersion());
        log.info("Is Development: {}", isDevelopment());
        log.info("Is Test: {}", isTest());
        log.info("Is Staging: {}", isStaging());
        log.info("Is Production: {}", isProduction());
        log.info("================================");
    }
    
    /**
     * Get environment-specific step configuration
     */
    public StepEnvironmentConfig getStepEnvironmentConfig() {
        return StepEnvironmentConfig.builder()
                .retryEnabled(getBooleanProperty("onboarding.steps.retry.enabled", true))
                .maxRetries(getIntProperty("onboarding.steps.retry.max-retries", 3))
                .retryDelayMs(getLongProperty("onboarding.steps.retry.delay-ms", 1000))
                .backoffMultiplier(getProperty("onboarding.steps.retry.backoff-multiplier", "2.0"))
                .timeoutMs(getLongProperty("onboarding.steps.timeout-ms", 30000))
                .build();
    }
    
    /**
     * Step environment configuration data class
     */
    @lombok.Data
    @lombok.Builder
    public static class StepEnvironmentConfig {
        private boolean retryEnabled;
        private int maxRetries;
        private long retryDelayMs;
        private String backoffMultiplier;
        private long timeoutMs;
    }
}

