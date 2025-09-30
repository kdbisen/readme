package com.banking.onboarding.controller;

import com.banking.onboarding.monitoring.ProcessingMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for monitoring and metrics endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    
    private final ProcessingMetrics metrics;
    
    /**
     * Get comprehensive metrics summary
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        log.info("Retrieving comprehensive metrics summary");
        
        Map<String, Object> metricsSummary = metrics.getMetricsSummary();
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "timestamp", System.currentTimeMillis(),
            "metrics", metricsSummary
        ));
    }
    
    /**
     * Get step-specific metrics
     */
    @GetMapping("/metrics/step/{stepName}")
    public ResponseEntity<Map<String, Object>> getStepMetrics(@PathVariable String stepName) {
        log.info("Retrieving metrics for step: {}", stepName);
        
        Map<String, Object> stepMetrics = metrics.getStepMetrics(stepName);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "timestamp", System.currentTimeMillis(),
            "stepName", stepName,
            "metrics", stepMetrics
        ));
    }
    
    /**
     * Reset all metrics
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<Map<String, Object>> resetMetrics() {
        log.info("Resetting all metrics");
        
        metrics.resetMetrics();
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "All metrics have been reset",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Health check endpoint with metrics
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> metricsSummary = metrics.getMetricsSummary();
        
        long totalProcesses = (Long) metricsSummary.get("totalProcesses");
        long failedProcesses = (Long) metricsSummary.get("failedProcesses");
        double successRate = (Double) metricsSummary.get("successRate");
        
        String healthStatus = "UP";
        if (successRate < 0.8 && totalProcesses > 10) {
            healthStatus = "DEGRADED";
        }
        if (successRate < 0.5 && totalProcesses > 5) {
            healthStatus = "DOWN";
        }
        
        return ResponseEntity.ok(Map.of(
            "status", healthStatus,
            "timestamp", System.currentTimeMillis(),
            "totalProcesses", totalProcesses,
            "failedProcesses", failedProcesses,
            "successRate", successRate,
            "metrics", metricsSummary
        ));
    }
}
