package com.banking.onboarding.controller;

import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.exception.ProcessException;
import com.banking.onboarding.exception.ValidationException;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.service.CorrelationIdService;
import com.banking.onboarding.service.GenericOnboardingFlowService;
import com.banking.onboarding.service.MonitoringService;
import com.banking.onboarding.service.TransformationService;
import com.banking.onboarding.service.FenergoService;
import com.banking.onboarding.service.WorkflowOrchestrationService;
import com.banking.onboarding.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Single Onboarding Controller
 * Handles all onboarding-related operations with clean architecture
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final CorrelationIdService correlationIdService;
    private final GenericOnboardingFlowService genericOnboardingFlowService;
    private final MonitoringService monitoringService;
    private final TransformationService transformationService;
    private final FenergoService fenergoService;
    private final WorkflowOrchestrationService workflowOrchestrationService;
    private final ValidationService validationService;

    // ===========================================
    // MAIN ONBOARDING ENDPOINTS
    // ===========================================

    /**
     * Process entity onboarding - Main entry point with complete 5-step flow - SYNCHRONOUS
     * Handles XML input, transforms to JSON, and executes all Fenergo steps with database persistence
     */
    @PostMapping("/process-entity")
    public ResponseEntity<OnboardingProcess> processEntity(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String xmlData = request.get("xmlData");
        String requestType = request.getOrDefault("requestType", "ADD_KYC");
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            throw new com.banking.onboarding.exception.ValidationException(
                    "XML data is required and cannot be empty", actualCorrelationId);
        }
        
        log.info("[CORRELATION:{}] Starting complete 5-step entity onboarding process", actualCorrelationId);
        
        OnboardingProcess process = genericOnboardingFlowService.executeCompleteFlow(xmlData, requestType, actualCorrelationId);
        log.info("[CORRELATION:{}] Complete onboarding process finished with status: {}", 
                actualCorrelationId, process.getStatus());
        return ResponseEntity.ok(process);
    }

    /**
     * Get onboarding process status from database
     */
    @GetMapping("/status/{processId}")
    public ResponseEntity<OnboardingProcess> getProcessStatus(@PathVariable String processId) {
        log.info("Getting process status for processId: {}", processId);
        
        OnboardingProcess process = genericOnboardingFlowService.getProcessById(processId);
        return ResponseEntity.ok(process);
    }

    // ===========================================
    // SUPPORTING ENDPOINTS FOR ONBOARDING
    // ===========================================

    /**
     * Transform XML to JSON (used internally by onboarding process)
     */
    @PostMapping("/transform/xml-to-json")
    public CompletableFuture<ResponseEntity<ApiResponse>> transformXmlToJson(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String xmlData = request.get("xmlData");
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(ApiResponse.error(
                            400, "xmlData is required", "xml-to-json", actualCorrelationId
                    ))
            );
        }
        
        log.info("[CORRELATION:{}] Transforming XML to JSON", actualCorrelationId);
        
        return transformationService.transformXmlToJson(xmlData, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] XML to JSON transformation completed with status: {}", 
                            actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Create Fenergo entity (used internally by onboarding process)
     */
    @PostMapping("/fenergo/entity/create")
    public CompletableFuture<ResponseEntity<ApiResponse>> createFenergoEntity(
            @RequestBody Map<String, Object> entityData,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        log.info("[CORRELATION:{}] Creating Fenergo entity", actualCorrelationId);
        
        return fenergoService.createEntity(entityData, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Fenergo entity creation completed with status: {}", 
                            actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Complete multi-step Fenergo workflow
     */
    @PostMapping("/fenergo/workflow")
    public CompletableFuture<ResponseEntity<ApiResponse>> processFenergoWorkflow(
            @RequestBody Map<String, Object> initialData,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        log.info("[CORRELATION:{}] Starting Fenergo workflow", actualCorrelationId);
        
        return workflowOrchestrationService.processFenergoWorkflow(initialData, actualCorrelationId)
                .thenApply(response -> {
                    log.info("[CORRELATION:{}] Fenergo workflow completed with status: {}", 
                            actualCorrelationId, response.getStatusCode());
                    return ResponseEntity.ok(response);
                });
    }

    // ===========================================
    // UTILITY ENDPOINTS
    // ===========================================

    /**
     * Health check for onboarding service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "service", "Onboarding Service",
                "status", "healthy",
                "architecture", "Clean Architecture",
                "features", Map.of(
                        "xmlToJsonTransformation", "Apigee transformation service",
                        "fenergoEntityCreation", "Fenergo proxy integration",
                        "workflowOrchestration", "Multi-step process management",
                        "correlationTracking", "Full request tracing"
                ),
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Get service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "serviceName", "Banking Onboarding Service",
                "version", "2.0.0",
                "description", "Enhanced onboarding service with validation, retry, and monitoring",
                "mainEndpoint", "/api/v1/onboarding/process-entity",
                "architecture", "Clean Architecture with Strategy Pattern",
                "patterns", new String[]{
                    "Strategy Pattern",
                    "Factory Pattern", 
                    "Builder Pattern",
                    "Facade Pattern",
                    "Retry Pattern",
                    "Monitoring Pattern"
                },
                "supportedOperations", new String[]{
                    "XML to JSON transformation",
                    "Fenergo entity creation",
                    "Multi-step workflow orchestration",
                    "Process status tracking",
                    "Input validation",
                    "Retry mechanism",
                    "Performance monitoring"
                }
        ));
    }
    
    // ===========================================
    // MONITORING AND METRICS ENDPOINTS
    // ===========================================
    
    /**
     * Get process metrics
     */
    @GetMapping("/metrics/process")
    public ResponseEntity<MonitoringService.ProcessMetrics> getProcessMetrics() {
        return ResponseEntity.ok(monitoringService.getProcessMetrics());
    }
    
    /**
     * Get step metrics
     */
    @GetMapping("/metrics/steps")
    public ResponseEntity<Map<String, MonitoringService.StepMetrics>> getStepMetrics() {
        return ResponseEntity.ok(monitoringService.getAllStepMetrics());
    }
    
    /**
     * Get metrics for specific step
     */
    @GetMapping("/metrics/steps/{stepName}")
    public ResponseEntity<MonitoringService.StepMetrics> getStepMetrics(@PathVariable String stepName) {
        return ResponseEntity.ok(monitoringService.getStepMetrics(stepName));
    }
    
    /**
     * Reset all metrics
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        monitoringService.resetMetrics();
        return ResponseEntity.ok(Map.of("message", "All metrics reset successfully"));
    }
    
    /**
     * Validate XML data
     */
    @PostMapping("/validate/xml")
    public ResponseEntity<ValidationService.ValidationResult> validateXml(@RequestBody Map<String, String> request) {
        String xmlData = request.get("xmlData");
        ValidationService.ValidationResult result = validationService.validateXmlData(xmlData);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Test individual step execution - SYNCHRONOUS
     */
    @PostMapping("/test/step/{stepName}")
    public ResponseEntity<com.banking.onboarding.step.StepResult<Object>> testStep(
            @PathVariable String stepName,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String inputData = request.get("inputData");
        
        if (inputData == null || inputData.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("[CORRELATION:{}] Testing step: {}", actualCorrelationId, stepName);
        
        try {
            com.banking.onboarding.step.StepResult<Object> result = 
                    genericOnboardingFlowService.executeStep(stepName, inputData, actualCorrelationId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[CORRELATION:{}] Step test failed: {}", actualCorrelationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}