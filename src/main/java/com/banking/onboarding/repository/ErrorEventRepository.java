package com.banking.onboarding.repository;

import com.banking.onboarding.model.ErrorEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ErrorEvent model
 */
@Repository
public interface ErrorEventRepository extends MongoRepository<ErrorEvent, String> {

    /**
     * Find error events by correlation ID
     */
    List<ErrorEvent> findByCorrelationIdOrderByTimestampDesc(String correlationId);

    /**
     * Find error events by trace ID
     */
    List<ErrorEvent> findByTraceIdOrderByTimestampDesc(String traceId);

    /**
     * Find error events by error type
     */
    List<ErrorEvent> findByErrorTypeOrderByTimestampDesc(String errorType);

    /**
     * Find error events by service name
     */
    List<ErrorEvent> findByServiceNameOrderByTimestampDesc(String serviceName);

    /**
     * Find unresolved error events
     */
    List<ErrorEvent> findByResolvedFalseOrderByTimestampDesc();

    /**
     * Find error events by severity
     */
    List<ErrorEvent> findBySeverityOrderByTimestampDesc(String severity);

    /**
     * Find error events within time range
     */
    List<ErrorEvent> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find error events by correlation ID and error type
     */
    List<ErrorEvent> findByCorrelationIdAndErrorTypeOrderByTimestampDesc(String correlationId, String errorType);

    /**
     * Count error events by error type
     */
    long countByErrorType(String errorType);

    /**
     * Count unresolved error events
     */
    long countByResolvedFalse();

    /**
     * Count error events by severity
     */
    long countBySeverity(String severity);

    /**
     * Find recent error events (last N hours)
     */
    @Query("{ 'timestamp': { $gte: ?0 } }")
    List<ErrorEvent> findRecentErrorEvents(LocalDateTime since);

    /**
     * Find error events by exception type
     */
    List<ErrorEvent> findByExceptionTypeOrderByTimestampDesc(String exceptionType);

    /**
     * Find error events by environment
     */
    List<ErrorEvent> findByEnvironmentOrderByTimestampDesc(String environment);

    /**
     * Find error events by version
     */
    List<ErrorEvent> findByVersionOrderByTimestampDesc(String version);

    /**
     * Find error events with specific context data
     */
    @Query("{ 'contextData.?0': ?1 }")
    List<ErrorEvent> findByContextDataKeyValue(String key, Object value);

    /**
     * Find error events by correlation ID and service name
     */
    List<ErrorEvent> findByCorrelationIdAndServiceNameOrderByTimestampDesc(String correlationId, String serviceName);

    /**
     * Find error events by method name
     */
    List<ErrorEvent> findByMethodNameOrderByTimestampDesc(String methodName);

    /**
     * Find error events by correlation ID and severity
     */
    List<ErrorEvent> findByCorrelationIdAndSeverityOrderByTimestampDesc(String correlationId, String severity);
}
