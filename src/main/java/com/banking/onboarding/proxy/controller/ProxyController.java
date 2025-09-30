package com.banking.onboarding.proxy.controller;

import com.banking.onboarding.bridge.ApiBridgeService;
import com.banking.onboarding.proxy.FenergoProxyService;
import com.banking.onboarding.proxy.config.ProxyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for proxy management and monitoring
 */
@Slf4j
@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
public class ProxyController {
    
    private final ApiBridgeService apiBridgeService;
    private final FenergoProxyService proxyService;
    private final ProxyConfig proxyConfig;
    
    /**
     * Get proxy status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProxyStatus() {
        log.info("Getting proxy status");
        
        Map<String, Object> status = apiBridgeService.getProxyStatus();
        return ResponseEntity.ok(status);
    }
    
    /**
     * Check if proxy is available
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkProxyHealth() {
        log.info("Checking proxy health");
        
        boolean available = proxyService.isProxyAvailable(proxyConfig.getHealthCheckUrl());
        
        Map<String, Object> health = Map.of(
                "available", available,
                "proxyUrl", proxyConfig.getProxyUrl(),
                "healthCheckUrl", proxyConfig.getHealthCheckUrl(),
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get proxy configuration
     */
    @GetMapping("/config")
    public ResponseEntity<ProxyConfig> getProxyConfig() {
        log.info("Getting proxy configuration");
        return ResponseEntity.ok(proxyConfig);
    }
    
    /**
     * Test proxy call
     */
    @PostMapping("/test/{endpointName}")
    public ResponseEntity<Map<String, Object>> testProxyCall(
            @PathVariable String endpointName,
            @RequestBody(required = false) Object payload) {
        
        log.info("Testing proxy call for endpoint: {}", endpointName);
        
        try {
            var response = apiBridgeService.callApiViaProxy(endpointName, payload);
            
            Map<String, Object> result = Map.of(
                    "endpoint", endpointName,
                    "success", response.isSuccess(),
                    "statusCode", response.getStatusCode(),
                    "responseBody", response.getData(),
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Proxy test failed for endpoint: {}", endpointName, e);
            
            Map<String, Object> error = Map.of(
                    "endpoint", endpointName,
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get all configured Fenergo endpoints
     */
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, ProxyConfig.FenergoEndpointConfig>> getFenergoEndpoints() {
        log.info("Getting Fenergo endpoints configuration");
        
        Map<String, ProxyConfig.FenergoEndpointConfig> endpoints = proxyConfig.getEndpoints();
        return ResponseEntity.ok(endpoints != null ? endpoints : Map.of());
    }
    
    /**
     * Get specific Fenergo endpoint configuration
     */
    @GetMapping("/endpoints/{endpointName}")
    public ResponseEntity<ProxyConfig.FenergoEndpointConfig> getFenergoEndpoint(
            @PathVariable String endpointName) {
        
        log.info("Getting Fenergo endpoint configuration: {}", endpointName);
        
        ProxyConfig.FenergoEndpointConfig config = proxyConfig.getEndpointConfig(endpointName);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(config);
    }
}
