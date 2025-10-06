package com.banking.onboarding.repository.collections;

import com.banking.onboarding.model.collections.Step;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Step Repository - Step execution data access
 */
@Repository
public interface StepRepository extends MongoRepository<Step, String> {
    
    // Find by process ID
    List<Step> findByProcessId(String processId);
    
    // Find by correlation ID
    List<Step> findByCorrelationId(String correlationId);
    
    // Find by step ID
    Optional<Step> findByStepId(String stepId);
    
    // Find by step name
    List<Step> findByStepName(String stepName);
    
    // Find by status
    List<Step> findByStatus(Step.StepStatus status);
    
    // Find by process ID and status
    List<Step> findByProcessIdAndStatus(String processId, Step.StepStatus status);
    
    // Find by correlation ID and step name
    List<Step> findByCorrelationIdAndStepName(String correlationId, String stepName);
    
    // Find by process ID and step order
    List<Step> findByProcessIdOrderByStepOrder(String processId);
    
    // Find by step name and status
    List<Step> findByStepNameAndStatus(String stepName, Step.StepStatus status);
    
    // Find by execution mode
    List<Step> findByExecutionMode(String executionMode);
    
    // Find by retry count
    List<Step> findByRetryCountGreaterThan(int retryCount);
    
    // Find by date range
    List<Step> findByStartedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by completed date range
    List<Step> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find failed steps
    List<Step> findByStatusAndErrorMessageIsNotNull(Step.StepStatus status);
    
    // Find by executor class
    List<Step> findByExecutorClass(String executorClass);
    
    // Find by priority
    List<Step> findByPriority(int priority);
    
    // Custom queries
    @Query("{'processId': ?0, 'stepOrder': {$gte: ?1}}")
    List<Step> findByProcessIdAndStepOrderGreaterThanEqual(String processId, int stepOrder);
    
    @Query("{'correlationId': ?0, 'status': {$in: ?1}}")
    List<Step> findByCorrelationIdAndStatusIn(String correlationId, List<Step.StepStatus> statuses);
    
    @Query("{'stepName': ?0, 'status': ?1, 'startedAt': {$gte: ?2}}")
    List<Step> findByStepNameAndStatusSince(String stepName, Step.StepStatus status, LocalDateTime since);
    
    @Query("{'processId': ?0, 'durationMs': {$gte: ?1}}")
    List<Step> findByProcessIdAndDurationGreaterThanEqual(String processId, long durationMs);
    
    // Count queries
    long countByProcessId(String processId);
    long countByStepName(String stepName);
    long countByStatus(Step.StepStatus status);
    long countByProcessIdAndStatus(String processId, Step.StepStatus status);
    
    // Exists queries
    boolean existsByStepId(String stepId);
    boolean existsByProcessIdAndStepName(String processId, String stepName);
}
