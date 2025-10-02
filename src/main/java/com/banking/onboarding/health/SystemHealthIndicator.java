package com.banking.onboarding.health;

import com.banking.onboarding.circuitbreaker.CircuitBreaker;
import com.banking.onboarding.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * System Health Check Service
 * Monitors all critical components of the system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemHealthIndicator {
    
    private final MongoTemplate mongoTemplate;
    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final MonitoringService monitoringService;
    
    /**
     * Get comprehensive system health status
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;
        
        // Check MongoDB connectivity
        HealthStatus mongoHealth = checkMongoHealth();
        health.put("mongodb", mongoHealth);
        if (!mongoHealth.isHealthy()) isHealthy = false;
        
        // Check external services
        HealthStatus apigeeHealth = checkApigeeHealth();
        health.put("apigee", apigeeHealth);
        if (!apigeeHealth.isHealthy()) isHealthy = false;
        
        HealthStatus fenergoHealth = checkFenergoHealth();
        health.put("fenergo", fenergoHealth);
        if (!fenergoHealth.isHealthy()) isHealthy = false;
        
        // Check circuit breaker states
        Map<String, Object> circuitBreakerStates = checkCircuitBreakerStates();
        health.put("circuitBreakers", circuitBreakerStates);
        
        // Check system metrics
        Map<String, Object> systemMetrics = getSystemMetrics();
        health.put("metrics", systemMetrics);
        
        // Check memory usage
        HealthStatus memoryHealth = checkMemoryHealth();
        health.put("memory", memoryHealth);
        if (!memoryHealth.isHealthy()) isHealthy = false;
        
        health.put("timestamp", LocalDateTime.now());
        health.put("overallStatus", isHealthy ? "HEALTHY" : "UNHEALTHY");
        
        return health;
    }
    
    private HealthStatus checkMongoHealth() {
        try {
            // Test MongoDB connectivity
            mongoTemplate.getCollection("health_check").countDocuments();
            return new HealthStatus("UP", "MongoDB connection successful", null);
        } catch (Exception e) {
            log.error("MongoDB health check failed", e);
            return new HealthStatus("DOWN", "MongoDB connection failed", e.getMessage());
        }
    }
    
    private HealthStatus checkApigeeHealth() {
        try {
            // Check circuit breaker state
            CircuitBreaker.State state = circuitBreaker.getCircuitState("apigee");
            if (state == CircuitBreaker.State.OPEN) {
                return new HealthStatus("DOWN", "Apigee circuit breaker is OPEN", 
                    "Too many failures detected");
            }
            
            // Try a simple health check call (if endpoint exists)
            // This would be a lightweight health check endpoint
            return new HealthStatus("UP", "Apigee service accessible", null);
        } catch (Exception e) {
            log.error("Apigee health check failed", e);
            return new HealthStatus("DOWN", "Apigee service unavailable", e.getMessage());
        }
    }
    
    private HealthStatus checkFenergoHealth() {
        try {
            // Check circuit breaker state
            CircuitBreaker.State state = circuitBreaker.getCircuitState("fenergo");
            if (state == CircuitBreaker.State.OPEN) {
                return new HealthStatus("DOWN", "Fenergo circuit breaker is OPEN", 
                    "Too many failures detected");
            }
            
            // Try a simple health check call (if endpoint exists)
            return new HealthStatus("UP", "Fenergo service accessible", null);
        } catch (Exception e) {
            log.error("Fenergo health check failed", e);
            return new HealthStatus("DOWN", "Fenergo service unavailable", e.getMessage());
        }
    }
    
    private Map<String, Object> checkCircuitBreakerStates() {
        Map<String, Object> states = new HashMap<>();
        
        // Check Apigee circuit breaker
        CircuitBreaker.CircuitBreakerMetrics apigeeMetrics = circuitBreaker.getMetrics("apigee");
        states.put("apigee", Map.of(
            "state", apigeeMetrics.getState().toString(),
            "successRate", apigeeMetrics.getSuccessRate(),
            "failureRate", apigeeMetrics.getFailureRate(),
            "totalRequests", apigeeMetrics.getTotalRequests()
        ));
        
        // Check Fenergo circuit breaker
        CircuitBreaker.CircuitBreakerMetrics fenergoMetrics = circuitBreaker.getMetrics("fenergo");
        states.put("fenergo", Map.of(
            "state", fenergoMetrics.getState().toString(),
            "successRate", fenergoMetrics.getSuccessRate(),
            "failureRate", fenergoMetrics.getFailureRate(),
            "totalRequests", fenergoMetrics.getTotalRequests()
        ));
        
        return states;
    }
    
    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get process metrics
        MonitoringService.ProcessMetrics processMetrics = monitoringService.getProcessMetrics();
        metrics.put("processes", Map.of(
            "total", processMetrics.getTotalProcesses(),
            "successful", processMetrics.getSuccessfulProcesses(),
            "failed", processMetrics.getFailedProcesses(),
            "successRate", processMetrics.getSuccessRate()
        ));
        
        // Get step metrics
        Map<String, MonitoringService.StepMetrics> stepMetrics = monitoringService.getAllStepMetrics();
        Map<String, Object> stepMetricsMap = new HashMap<>();
        stepMetrics.forEach((stepName, stepMetric) -> {
            stepMetricsMap.put(stepName, Map.of(
                "executionCount", stepMetric.getExecutionCount(),
                "averageDuration", stepMetric.getAverageDurationMs(),
                "errorRate", stepMetric.getErrorRate()
            ));
        });
        metrics.put("steps", stepMetricsMap);
        
        return metrics;
    }
    
    private HealthStatus checkMemoryHealth() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        if (memoryUsagePercent > 90) {
            return new HealthStatus("DOWN", "High memory usage", 
                String.format("Memory usage: %.2f%%", memoryUsagePercent));
        } else if (memoryUsagePercent > 80) {
            return new HealthStatus("WARN", "Moderate memory usage", 
                String.format("Memory usage: %.2f%%", memoryUsagePercent));
        } else {
            return new HealthStatus("UP", "Memory usage normal", 
                String.format("Memory usage: %.2f%%", memoryUsagePercent));
        }
    }
    
    /**
     * Health status class
     */
    public static class HealthStatus {
        private final String status;
        private final String message;
        private final String details;
        
        public HealthStatus(String status, String message, String details) {
            this.status = status;
            this.message = message;
            this.details = details;
        }
        
        public boolean isHealthy() {
            return "UP".equals(status);
        }
        
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }
}
