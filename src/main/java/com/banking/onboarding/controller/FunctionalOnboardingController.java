package com.banking.onboarding.controller;

import com.banking.onboarding.dto.ProcessEntityResponse;
import com.banking.onboarding.dto.ProcessStatusResponse;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.RequestType;
import com.banking.onboarding.service.CorrelationIdService;
import com.banking.onboarding.service.FunctionalOnboardingService;
import com.banking.onboarding.service.OnboardingProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller using functional processing approach
 */
@Slf4j
@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class FunctionalOnboardingController {
    
    private final FunctionalOnboardingService functionalOnboardingService;
    private final OnboardingProcessService processService;
    private final CorrelationIdService correlationIdService;
    
    @PostMapping("/process-entity/{requestType}")
    public ResponseEntity<ProcessEntityResponse> processEntity(
            @PathVariable RequestType requestType,
            @RequestBody String payload) {
        
        log.info("Received functional process entity request with type: {}", requestType);
        
        try {
            // Get correlation ID from request context (automatically handled by interceptor)
            String correlationId = correlationIdService.getCurrentCorrelationId();
            String processId = UUID.randomUUID().toString();
            
            // Create initial process record
            OnboardingProcess process = processService.createProcess(payload, correlationId, requestType);
            
            // Start functional async processing
            functionalOnboardingService.processEntityAsync(payload, requestType, processId);
            
            // Return immediate response
            ProcessEntityResponse response = ProcessEntityResponse.builder()
                    .processId(processId)
                    .correlationId(correlationId)
                    .status("PROCESSING")
                    .message("Functional entity processing started successfully")
                    .estimatedCompletionTime("5-10 minutes")
                    .build();
            
            log.info("Successfully initiated functional processing for process: {}", processId);
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error in functional processing request: {}", e.getMessage(), e);
            
            ProcessEntityResponse errorResponse = ProcessEntityResponse.builder()
                    .processId(null)
                    .correlationId(null)
                    .status("FAILED")
                    .message("Failed to initiate functional processing: " + e.getMessage())
                    .estimatedCompletionTime(null)
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/process-entity-chain/{requestType}")
    public ResponseEntity<ProcessEntityResponse> processEntityWithChain(
            @PathVariable RequestType requestType,
            @RequestBody String payload) {
        
        log.info("Received chain-based process entity request with type: {}", requestType);
        
        try {
            // Get correlation ID from request context (automatically handled by interceptor)
            String correlationId = correlationIdService.getCurrentCorrelationId();
            String processId = UUID.randomUUID().toString();
            
            // Create initial process record
            OnboardingProcess process = processService.createProcess(payload, correlationId, requestType);
            
            // Start chain-based async processing
            functionalOnboardingService.processEntityWithChain(payload, requestType, processId);
            
            // Return immediate response
            ProcessEntityResponse response = ProcessEntityResponse.builder()
                    .processId(processId)
                    .correlationId(correlationId)
                    .status("PROCESSING")
                    .message("Chain-based entity processing started successfully")
                    .estimatedCompletionTime("5-10 minutes")
                    .build();
            
            log.info("Successfully initiated chain-based processing for process: {}", processId);
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error in chain-based processing request: {}", e.getMessage(), e);
            
            ProcessEntityResponse errorResponse = ProcessEntityResponse.builder()
                    .processId(null)
                    .correlationId(null)
                    .status("FAILED")
                    .message("Failed to initiate chain-based processing: " + e.getMessage())
                    .estimatedCompletionTime(null)
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessStatusResponse> getProcessStatus(@PathVariable String processId) {
        log.info("Retrieving status for process: {}", processId);
        
        try {
            OnboardingProcess process = processService.getProcessById(processId)
                    .orElseThrow(() -> new RuntimeException("Process not found: " + processId));
            
            // Calculate progress percentage based on status
            int progressPercentage = calculateProgressPercentage(process.getStatus());
            
            ProcessStatusResponse response = ProcessStatusResponse.builder()
                    .processId(process.getProcessId())
                    .correlationId(process.getCorrelationId())
                    .status(process.getStatus().toString())
                    .message(getStatusMessage(process.getStatus()))
                    .entityData(process.getTransformedJson())
                    .fenergoResponse(process.getFenergoResponse())
                    .errorMessage(process.getErrorMessage())
                    .createdAt(process.getCreatedAt())
                    .updatedAt(process.getUpdatedAt())
                    .completedAt(process.getCompletedAt())
                    .progressPercentage(progressPercentage)
                    .build();
            
            log.info("Successfully retrieved status for process: {}", processId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Process not found: {}", processId);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error retrieving status for process: {}", processId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        try {
            long activeProcesses = processService.getProcessCountByStatus(OnboardingProcess.ProcessStatus.RECEIVED) +
                    processService.getProcessCountByStatus(OnboardingProcess.ProcessStatus.TRANSFORMING) +
                    processService.getProcessCountByStatus(OnboardingProcess.ProcessStatus.VALIDATING) +
                    processService.getProcessCountByStatus(OnboardingProcess.ProcessStatus.PROCESSING_FENERGO);
            
            long completedProcesses = processService.getProcessCountByStatus(OnboardingProcess.ProcessStatus.COMPLETED);
            long failedProcesses = processService.getProcessCountByStatus(OnboardingProcess.ProcessStatus.FAILED);
            
            return ResponseEntity.ok(java.util.Map.of(
                    "status", "UP",
                    "timestamp", LocalDateTime.now(),
                    "processingType", "FUNCTIONAL",
                    "activeProcesses", activeProcesses,
                    "completedProcesses", completedProcesses,
                    "failedProcesses", failedProcesses
            ));
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(java.util.Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
    
    private int calculateProgressPercentage(OnboardingProcess.ProcessStatus status) {
        return switch (status) {
            case RECEIVED -> 10;
            case TRANSFORMING -> 30;
            case VALIDATING -> 50;
            case PROCESSING_FENERGO -> 80;
            case COMPLETED -> 100;
            case FAILED -> 0;
        };
    }
    
    private String getStatusMessage(OnboardingProcess.ProcessStatus status) {
        return switch (status) {
            case RECEIVED -> "Entity received and queued for functional processing";
            case TRANSFORMING -> "Transforming XML data using functional pipeline";
            case VALIDATING -> "Validating entity data using functional validation";
            case PROCESSING_FENERGO -> "Submitting entity to Fenergo using functional approach";
            case COMPLETED -> "Entity onboarding completed successfully via functional pipeline";
            case FAILED -> "Entity onboarding failed in functional pipeline";
        };
    }
}
