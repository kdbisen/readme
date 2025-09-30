package com.banking.onboarding.controller;

import com.banking.onboarding.dto.ProcessEntityResponse;
import com.banking.onboarding.dto.ProcessStatusResponse;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.RequestType;
import com.banking.onboarding.service.CorrelationIdService;
import com.banking.onboarding.service.OnboardingProcessService;
import com.banking.onboarding.service.FunctionalOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingProcessService processService;
    private final FunctionalOnboardingService functionalService;
    private final CorrelationIdService correlationIdService;
    
    @PostMapping("/process-entity/{requestType}")
    public ResponseEntity<ProcessEntityResponse> processEntity(
            @PathVariable RequestType requestType,
            @RequestBody String payload) {
        
        log.info("Received process entity request with type: {}", requestType);
        
        try {
            // Get correlation ID from request context (automatically handled by interceptor)
            String correlationId = correlationIdService.getCurrentCorrelationId();

            // Generate process ID
            String processId = UUID.randomUUID().toString();
            
            // Start async processing using functional service
            functionalService.processEntityAsync(payload, requestType, processId);
            
            // Prepare response
            ProcessEntityResponse response = ProcessEntityResponse.builder()
                    .processId(processId)
                    .correlationId(correlationId)
                    .status("PROCESSING")
                    .message("Entity processing started successfully")
                    .estimatedCompletionTime("5-10 minutes")
                    .build();
            
            log.info("Successfully initiated processing for process: {}", processId);
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error processing entity request: {}", e.getMessage(), e);
            
            ProcessEntityResponse errorResponse = ProcessEntityResponse.builder()
                    .processId(null)
                    .correlationId(null)
                    .status("FAILED")
                    .message("Failed to initiate processing: " + e.getMessage())
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
    
    @GetMapping("/status/correlation/{correlationId}")
    public ResponseEntity<ProcessStatusResponse> getProcessStatusByCorrelationId(@PathVariable String correlationId) {
        log.info("Retrieving status for correlation ID: {}", correlationId);
        
        try {
            OnboardingProcess process = processService.getProcessByCorrelationId(correlationId)
                    .orElseThrow(() -> new RuntimeException("Process not found for correlation ID: " + correlationId));
            
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
            
            log.info("Successfully retrieved status for correlation ID: {}", correlationId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Process not found for correlation ID: {}", correlationId);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error retrieving status for correlation ID: {}", correlationId, e);
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
            case RECEIVED -> "Entity received and queued for processing";
            case TRANSFORMING -> "Transforming XML data to structured format";
            case VALIDATING -> "Validating entity data and compliance requirements";
            case PROCESSING_FENERGO -> "Submitting entity to Fenergo for onboarding";
            case COMPLETED -> "Entity onboarding completed successfully";
            case FAILED -> "Entity onboarding failed";
        };
    }
}
