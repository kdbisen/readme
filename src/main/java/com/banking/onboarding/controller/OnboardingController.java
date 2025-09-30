package com.banking.onboarding.controller;

import com.banking.onboarding.model.RequestType;
import com.banking.onboarding.service.CorrelationIdService;
import com.banking.onboarding.service.FenergoJourneyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Enhanced Onboarding Controller with Fenergo Journey Integration
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final FenergoJourneyService fenergoJourneyService;
    private final CorrelationIdService correlationIdService;

    /**
     * Process entity with multi-step Fenergo journey
     * 
     * Flow:
     * 1. Generate process ID and correlation ID
     * 2. Start async processing:
     *    - Call Apigee API (XML -> JSON)
     *    - Call Fenergo Entity Create API via proxy
     *    - Call Fenergo Journey Info API
     *    - Call Fenergo Journey Initiate API
     *    - Call Fenergo Journey Details API
     *    - Save each step to database
     */
    @PostMapping("/process-entity/{requestType}")
    public ResponseEntity<ProcessEntityResponse> processEntity(
            @PathVariable RequestType requestType,
            @RequestBody String payload) {
        
        try {
            // Generate unique process ID
            String processId = "PROC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Get correlation ID from interceptor
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            log.info("[CORRELATION:{}] Starting Fenergo journey processing - ProcessId: {}, RequestType: {}", 
                    correlationId, processId, requestType);
            
            // Start async processing
            fenergoJourneyService.processFenergoJourneyAsync(payload, requestType, processId, correlationId);
            
            // Return immediate response
            ProcessEntityResponse response = ProcessEntityResponse.builder()
                    .processId(processId)
                    .correlationId(correlationId)
                    .status("PROCESSING")
                    .message("Fenergo journey processing initiated successfully")
                    .requestType(requestType)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            log.info("[CORRELATION:{}] Fenergo journey processing initiated - ProcessId: {}", 
                    correlationId, processId);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Failed to initiate Fenergo journey processing", 
                    correlationIdService.getCurrentCorrelationId(), e);
            
            ProcessEntityResponse errorResponse = ProcessEntityResponse.builder()
                    .processId("ERROR-" + UUID.randomUUID().toString().substring(0, 8))
                    .correlationId(correlationIdService.getCurrentCorrelationId())
                    .status("FAILED")
                    .message("Failed to initiate Fenergo journey processing: " + e.getMessage())
                    .requestType(requestType)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get process status by process ID
     */
    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessStatusResponse> getProcessStatus(@PathVariable String processId) {
        try {
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            log.info("[CORRELATION:{}] Getting process status - ProcessId: {}", correlationId, processId);
            
            ProcessStatusResponse status = fenergoJourneyService.getProcessStatus(processId);
            
            if (status == null) {
                log.warn("[CORRELATION:{}] Process not found - ProcessId: {}", correlationId, processId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            log.info("[CORRELATION:{}] Process status retrieved - ProcessId: {}, Status: {}", 
                    correlationId, processId, status.getStatus());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Failed to get process status - ProcessId: {}", 
                    correlationIdService.getCurrentCorrelationId(), processId, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get journey details by process ID
     */
    @GetMapping("/journey/{processId}")
    public ResponseEntity<JourneyDetailsResponse> getJourneyDetails(@PathVariable String processId) {
        try {
            String correlationId = correlationIdService.getCurrentCorrelationId();
            
            log.info("[CORRELATION:{}] Getting journey details - ProcessId: {}", correlationId, processId);
            
            JourneyDetailsResponse journeyDetails = fenergoJourneyService.getJourneyDetails(processId);
            
            if (journeyDetails == null) {
                log.warn("[CORRELATION:{}] Journey details not found - ProcessId: {}", correlationId, processId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            log.info("[CORRELATION:{}] Journey details retrieved - ProcessId: {}", correlationId, processId);
            
            return ResponseEntity.ok(journeyDetails);
            
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Failed to get journey details - ProcessId: {}", 
                    correlationIdService.getCurrentCorrelationId(), processId, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}