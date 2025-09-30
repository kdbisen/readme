package com.banking.onboarding.controller;

import com.banking.onboarding.bridge.ApiEndpoint;
import com.banking.onboarding.bridge.ApiEndpointRegistry;
import com.banking.onboarding.bridge.config.EndpointConfigurationLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for endpoint configuration management
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigurationController {
    
    private final ApiEndpointRegistry endpointRegistry;
    private final EndpointConfigurationLoader configLoader;
    
    /**
     * Get all endpoint configurations
     */
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, ApiEndpoint>> getEndpoints() {
        Map<String, ApiEndpoint> endpoints = endpointRegistry.getAllEndpoints();
        return ResponseEntity.ok(endpoints);
    }
    
    /**
     * Get specific endpoint configuration
     */
    @GetMapping("/endpoints/{endpointName}")
    public ResponseEntity<ApiEndpoint> getEndpoint(@PathVariable String endpointName) {
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(endpointName);
        if (endpoint != null) {
            return ResponseEntity.ok(endpoint);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Check if endpoint exists
     */
    @GetMapping("/endpoints/{endpointName}/exists")
    public ResponseEntity<Map<String, Object>> checkEndpoint(@PathVariable String endpointName) {
        boolean exists = endpointRegistry.hasEndpoint(endpointName);
        return ResponseEntity.ok(Map.of("exists", exists, "endpoint", endpointName));
    }
    
    /**
     * Register new endpoint dynamically
     */
    @PostMapping("/endpoints")
    public ResponseEntity<Map<String, String>> registerEndpoint(@RequestBody ApiEndpoint endpoint) {
        endpointRegistry.registerEndpoint(endpoint);
        return ResponseEntity.ok(Map.of("message", "Endpoint registered successfully", "name", endpoint.getName()));
    }
    
    /**
     * Reload endpoint configurations
     */
    @PostMapping("/endpoints/reload")
    public ResponseEntity<Map<String, String>> reloadEndpoints() {
        endpointRegistry.reloadEndpoints();
        return ResponseEntity.ok(Map.of("message", "Endpoints reloaded successfully"));
    }
    
    /**
     * Get configuration statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConfigurationStats() {
        Map<String, Object> stats = endpointRegistry.getEndpointStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get configuration file info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getConfigurationInfo() {
        Map<String, Object> info = Map.of(
                "configFile", "endpoints.properties",
                "configEnabled", true,
                "totalEndpoints", endpointRegistry.getAllEndpoints().size(),
                "authRequiredEndpoints", endpointRegistry.getAllEndpoints().values().stream()
                        .mapToInt(endpoint -> endpoint.getAuthRequired() ? 1 : 0)
                        .sum(),
                "publicEndpoints", endpointRegistry.getAllEndpoints().values().stream()
                        .mapToInt(endpoint -> !endpoint.getAuthRequired() ? 1 : 0)
                        .sum()
        );
        return ResponseEntity.ok(info);
    }
}
