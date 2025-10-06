package com.banking.onboarding.repository.collections;

import com.banking.onboarding.model.collections.Process;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Process Repository - Core process data access
 */
@Repository
public interface ProcessRepository extends MongoRepository<Process, String> {
    
    // Find by correlation ID
    Optional<Process> findByCorrelationId(String correlationId);
    
    // Find by process ID
    Optional<Process> findByProcessId(String processId);
    
    // Find by status
    List<Process> findByStatus(Process.ProcessStatus status);
    
    // Find by request type
    List<Process> findByRequestType(String requestType);
    
    // Find by tenant ID
    List<Process> findByTenantId(String tenantId);
    
    // Find by user ID
    List<Process> findByUserId(String userId);
    
    // Find by date range
    List<Process> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by status and date range
    List<Process> findByStatusAndCreatedAtBetween(
            Process.ProcessStatus status, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );
    
    // Find by correlation ID and request type
    List<Process> findByCorrelationIdAndRequestType(String correlationId, String requestType);
    
    // Find by tenant ID and status
    List<Process> findByTenantIdAndStatus(String tenantId, Process.ProcessStatus status);
    
    // Find by business unit
    List<Process> findByBusinessUnit(String businessUnit);
    
    // Find by priority
    List<Process> findByPriority(String priority);
    
    // Custom queries
    @Query("{'correlationId': ?0, 'status': {$in: ?1}}")
    List<Process> findByCorrelationIdAndStatusIn(String correlationId, List<Process.ProcessStatus> statuses);
    
    @Query("{'createdAt': {$gte: ?0}, 'status': ?1}")
    List<Process> findRecentByStatus(LocalDateTime since, Process.ProcessStatus status);
    
    @Query("{'requestType': ?0, 'status': ?1, 'createdAt': {$gte: ?2}}")
    List<Process> findByRequestTypeAndStatusSince(
            String requestType, 
            Process.ProcessStatus status, 
            LocalDateTime since
    );
    
    // Count queries
    long countByStatus(Process.ProcessStatus status);
    long countByRequestType(String requestType);
    long countByTenantId(String tenantId);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Exists queries
    boolean existsByCorrelationId(String correlationId);
    boolean existsByProcessId(String processId);
}
