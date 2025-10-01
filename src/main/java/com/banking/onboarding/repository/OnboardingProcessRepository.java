package com.banking.onboarding.repository;

import com.banking.onboarding.model.OnboardingProcess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for OnboardingProcess
 */
@Repository
public interface OnboardingProcessRepository extends MongoRepository<OnboardingProcess, String> {
    
    Optional<OnboardingProcess> findByProcessId(String processId);
    
    Optional<OnboardingProcess> findByCorrelationId(String correlationId);
    
    void deleteByProcessId(String processId);
}
