package com.banking.onboarding.bridge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads endpoint configurations from properties files with environment variable support
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointConfigurationLoader {
    
    @Value("${endpoint.config.file:classpath:endpoints.properties}")
    private String configFile;
    
    @Value("${endpoint.config.enabled:true}")
    private boolean configEnabled;
    
    private final Environment environment;
    private final ResourceLoader resourceLoader;
    
    private Map<String, EndpointConfig> endpointConfigs = new HashMap<>();
    
    @PostConstruct
    public void loadEndpointConfigurations() {
        if (!configEnabled) {
            log.info("Endpoint configuration loading is disabled");
            return;
        }
        
        try {
            Resource resource = resourceLoader.getResource(configFile);
            if (!resource.exists()) {
                log.warn("Endpoint configuration file not found: {}", configFile);
                return;
            }
            
            Properties properties = new Properties();
            try (InputStream inputStream = resource.getInputStream()) {
                properties.load(inputStream);
            }
            
            // Resolve environment variables in properties
            Properties resolvedProperties = resolveEnvironmentVariables(properties);
            
            Map<String, EndpointConfig> configs = parseProperties(resolvedProperties);
            endpointConfigs.putAll(configs);
            
            log.info("Loaded {} endpoint configurations from {} with environment variable resolution", 
                    configs.size(), configFile);
            
            // Log loaded endpoints
            configs.forEach((name, config) -> 
                log.debug("Loaded endpoint: {} -> {} {} (timeout: {}ms, retries: {})", 
                        name, config.getMethod(), config.getPath(), 
                        config.getTimeoutMs(), config.getRetryAttempts()));
            
        } catch (IOException e) {
            log.error("Failed to load endpoint configurations from {}", configFile, e);
        }
    }
    
    /**
     * Resolve environment variables in properties
     * Format: ${ENV_VAR:default_value}
     */
    private Properties resolveEnvironmentVariables(Properties properties) {
        Properties resolved = new Properties();
        
        properties.forEach((key, value) -> {
            String resolvedValue = resolveEnvironmentVariable(value.toString());
            resolved.setProperty(key.toString(), resolvedValue);
        });
        
        return resolved;
    }
    
    /**
     * Resolve a single environment variable
     * Supports format: ${ENV_VAR:default_value}
     */
    private String resolveEnvironmentVariable(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        
        // Pattern: ${ENV_VAR:default_value}
        String pattern = "\\$\\{([^:}]+)(?::([^}]*))?\\}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(value);
        
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            String envVar = m.group(1);
            String defaultValue = m.group(2);
            
            String envValue = environment.getProperty(envVar);
            if (envValue == null) {
                envValue = System.getenv(envVar);
            }
            
            String replacement = (envValue != null) ? envValue : defaultValue;
            m.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        m.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Parse properties into endpoint configurations
     */
    private Map<String, EndpointConfig> parseProperties(Properties properties) {
        Map<String, EndpointConfig> configs = new HashMap<>();
        
        // Group properties by endpoint name
        Map<String, Map<String, String>> endpointGroups = properties.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith("endpoint."))
                .collect(Collectors.groupingBy(
                        entry -> extractEndpointName(entry.getKey().toString()),
                        Collectors.toMap(
                                entry -> extractPropertyName(entry.getKey().toString()),
                                entry -> entry.getValue().toString()
                        )
                ));
        
        // Convert each group to EndpointConfig
        endpointGroups.forEach((endpointName, endpointProps) -> {
            try {
                EndpointConfig config = buildEndpointConfig(endpointName, endpointProps);
                configs.put(endpointName, config);
            } catch (Exception e) {
                log.error("Failed to parse configuration for endpoint: {}", endpointName, e);
            }
        });
        
        return configs;
    }
    
    /**
     * Extract endpoint name from property key
     * endpoint.CREATE_ENTITY.method -> CREATE_ENTITY
     */
    private String extractEndpointName(String key) {
        String[] parts = key.split("\\.");
        return parts.length >= 2 ? parts[1] : "";
    }
    
    /**
     * Extract property name from property key
     * endpoint.CREATE_ENTITY.method -> method
     */
    private String extractPropertyName(String key) {
        String[] parts = key.split("\\.");
        return parts.length >= 3 ? parts[2] : "";
    }
    
    /**
     * Build EndpointConfig from properties
     */
    private EndpointConfig buildEndpointConfig(String endpointName, Map<String, String> props) {
        EndpointConfig.EndpointConfigBuilder builder = EndpointConfig.builder()
                .name(endpointName);
        
        // Set properties
        props.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "method":
                    builder.method(value.toUpperCase());
                    break;
                case "path":
                    builder.path(value);
                    break;
                case "description":
                    builder.description(value);
                    break;
                case "authrequired":
                    builder.authRequired(Boolean.parseBoolean(value));
                    break;
                case "authscope":
                    builder.authScope(value);
                    break;
                case "authtype":
                    builder.authType(value);
                    break;
                case "timeoutms":
                    builder.timeoutMs(Integer.parseInt(value));
                    break;
                case "retryattempts":
                    builder.retryAttempts(Integer.parseInt(value));
                    break;
                default:
                    log.debug("Unknown property: {} for endpoint: {}", key, endpointName);
            }
        });
        
        return builder.build();
    }
    
    /**
     * Get all loaded endpoint configurations
     */
    public Map<String, EndpointConfig> getAllEndpointConfigs() {
        return Map.copyOf(endpointConfigs);
    }
    
    /**
     * Get specific endpoint configuration
     */
    public Optional<EndpointConfig> getEndpointConfig(String endpointName) {
        return Optional.ofNullable(endpointConfigs.get(endpointName));
    }
    
    /**
     * Check if endpoint configuration exists
     */
    public boolean hasEndpointConfig(String endpointName) {
        return endpointConfigs.containsKey(endpointName);
    }
    
    /**
     * Reload configurations (useful for dynamic updates)
     */
    public void reloadConfigurations() {
        log.info("Reloading endpoint configurations from {} with environment variable resolution...", configFile);
        endpointConfigs.clear();
        loadEndpointConfigurations();
    }
    
    /**
     * Get configuration statistics
     */
    public Map<String, Object> getConfigurationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEndpoints", endpointConfigs.size());
        stats.put("authRequiredEndpoints", 
                endpointConfigs.values().stream()
                        .mapToInt(config -> config.getAuthRequired() ? 1 : 0)
                        .sum());
        stats.put("publicEndpoints", 
                endpointConfigs.values().stream()
                        .mapToInt(config -> !config.getAuthRequired() ? 1 : 0)
                        .sum());
        stats.put("configFile", configFile);
        stats.put("configEnabled", configEnabled);
        stats.put("configSource", "Properties File with Environment Variables");
        
        // Add environment variable usage stats
        Map<String, Integer> envVarUsage = new HashMap<>();
        endpointConfigs.values().forEach(config -> {
            if (config.getAuthScope() != null && config.getAuthScope().contains("${")) {
                envVarUsage.merge("authScope", 1, Integer::sum);
            }
            if (config.getTimeoutMs() != null && config.getTimeoutMs().toString().contains("${")) {
                envVarUsage.merge("timeout", 1, Integer::sum);
            }
        });
        stats.put("environmentVariableUsage", envVarUsage);
        
        return stats;
    }
}
