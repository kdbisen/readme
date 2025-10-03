package com.banking.onboarding.controller;

import com.banking.onboarding.constants.OnboardingConstants;
import com.banking.onboarding.exception.ValidationException;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.service.CorrelationIdStrategyService;
import com.banking.onboarding.service.GenericOnboardingFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Single Onboarding Controller - Main Process Endpoint Only
 * Handles the core onboarding process
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final CorrelationIdStrategyService correlationIdStrategyService;
    private final GenericOnboardingFlowService genericOnboardingFlowService;

    /**
     * Process entity onboarding - Main entry point with complete 4-step flow - SYNCHRONOUS
     * Handles XML input, transforms to JSON, and executes all Fenergo steps with database persistence
     */
    @PostMapping("/process-entity")
    public ResponseEntity<OnboardingProcess> processEntity(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        // Simple correlation ID processing
        CorrelationIdStrategyService.SimpleCorrelationResult correlationResult = 
            correlationIdStrategyService.processCorrelationId(correlationId, request);
        
        String actualCorrelationId = correlationResult.getCorrelationId();
        String xmlData = request.get("xmlData");
        String requestType = request.getOrDefault("requestType", OnboardingConstants.RequestTypes.ADD_KYC);
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            throw new ValidationException(
                    OnboardingConstants.Messages.XML_DATA_REQUIRED, actualCorrelationId);
        }
        
        // Log simple correlation ID info
        log.info("[CORRELATION:{}] Processing request #{} - New Correlation ID: {}", 
                actualCorrelationId, correlationResult.getRequestNumber(), correlationResult.isNewCorrelationId());
        
        log.info("[CORRELATION:{}] Starting complete 4-step entity onboarding process", actualCorrelationId);
        
        OnboardingProcess process = genericOnboardingFlowService.executeCompleteFlow(xmlData, requestType, actualCorrelationId);
        log.info("[CORRELATION:{}] Complete onboarding process finished with status: {}", 
                actualCorrelationId, process.getStatus());
        
        return ResponseEntity.ok(process);
    }
}