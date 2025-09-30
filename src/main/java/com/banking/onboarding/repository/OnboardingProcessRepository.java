package com.banking.onboarding.repository;

import com.banking.onboarding.model.OnboardingProcess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingProcessRepository extends MongoRepository<OnboardingProcess, String> {
    
    Optional<OnboardingProcess> findByProcessId(String processId);
    
    Optional<OnboardingProcess> findByCorrelationId(String correlationId);
    
    List<OnboardingProcess> findByStatus(OnboardingProcess.ProcessStatus status);
    
    List<OnboardingProcess> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{ 'status': { $in: ['RECEIVED', 'TRANSFORMING', 'VALIDATING', 'PROCESSING_FENERGO'] } }")
    List<OnboardingProcess> findActiveProcesses();
    
    @Query("{ 'status': 'FAILED', 'createdAt': { $gte: ?0 } }")
    List<OnboardingProcess> findFailedProcessesSince(LocalDateTime since);
    
    long countByStatus(OnboardingProcess.ProcessStatus status);
    
    @Query("{ 'entityData.entityId': ?0 }")
    List<OnboardingProcess> findByEntityId(String entityId);
    
    @Query("{ 'entityData.entityName': { $regex: ?0, $options: 'i' } }")
    List<OnboardingProcess> findByEntityNameContaining(String entityName);
}
