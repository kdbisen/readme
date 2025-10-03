package com.banking.onboarding.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Profile-specific configuration classes
 * Each profile can have its own configuration overrides
 */
public class ProfileConfigurations {

    /**
     * Development Environment Configuration
     */
    @Slf4j
    @Configuration
    @Profile("dev")
    public static class DevConfiguration {
        
        public DevConfiguration() {
            log.info("Initializing Development Environment Configuration");
        }
        
        // Development-specific beans and configurations can be added here
        // For example: Mock services, debug endpoints, etc.
    }

    /**
     * Test Environment Configuration
     */
    @Slf4j
    @Configuration
    @Profile("test")
    public static class TestConfiguration {
        
        public TestConfiguration() {
            log.info("Initializing Test Environment Configuration");
        }
        
        // Test-specific beans and configurations can be added here
        // For example: Test databases, mock services, etc.
    }

    /**
     * Staging Environment Configuration
     */
    @Slf4j
    @Configuration
    @Profile("staging")
    public static class StagingConfiguration {
        
        public StagingConfiguration() {
            log.info("Initializing Staging Environment Configuration");
        }
        
        // Staging-specific beans and configurations can be added here
        // For example: Staging databases, external service configurations, etc.
    }

    /**
     * Production Environment Configuration
     */
    @Slf4j
    @Configuration
    @Profile("prod")
    public static class ProdConfiguration {
        
        public ProdConfiguration() {
            log.info("Initializing Production Environment Configuration");
        }
        
        // Production-specific beans and configurations can be added here
        // For example: Production databases, security configurations, etc.
    }
}
