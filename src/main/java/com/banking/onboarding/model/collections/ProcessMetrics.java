package com.banking.onboarding.model.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Process Metrics Collection - Performance and operational metrics
 * Collection: process_metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "process_metrics")
@CompoundIndex(def = "{'processId': 1, 'metricType': 1}")
@CompoundIndex(def = "{'correlationId': 1, 'timestamp': 1}")
@CompoundIndex(def = "{'requestType': 1, 'timestamp': 1}")
public class ProcessMetrics {
    
    @Id
    private String id;
    
    @Indexed
    private String processId;
    
    @Indexed
    private String correlationId;
    
    private String requestType;
    private String metricType;          // PERFORMANCE, THROUGHPUT, ERROR_RATE, etc.
    private String metricCategory;     // PROCESS, STEP, SYSTEM, BUSINESS, etc.
    
    // Performance metrics
    private long totalExecutionTimeMs;
    private long stepExecutionTimeMs;
    private long waitingTimeMs;
    private long processingTimeMs;
    
    // Throughput metrics
    private int totalSteps;
    private int successfulSteps;
    private int failedSteps;
    private int skippedSteps;
    private int retriedSteps;
    
    // Resource metrics
    private long memoryUsedBytes;
    private long cpuTimeMs;
    private long diskIOMs;
    private long networkIOMs;
    
    // Business metrics
    private String businessUnit;
    private String clientId;
    private String tenantId;
    private String applicationId;
    private String priority;
    
    // Quality metrics
    private double successRate;
    private double errorRate;
    private double retryRate;
    private double averageStepTimeMs;
    private double maxStepTimeMs;
    private double minStepTimeMs;
    
    // Custom metrics
    private Map<String, Object> customMetrics;
    private Map<String, Object> businessMetrics;
    private Map<String, Object> technicalMetrics;
    
    // Timestamps
    private LocalDateTime timestamp;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
    
    // System context
    private String systemVersion;
    private String environment;
    private String region;
    private String availabilityZone;
    private String instanceId;
    
    // Comparison metrics
    private double percentile50;      // Median
    private double percentile90;      // 90th percentile
    private double percentile95;      // 95th percentile
    private double percentile99;      // 99th percentile
}
