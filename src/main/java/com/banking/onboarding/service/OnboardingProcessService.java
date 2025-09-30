package com.banking.onboarding.service;

import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.RequestType;
import com.banking.onboarding.repository.OnboardingProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingProcessService {
    
    private final OnboardingProcessRepository repository;
    
    public OnboardingProcess createProcess(String payload, String correlationId, RequestType requestType) {
        log.info("Creating new onboarding process with correlation ID: {} and request type: {}", correlationId, requestType);
        
        String processId = UUID.randomUUID().toString();
        
        OnboardingProcess process = OnboardingProcess.builder()
                .processId(processId)
                .correlationId(correlationId)
                .requestType(requestType)
                .status(OnboardingProcess.ProcessStatus.RECEIVED)
                .originalXml(payload)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        OnboardingProcess savedProcess = repository.save(process);
        log.info("Created onboarding process: {}", processId);
        
        return savedProcess;
    }
    
    public OnboardingProcess updateProcess(OnboardingProcess process) {
        log.info("Updating onboarding process: {}", process.getProcessId());
        
        process.setUpdatedAt(LocalDateTime.now());
        
        if (process.getStatus() == OnboardingProcess.ProcessStatus.COMPLETED ||
            process.getStatus() == OnboardingProcess.ProcessStatus.FAILED) {
            process.setCompletedAt(LocalDateTime.now());
        }
        
        return repository.save(process);
    }
    
    public Optional<OnboardingProcess> getProcessById(String processId) {
        return repository.findByProcessId(processId);
    }
    
    public Optional<OnboardingProcess> getProcessByCorrelationId(String correlationId) {
        return repository.findByCorrelationId(correlationId);
    }
    
    public List<OnboardingProcess> getProcessesByStatus(OnboardingProcess.ProcessStatus status) {
        return repository.findByStatus(status);
    }
    
    public List<OnboardingProcess> getActiveProcesses() {
        return repository.findActiveProcesses();
    }
    
    public List<OnboardingProcess> getFailedProcessesSince(LocalDateTime since) {
        return repository.findFailedProcessesSince(since);
    }
    
    public long getProcessCountByStatus(OnboardingProcess.ProcessStatus status) {
        return repository.countByStatus(status);
    }
    
    public List<OnboardingProcess> getProcessesByEntityId(String entityId) {
        return repository.findByEntityId(entityId);
    }
    
    public List<OnboardingProcess> getProcessesByEntityName(String entityName) {
        return repository.findByEntityNameContaining(entityName);
    }
}
