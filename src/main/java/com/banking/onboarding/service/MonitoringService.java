package com.banking.onboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

/**
 * Monitoring and Metrics Service
 */
@Slf4j
@Service
public class MonitoringService {
    
    private final Map<String, AtomicLong> stepCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> stepDurations = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final AtomicLong totalProcesses = new AtomicLong(0);
    private final AtomicLong successfulProcesses = new AtomicLong(0);
    private final AtomicLong failedProcesses = new AtomicLong(0);
    
    /**
     * Record step execution
     */
    public void recordStepExecution(String stepName, long durationMs, boolean success) {
        stepCounters.computeIfAbsent(stepName, k -> new AtomicLong(0)).incrementAndGet();
        stepDurations.computeIfAbsent(stepName, k -> new AtomicLong(0)).addAndGet(durationMs);
        
        if (!success) {
            errorCounters.computeIfAbsent(stepName, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        log.info("Step {} executed in {}ms, success: {}", stepName, durationMs, success);
    }
    
    /**
     * Record process completion
     */
    public void recordProcessCompletion(boolean success) {
        totalProcesses.incrementAndGet();
        if (success) {
            successfulProcesses.incrementAndGet();
        } else {
            failedProcesses.incrementAndGet();
        }
        
        log.info("Process completed. Success: {}, Total: {}, Success Rate: {}%", 
                success, totalProcesses.get(), getSuccessRate());
    }
    
    /**
     * Get step metrics
     */
    public StepMetrics getStepMetrics(String stepName) {
        long count = stepCounters.getOrDefault(stepName, new AtomicLong(0)).get();
        long totalDuration = stepDurations.getOrDefault(stepName, new AtomicLong(0)).get();
        long errors = errorCounters.getOrDefault(stepName, new AtomicLong(0)).get();
        
        return new StepMetrics(
                stepName,
                count,
                count > 0 ? totalDuration / count : 0,
                totalDuration,
                errors,
                count > 0 ? (double) errors / count * 100 : 0
        );
    }
    
    /**
     * Get overall process metrics
     */
    public ProcessMetrics getProcessMetrics() {
        long total = totalProcesses.get();
        long successful = successfulProcesses.get();
        long failed = failedProcesses.get();
        
        return new ProcessMetrics(
                total,
                successful,
                failed,
                getSuccessRate(),
                LocalDateTime.now()
        );
    }
    
    /**
     * Get all step metrics
     */
    public Map<String, StepMetrics> getAllStepMetrics() {
        Map<String, StepMetrics> metrics = new ConcurrentHashMap<>();
        
        stepCounters.keySet().forEach(stepName -> {
            metrics.put(stepName, getStepMetrics(stepName));
        });
        
        return metrics;
    }
    
    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        stepCounters.clear();
        stepDurations.clear();
        errorCounters.clear();
        totalProcesses.set(0);
        successfulProcesses.set(0);
        failedProcesses.set(0);
        
        log.info("All metrics reset");
    }
    
    private double getSuccessRate() {
        long total = totalProcesses.get();
        if (total == 0) return 0.0;
        return (double) successfulProcesses.get() / total * 100;
    }
    
    /**
     * Step metrics class
     */
    public static class StepMetrics {
        private final String stepName;
        private final long executionCount;
        private final long averageDurationMs;
        private final long totalDurationMs;
        private final long errorCount;
        private final double errorRate;
        
        public StepMetrics(String stepName, long executionCount, long averageDurationMs, 
                         long totalDurationMs, long errorCount, double errorRate) {
            this.stepName = stepName;
            this.executionCount = executionCount;
            this.averageDurationMs = averageDurationMs;
            this.totalDurationMs = totalDurationMs;
            this.errorCount = errorCount;
            this.errorRate = errorRate;
        }
        
        // Getters
        public String getStepName() { return stepName; }
        public long getExecutionCount() { return executionCount; }
        public long getAverageDurationMs() { return averageDurationMs; }
        public long getTotalDurationMs() { return totalDurationMs; }
        public long getErrorCount() { return errorCount; }
        public double getErrorRate() { return errorRate; }
    }
    
    /**
     * Process metrics class
     */
    public static class ProcessMetrics {
        private final long totalProcesses;
        private final long successfulProcesses;
        private final long failedProcesses;
        private final double successRate;
        private final LocalDateTime timestamp;
        
        public ProcessMetrics(long totalProcesses, long successfulProcesses, 
                            long failedProcesses, double successRate, LocalDateTime timestamp) {
            this.totalProcesses = totalProcesses;
            this.successfulProcesses = successfulProcesses;
            this.failedProcesses = failedProcesses;
            this.successRate = successRate;
            this.timestamp = timestamp;
        }
        
        // Getters
        public long getTotalProcesses() { return totalProcesses; }
        public long getSuccessfulProcesses() { return successfulProcesses; }
        public long getFailedProcesses() { return failedProcesses; }
        public double getSuccessRate() { return successRate; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
