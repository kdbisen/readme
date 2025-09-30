package com.banking.onboarding.controller;

import com.banking.onboarding.bridge.ApiBridgeService;
import com.banking.onboarding.bridge.ApiEndpoint;
import com.banking.onboarding.bridge.ApiRequest;
import com.banking.onboarding.bridge.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for dynamic API bridge operations
 */
@Slf4j
@RestController
@RequestMapping("/api/bridge")
@RequiredArgsConstructor
public class ApiBridgeController {
    
    private final ApiBridgeService bridgeService;
    
    /**
     * Call Fenergo API by endpoint name
     */
    @PostMapping("/call/{endpointName}")
    public ResponseEntity<ApiResponse> callApi(
            @PathVariable String endpointName,
            @RequestBody(required = false) Object payload,
            @RequestHeader Map<String, String> headers) {
        
        log.info("Bridge API call requested for endpoint: {}", endpointName);
        
        ApiResponse response = bridgeService.callApi(endpointName, payload, Map.copyOf(headers));
        
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    /**
     * Call Fenergo API with direct URL
     */
    @PostMapping("/call-direct")
    public ResponseEntity<ApiResponse> callApiDirect(@RequestBody ApiRequest request) {
        
        log.info("Direct bridge API call requested: {} {}", request.getMethod(), request.getEndpoint());
        
        ApiResponse response = bridgeService.callApiDirect(
                request.getEndpoint(),
                request.getMethod(),
                request.getPayload(),
                request.getHeaders(),
                request.getQueryParams()
        );
        
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    /**
     * Get all available endpoints
     */
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, ApiEndpoint>> getEndpoints() {
        Map<String, ApiEndpoint> endpoints = bridgeService.getAvailableEndpoints();
        return ResponseEntity.ok(endpoints);
    }
    
    /**
     * Check if endpoint exists
     */
    @GetMapping("/endpoints/{endpointName}/exists")
    public ResponseEntity<Map<String, Object>> checkEndpoint(@PathVariable String endpointName) {
        boolean exists = bridgeService.hasEndpoint(endpointName);
        return ResponseEntity.ok(Map.of("exists", exists, "endpoint", endpointName));
    }
    
    /**
     * Register new endpoint dynamically
     */
    @PostMapping("/endpoints")
    public ResponseEntity<Map<String, String>> registerEndpoint(@RequestBody ApiEndpoint endpoint) {
        bridgeService.registerEndpoint(endpoint);
        return ResponseEntity.ok(Map.of("message", "Endpoint registered successfully", "name", endpoint.getName()));
    }
}
