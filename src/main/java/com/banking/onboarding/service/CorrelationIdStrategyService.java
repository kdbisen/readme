package com.banking.onboarding.service;

import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.repository.OnboardingProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Correlation ID Strategy Service - Handles duplicate correlation ID scenarios
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorrelationIdStrategyService {
    
    private final OnboardingProcessRepository processRepository;
    
    /**
     * Handle correlation ID strategy when processing new request
     * 
     * @param correlationId The correlation ID from request
     * @param requestType The request type
     * @return CorrelationIdStrategyResult with strategy and existing processes
     */
    public CorrelationIdStrategyResult handleCorrelationIdStrategy(String correlationId, String requestType) {
        log.info("[CORRELATION:{}] Analyzing correlation ID strategy for requestType: {}", correlationId, requestType);
        
        // Check if correlation ID already exists
        List<OnboardingProcess> existingProcesses = processRepository.findByCorrelationIdOrderByCreatedAtDesc(correlationId);
        
        if (existingProcesses.isEmpty()) {
            log.info("[CORRELATION:{}] New correlation ID - proceeding with new process", correlationId);
            return CorrelationIdStrategyResult.builder()
                    .strategy(CorrelationIdStrategy.NEW_PROCESS)
                    .existingProcesses(existingProcesses)
                    .activeProcessCount(0)
                    .completedProcessCount(0)
                    .shouldProceed(true)
                    .message("New correlation ID - creating new process")
                    .build();
        }
        
        // Analyze existing processes
        long activeCount = existingProcesses.stream()
                .filter(p -> !isTerminalStatus(p.getStatus()))
                .count();
        
        long completedCount = existingProcesses.stream()
                .filter(p -> p.getStatus() == OnboardingProcess.ProcessStatus.COMPLETED)
                .count();
        
        long failedCount = existingProcesses.stream()
                .filter(p -> p.getStatus() == OnboardingProcess.ProcessStatus.FAILED)
                .count();
        
        log.info("[CORRELATION:{}] Found {} existing processes: {} active, {} completed, {} failed", 
                correlationId, existingProcesses.size(), activeCount, completedCount, failedCount);
        
        // Determine strategy based on existing processes
        CorrelationIdStrategy strategy;
        boolean shouldProceed;
        String message;
        
        if (activeCount > 0) {
            // Has active processes - check if we should allow new process
            strategy = determineActiveProcessStrategy(requestType, activeCount);
            shouldProceed = strategy != CorrelationIdStrategy.REJECT_REQUEST;
            message = strategy == CorrelationIdStrategy.REJECT_REQUEST ? 
                    "Active process exists - rejecting duplicate request" :
                    "Active process exists - allowing new process";
        } else if (completedCount > 0) {
            // Has completed processes - check if we should allow retry
            strategy = determineCompletedProcessStrategy(requestType, completedCount);
            shouldProceed = strategy != CorrelationIdStrategy.REJECT_REQUEST;
            message = strategy == CorrelationIdStrategy.REJECT_REQUEST ? 
                    "Completed process exists - rejecting duplicate request" :
                    "Completed process exists - allowing retry";
        } else {
            // Only failed processes - allow retry
            strategy = CorrelationIdStrategy.RETRY_PROCESS;
            shouldProceed = true;
            message = "Only failed processes exist - allowing retry";
        }
        
        log.info("[CORRELATION:{}] Strategy determined: {} - Proceed: {}", correlationId, strategy, shouldProceed);
        
        return CorrelationIdStrategyResult.builder()
                .strategy(strategy)
                .existingProcesses(existingProcesses)
                .activeProcessCount(activeCount)
                .completedProcessCount(completedCount)
                .shouldProceed(shouldProceed)
                .message(message)
                .build();
    }
    
    /**
     * Determine strategy when active processes exist
     */
    private CorrelationIdStrategy determineActiveProcessStrategy(String requestType, long activeCount) {
        // For critical operations, reject duplicate requests
        if ("ADD_KYC".equals(requestType) || "UPDATE_KYC".equals(requestType)) {
            return CorrelationIdStrategy.REJECT_REQUEST;
        }
        
        // For other operations, allow limited concurrent processes
        if (activeCount >= 3) {
            return CorrelationIdStrategy.REJECT_REQUEST;
        }
        
        return CorrelationIdStrategy.ALLOW_CONCURRENT;
    }
    
    /**
     * Determine strategy when completed processes exist
     */
    private CorrelationIdStrategy determineCompletedProcessStrategy(String requestType, long completedCount) {
        // For critical operations, reject duplicate requests
        if ("ADD_KYC".equals(requestType)) {
            return CorrelationIdStrategy.REJECT_REQUEST;
        }
        
        // For other operations, allow retry
        return CorrelationIdStrategy.RETRY_PROCESS;
    }
    
    /**
     * Check if status is terminal (no longer active)
     */
    private boolean isTerminalStatus(OnboardingProcess.ProcessStatus status) {
        return status == OnboardingProcess.ProcessStatus.COMPLETED ||
               status == OnboardingProcess.ProcessStatus.FAILED ||
               status == OnboardingProcess.ProcessStatus.CANCELLED;
    }
    
    /**
     * Get the latest process by correlation ID
     */
    public Optional<OnboardingProcess> getLatestProcessByCorrelationId(String correlationId) {
        List<OnboardingProcess> processes = processRepository.findByCorrelationIdOrderByCreatedAtDesc(correlationId);
        return processes.isEmpty() ? Optional.empty() : Optional.of(processes.get(0));
    }
    
    /**
     * Get all processes by correlation ID
     */
    public List<OnboardingProcess> getAllProcessesByCorrelationId(String correlationId) {
        return processRepository.findByCorrelationIdOrderByCreatedAtDesc(correlationId);
    }
    
    /**
     * Get active processes by correlation ID
     */
    public List<OnboardingProcess> getActiveProcessesByCorrelationId(String correlationId) {
        return processRepository.findActiveByCorrelationId(correlationId);
    }
    
    /**
     * Check if correlation ID has active processes
     */
    public boolean hasActiveProcesses(String correlationId) {
        return processRepository.existsActiveByCorrelationId(correlationId);
    }
    
    /**
     * Count processes by correlation ID
     */
    public long countProcessesByCorrelationId(String correlationId) {
        return processRepository.countByCorrelationId(correlationId);
    }
    
    /**
     * Correlation ID Strategy Enum
     */
    public enum CorrelationIdStrategy {
        NEW_PROCESS,        // Create new process (no existing processes)
        RETRY_PROCESS,      // Allow retry (only failed processes exist)
        ALLOW_CONCURRENT,   // Allow concurrent process (limited active processes)
        REJECT_REQUEST      // Reject request (too many active processes or critical operation)
    }
    
    /**
     * Correlation ID Strategy Result
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CorrelationIdStrategyResult {
        private CorrelationIdStrategy strategy;
        private List<OnboardingProcess> existingProcesses;
        private long activeProcessCount;
        private long completedProcessCount;
        private boolean shouldProceed;
        private String message;
        
        /**
         * Get the latest existing process
         */
        public Optional<OnboardingProcess> getLatestProcess() {
            return existingProcesses.isEmpty() ? Optional.empty() : Optional.of(existingProcesses.get(0));
        }
        
        /**
         * Get the latest completed process
         */
        public Optional<OnboardingProcess> getLatestCompletedProcess() {
            return existingProcesses.stream()
                    .filter(p -> p.getStatus() == OnboardingProcess.ProcessStatus.COMPLETED)
                    .findFirst();
        }
        
        /**
         * Get the latest failed process
         */
        public Optional<OnboardingProcess> getLatestFailedProcess() {
            return existingProcesses.stream()
                    .filter(p -> p.getStatus() == OnboardingProcess.ProcessStatus.FAILED)
                    .findFirst();
        }
    }
}




