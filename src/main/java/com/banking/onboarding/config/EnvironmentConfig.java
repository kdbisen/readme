package com.banking.onboarding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Environment-specific configuration properties
 * Automatically maps properties based on active profile
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "banking.onboarding")
public class EnvironmentConfig {
    
    private String environment;
    private String appVersion;
    private DatabaseConfig database;
    private ExternalServicesConfig externalServices;
    private SecurityConfig security;
    private MonitoringConfig monitoring;
    
    @Data
    public static class DatabaseConfig {
        private String host;
        private int port;
        private String database;
        private String authenticationDatabase;
        private String uri;
        private ConnectionPoolConfig connectionPool;
        
        @Data
        public static class ConnectionPoolConfig {
            private int minSize;
            private int maxSize;
            private int maxWaitTime;
            private int maxConnectionIdleTime;
        }
    }
    
    @Data
    public static class ExternalServicesConfig {
        private FenergoConfig fenergo;
        private ApigeeConfig apigee;
        
        @Data
        public static class FenergoConfig {
            private String entityApiUrl;
            private String logicEngineUrl;
            private String journeyCommandUrl;
            private String tenantId;
            private String journeyTypeFilter;
            private AuthConfig auth;
            
            @Data
            public static class AuthConfig {
                private String tokenServiceUrl;
                private String clientId;
                private String clientSecret;
                private String defaultScope;
                private String scope;
                private boolean cacheEnabled;
            }
        }
        
        @Data
        public static class ApigeeConfig {
            private String transformationEndpoint;
            private String authTokenServiceUrl;
            private String transformationAuthScope;
            private AuthConfig auth;
            
            @Data
            public static class AuthConfig {
                private String clientId;
                private String clientSecret;
                private String defaultScope;
                private boolean cacheEnabled;
            }
        }
    }
    
    @Data
    public static class SecurityConfig {
        private boolean sslEnabled;
        private String keystorePath;
        private String keystorePassword;
        private String truststorePath;
        private String truststorePassword;
        private boolean certificateValidationEnabled;
    }
    
    @Data
    public static class MonitoringConfig {
        private boolean metricsEnabled;
        private boolean healthCheckEnabled;
        private boolean tracingEnabled;
        private String metricsEndpoint;
        private String healthCheckEndpoint;
        private String tracingEndpoint;
    }
}

