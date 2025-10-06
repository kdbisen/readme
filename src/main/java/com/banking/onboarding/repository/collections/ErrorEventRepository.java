package com.banking.onboarding.repository.collections;

import com.banking.onboarding.model.collections.ErrorEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Error Event Repository - Detailed error tracking data access
 */
@Repository
public interface ErrorEventRepository extends MongoRepository<ErrorEvent, String> {
    
    // Find by process ID
    List<ErrorEvent> findByProcessId(String processId);
    
    // Find by step ID
    List<ErrorEvent> findByStepId(String stepId);
    
    // Find by correlation ID
    List<ErrorEvent> findByCorrelationId(String correlationId);
    
    // Find by error type
    List<ErrorEvent> findByErrorType(String errorType);
    
    // Find by error severity
    List<ErrorEvent> findByErrorSeverity(String errorSeverity);
    
    // Find by error category
    List<ErrorEvent> findByErrorCategory(String errorCategory);
    
    // Find by error code
    List<ErrorEvent> findByErrorCode(String errorCode);
    
    // Find by process ID and step ID
    List<ErrorEvent> findByProcessIdAndStepId(String processId, String stepId);
    
    // Find by correlation ID and error type
    List<ErrorEvent> findByCorrelationIdAndErrorType(String correlationId, String errorType);
    
    // Find by error severity and date range
    List<ErrorEvent> findByErrorSeverityAndTimestampBetween(
            String errorSeverity, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );
    
    // Find recoverable errors
    List<ErrorEvent> findByIsRecoverableTrue();
    
    // Find unresolved errors
    List<ErrorEvent> findByIsResolvedFalse();
    
    // Find resolved errors
    List<ErrorEvent> findByIsResolvedTrue();
    
    // Find by retry count
    List<ErrorEvent> findByRetryCountGreaterThan(int retryCount);
    
    // Find by impact level
    List<ErrorEvent> findByImpactLevel(String impactLevel);
    
    // Find user-facing errors
    List<ErrorEvent> findByIsUserFacingTrue();
    
    // Find by exception class
    List<ErrorEvent> findByExceptionClass(String exceptionClass);
    
    // Find by error source
    List<ErrorEvent> findByErrorSource(String errorSource);
    
    // Find by error component
    List<ErrorEvent> findByErrorComponent(String errorComponent);
    
    // Find by date range
    List<ErrorEvent> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by first occurrence date range
    List<ErrorEvent> findByFirstOccurrenceBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by last occurrence date range
    List<ErrorEvent> findByLastOccurrenceBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by resolved date range
    List<ErrorEvent> findByResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by resolved by
    List<ErrorEvent> findByResolvedBy(String resolvedBy);
    
    // Find by resolution action
    List<ErrorEvent> findByResolutionAction(String resolutionAction);
    
    // Find by retry strategy
    List<ErrorEvent> findByRetryStrategy(String retryStrategy);
    
    // Find by environment
    List<ErrorEvent> findByEnvironment(String environment);
    
    // Find by system version
    List<ErrorEvent> findBySystemVersion(String systemVersion);
    
    // Custom queries
    @Query("{'processId': ?0, 'errorSeverity': {$in: ?1}}")
    List<ErrorEvent> findByProcessIdAndErrorSeverityIn(String processId, List<String> severities);
    
    @Query("{'correlationId': ?0, 'isResolved': ?1}")
    List<ErrorEvent> findByCorrelationIdAndIsResolved(String correlationId, boolean isResolved);
    
    @Query("{'errorType': ?0, 'timestamp': {$gte: ?1}}")
    List<ErrorEvent> findByErrorTypeSince(String errorType, LocalDateTime since);
    
    @Query("{'errorSeverity': ?0, 'isResolved': ?1, 'timestamp': {$gte: ?2}}")
    List<ErrorEvent> findByErrorSeverityAndIsResolvedSince(
            String errorSeverity, 
            boolean isResolved, 
            LocalDateTime since
    );
    
    @Query("{'processId': ?0, 'retryCount': {$gte: ?1}}")
    List<ErrorEvent> findByProcessIdAndRetryCountGreaterThanEqual(String processId, int retryCount);
    
    @Query("{'isUserFacing': true, 'timestamp': {$gte: ?0}}")
    List<ErrorEvent> findUserFacingErrorsSince(LocalDateTime since);
    
    @Query("{'errorComponent': ?0, 'errorMethod': ?1}")
    List<ErrorEvent> findByErrorComponentAndMethod(String component, String method);
    
    @Query("{'exceptionClass': ?0, 'timestamp': {$gte: ?1}}")
    List<ErrorEvent> findByExceptionClassSince(String exceptionClass, LocalDateTime since);
    
    // Count queries
    long countByProcessId(String processId);
    long countByStepId(String stepId);
    long countByErrorType(String errorType);
    long countByErrorSeverity(String errorSeverity);
    long countByErrorCategory(String errorCategory);
    long countByIsResolved(boolean isResolved);
    long countByIsRecoverable(boolean isRecoverable);
    long countByIsUserFacing(boolean isUserFacing);
    long countByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByErrorSeverityAndTimestampBetween(String severity, LocalDateTime startDate, LocalDateTime endDate);
    
    // Exists queries
    boolean existsByProcessId(String processId);
    boolean existsByStepId(String stepId);
    boolean existsByCorrelationId(String correlationId);
    boolean existsByErrorCode(String errorCode);
}
