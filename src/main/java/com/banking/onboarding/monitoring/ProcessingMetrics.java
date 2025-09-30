package com.banking.onboarding.monitoring;

import com.banking.onboarding.context.ProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Comprehensive metrics and monitoring service for processing functions
 */
@Slf4j
@Component
public class ProcessingMetrics {
    
    // Step execution metrics
    private final Map<String, StepMetrics> stepMetrics = new ConcurrentHashMap<>();
    
    // Overall process metrics
    private final AtomicLong totalProcesses = new AtomicLong(0);
    private final AtomicLong successfulProcesses = new AtomicLong(0);
    private final AtomicLong failedProcesses = new AtomicLong(0);
    private final LongAdder totalProcessingTime = new LongAdder();
    
    // Error tracking
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    
    /**
     * Record step execution metrics
     */
    public void recordStepExecution(ProcessingContext<?> context, String stepName, long durationMs, boolean success) {
        StepMetrics metrics = stepMetrics.computeIfAbsent(stepName, k -> new StepMetrics());
        
        metrics.totalExecutions.incrementAndGet();
        metrics.totalDuration.add(durationMs);
        
        if (success) {
            metrics.successfulExecutions.incrementAndGet();
        } else {
            metrics.failedExecutions.incrementAndGet();
        }
        
        // Update min/max duration
        metrics.minDuration.updateAndGet(current -> Math.min(current, durationMs));
        metrics.maxDuration.updateAndGet(current -> Math.max(current, durationMs));
        
        log.debug("Recorded metrics for step: {} - Duration: {}ms, Success: {}", stepName, durationMs, success);
    }
    
    /**
     * Record process completion metrics
     */
    public void recordProcessCompletion(ProcessingContext<?> context, boolean success) {
        totalProcesses.incrementAndGet();
        
        if (success) {
            successfulProcesses.incrementAndGet();
        } else {
            failedProcesses.incrementAndGet();
        }
        
        long processingTime = context.getProcessingDurationMs();
        totalProcessingTime.add(processingTime);
        
        log.info("Recorded process completion - Process: {}, Success: {}, Duration: {}ms", 
                context.getProcessId(), success, processingTime);
    }
    
    /**
     * Record error occurrence
     */
    public void recordError(String stepName, String errorCode) {
        String errorKey = stepName + ":" + errorCode;
        errorCounts.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();
        
        log.warn("Recorded error - Step: {}, ErrorCode: {}", stepName, errorCode);
    }
    
    /**
     * Get comprehensive metrics summary
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new ConcurrentHashMap<>();
        
        // Overall process metrics
        long total = totalProcesses.get();
        summary.put("totalProcesses", total);
        summary.put("successfulProcesses", successfulProcesses.get());
        summary.put("failedProcesses", failedProcesses.get());
        summary.put("successRate", total > 0 ? (double) successfulProcesses.get() / total : 0.0);
        summary.put("averageProcessingTime", total > 0 ? totalProcessingTime.sum() / total : 0.0);
        
        // Step-level metrics
        Map<String, Object> stepSummary = new ConcurrentHashMap<>();
        stepMetrics.forEach((stepName, metrics) -> {
            Map<String, Object> stepData = new ConcurrentHashMap<>();
            stepData.put("totalExecutions", metrics.totalExecutions.get());
            stepData.put("successfulExecutions", metrics.successfulExecutions.get());
            stepData.put("failedExecutions", metrics.failedExecutions.get());
            stepData.put("averageDuration", metrics.totalExecutions.get() > 0 ? 
                    metrics.totalDuration.sum() / metrics.totalExecutions.get() : 0.0);
            stepData.put("minDuration", metrics.minDuration.get());
            stepData.put("maxDuration", metrics.maxDuration.get());
            stepData.put("successRate", metrics.totalExecutions.get() > 0 ? 
                    (double) metrics.successfulExecutions.get() / metrics.totalExecutions.get() : 0.0);
            
            stepSummary.put(stepName, stepData);
        });
        summary.put("stepMetrics", stepSummary);
        
        // Error metrics
        summary.put("errorCounts", new ConcurrentHashMap<>(errorCounts));
        
        return summary;
    }
    
    /**
     * Get step-specific metrics
     */
    public Map<String, Object> getStepMetrics(String stepName) {
        StepMetrics metrics = stepMetrics.get(stepName);
        if (metrics == null) {
            return Map.of("error", "No metrics found for step: " + stepName);
        }
        
        Map<String, Object> stepData = new ConcurrentHashMap<>();
        stepData.put("stepName", stepName);
        stepData.put("totalExecutions", metrics.totalExecutions.get());
        stepData.put("successfulExecutions", metrics.successfulExecutions.get());
        stepData.put("failedExecutions", metrics.failedExecutions.get());
        stepData.put("totalDuration", metrics.totalDuration.sum());
        stepData.put("averageDuration", metrics.totalExecutions.get() > 0 ? 
                metrics.totalDuration.sum() / metrics.totalExecutions.get() : 0.0);
        stepData.put("minDuration", metrics.minDuration.get());
        stepData.put("maxDuration", metrics.maxDuration.get());
        stepData.put("successRate", metrics.totalExecutions.get() > 0 ? 
                (double) metrics.successfulExecutions.get() / metrics.totalExecutions.get() : 0.0);
        
        return stepData;
    }
    
    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        stepMetrics.clear();
        totalProcesses.set(0);
        successfulProcesses.set(0);
        failedProcesses.set(0);
        totalProcessingTime.reset();
        errorCounts.clear();
        
        log.info("All metrics have been reset");
    }
    
    /**
     * Step-level metrics holder
     */
    private static class StepMetrics {
        final AtomicLong totalExecutions = new AtomicLong(0);
        final AtomicLong successfulExecutions = new AtomicLong(0);
        final AtomicLong failedExecutions = new AtomicLong(0);
        final LongAdder totalDuration = new LongAdder();
        final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        final AtomicLong maxDuration = new AtomicLong(0);
    }
}
