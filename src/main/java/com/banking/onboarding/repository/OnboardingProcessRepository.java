package com.banking.onboarding.repository;

import com.banking.onboarding.model.OnboardingProcess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OnboardingProcess - Enhanced to handle correlation ID strategy
 */
@Repository
public interface OnboardingProcessRepository extends MongoRepository<OnboardingProcess, String> {
    
    /**
     * Find by process ID - UNIQUE constraint ensures only one result
     */
    Optional<OnboardingProcess> findByProcessId(String processId);
    
    /**
     * Find by correlation ID - Returns ALL processes with same correlation ID
     * This allows multiple processes per correlation ID (retry scenarios, etc.)
     */
    List<OnboardingProcess> findByCorrelationId(String correlationId);
    
    /**
     * Find by correlation ID ordered by creation time (newest first)
     */
    List<OnboardingProcess> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);
    
    /**
     * Find the latest process by correlation ID
     */
    @Query("{ 'correlationId': ?0 }")
    Optional<OnboardingProcess> findLatestByCorrelationId(String correlationId);
    
    /**
     * Find active processes by correlation ID (not completed/failed/cancelled)
     */
    @Query("{ 'correlationId': ?0, 'status': { $nin: ['COMPLETED', 'FAILED', 'CANCELLED'] } }")
    List<OnboardingProcess> findActiveByCorrelationId(String correlationId);
    
    /**
     * Find completed processes by correlation ID
     */
    @Query("{ 'correlationId': ?0, 'status': 'COMPLETED' }")
    List<OnboardingProcess> findCompletedByCorrelationId(String correlationId);
    
    /**
     * Check if correlation ID has any active processes
     */
    @Query(value = "{ 'correlationId': ?0, 'status': { $nin: ['COMPLETED', 'FAILED', 'CANCELLED'] } }", exists = true)
    boolean existsActiveByCorrelationId(String correlationId);
    
    /**
     * Count processes by correlation ID
     */
    long countByCorrelationId(String correlationId);
    
    /**
     * Delete by process ID
     */
    void deleteByProcessId(String processId);
    
    /**
     * Delete all processes by correlation ID
     */
    void deleteByCorrelationId(String correlationId);
}
