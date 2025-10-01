package com.banking.onboarding.controller;

import com.banking.onboarding.service.LoggingDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to demonstrate logging functionality
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/logging-demo")
@RequiredArgsConstructor
public class LoggingDemoController {

    private final LoggingDemoService loggingDemoService;

    /**
     * Demonstrate business event logging
     */
    @PostMapping("/business-event")
    public ResponseEntity<Map<String, Object>> demonstrateBusinessEvent(@RequestBody Map<String, Object> eventData) {
        try {
            loggingDemoService.logBusinessEvent("DEMO_BUSINESS_EVENT", eventData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Business event logged successfully");
            response.put("eventData", eventData);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to demonstrate business event logging: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Demonstrate performance metrics logging
     */
    @PostMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> demonstratePerformanceMetrics(@RequestBody Map<String, Object> metricsData) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Simulate some work
            Thread.sleep(100);
            
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("operation", "DEMO_PERFORMANCE_TEST");
            metrics.put("durationMs", duration);
            metrics.put("metricsData", metricsData);
            
            loggingDemoService.logPerformanceMetrics("DEMO_PERFORMANCE_TEST", duration, metrics);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Performance metrics logged successfully");
            response.put("durationMs", duration);
            response.put("metrics", metrics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to demonstrate performance metrics logging: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Demonstrate external API call logging
     */
    @PostMapping("/external-api-call")
    public ResponseEntity<Map<String, Object>> demonstrateExternalApiCall(@RequestBody Map<String, Object> apiData) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer demo-token");
            
            String body = "{\"demo\": \"external api call\"}";
            
            loggingDemoService.logExternalApiCall("POST", "https://demo-api.com/test", headers, body);
            
            // Simulate response
            Thread.sleep(50);
            
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("Content-Type", "application/json");
            
            loggingDemoService.logExternalApiResponse("POST", "https://demo-api.com/test", 200, responseHeaders, "{\"success\": true}", 50);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "External API call logged successfully");
            response.put("apiData", apiData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to demonstrate external API call logging: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Demonstrate error logging
     */
    @PostMapping("/error-logging")
    public ResponseEntity<Map<String, Object>> demonstrateErrorLogging() {
        try {
            loggingDemoService.demonstrateErrorLogging();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error logging demonstrated successfully");
            response.put("note", "Check logs and database for error events");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to demonstrate error logging: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Demonstrate timeout error logging
     */
    @PostMapping("/timeout-error")
    public ResponseEntity<Map<String, Object>> demonstrateTimeoutError() {
        try {
            loggingDemoService.demonstrateTimeoutErrorLogging();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Timeout error logging demonstrated successfully");
            response.put("note", "Check logs and database for timeout error events");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to demonstrate timeout error logging: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Demonstrate external service error logging
     */
    @PostMapping("/external-service-error")
    public ResponseEntity<Map<String, Object>> demonstrateExternalServiceError() {
        try {
            loggingDemoService.demonstrateExternalServiceErrorLogging();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "External service error logging demonstrated successfully");
            response.put("note", "Check logs and database for external service error events");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to demonstrate external service error logging: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get logging statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLoggingStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("message", "Logging statistics");
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("note", "This endpoint demonstrates the logging system is working");
            stats.put("features", new String[]{
                "Request/Response logging",
                "Business event logging", 
                "Performance metrics logging",
                "External API call logging",
                "Error event logging",
                "Correlation ID tracking",
                "Trace ID tracking"
            });
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get logging stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
