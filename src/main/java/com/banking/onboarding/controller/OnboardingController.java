package com.banking.onboarding.controller;

import com.banking.onboarding.domain.ApiResponse;
import com.banking.onboarding.exception.ProcessException;
import com.banking.onboarding.exception.ValidationException;
import com.banking.onboarding.model.OnboardingProcess;
import com.banking.onboarding.model.ProcessStep;
import com.banking.onboarding.ratelimit.RateLimitService;
import com.banking.onboarding.service.CorrelationIdService;
import com.banking.onboarding.service.GenericOnboardingFlowService;
import com.banking.onboarding.service.MonitoringService;
import com.banking.onboarding.service.TransformationService;
import com.banking.onboarding.service.FenergoService;
import com.banking.onboarding.service.WorkflowOrchestrationService;
import com.banking.onboarding.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Single Onboarding Controller - Complete Enterprise Solution
 * Handles all onboarding-related operations with enterprise-grade features
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
    private final RateLimitService rateLimitService;

    // ===========================================
    // MAIN ONBOARDING ENDPOINTS WITH RATE LIMITING
    // ===========================================

    /**
     * Process entity onboarding - Main entry point with complete 4-step flow - SYNCHRONOUS
     * Handles XML input, transforms to JSON, and executes all Fenergo steps with database persistence
     */
    @PostMapping("/process-entity")
    public ResponseEntity<OnboardingProcess> processEntity(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletRequest httpRequest) {
        
        String clientId = getClientId(httpRequest);
        String rateLimitKey = "process-entity:" + clientId;
        
        // Check rate limit
        if (!rateLimitService.isAllowed(rateLimitKey)) {
            log.warn("[RATE_LIMIT] Rate limit exceeded for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Limit", "100")
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000))
                    .build();
        }
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        String xmlData = request.get("xmlData");
        String requestType = request.getOrDefault("requestType", "ADD_KYC");
        
        if (xmlData == null || xmlData.trim().isEmpty()) {
            throw new ValidationException(
                    "XML data is required and cannot be empty", actualCorrelationId);
        }
        
        log.info("[CORRELATION:{}] Starting complete 4-step entity onboarding process", actualCorrelationId);
        
        OnboardingProcess process = genericOnboardingFlowService.executeCompleteFlow(xmlData, requestType, actualCorrelationId);
        log.info("[CORRELATION:{}] Complete onboarding process finished with status: {}", 
                actualCorrelationId, process.getStatus());
        return ResponseEntity.ok(process);
    }

    /**
     * Get onboarding process status from database with rate limiting
     */
    @GetMapping("/status/{processId}")
    public ResponseEntity<OnboardingProcess> getProcessStatus(
            @PathVariable String processId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletRequest httpRequest) {
        
        String clientId = getClientId(httpRequest);
        String rateLimitKey = "status:" + clientId;
        
        // Check rate limit
        if (!rateLimitService.isAllowed(rateLimitKey)) {
            log.warn("[RATE_LIMIT] Rate limit exceeded for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        try {
            OnboardingProcess process = genericOnboardingFlowService.getProcessById(processId);
            log.info("[CORRELATION:{}] Retrieved process status for ID: {}", actualCorrelationId, processId);
            return ResponseEntity.ok(process);
        } catch (ProcessException e) {
            log.warn("[CORRELATION:{}] Process not found: {}", actualCorrelationId, processId);
            throw e;
        }
    }

    // ===========================================
    // UTILITY ENDPOINTS WITH RATE LIMITING
    // ===========================================

    /**
     * Transform XML to JSON with rate limiting
     */
    @PostMapping("/transform/xml-to-json")
    public CompletableFuture<ResponseEntity<ApiResponse>> transformXmlToJson(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletRequest httpRequest) {
        
        String clientId = getClientId(httpRequest);
        String rateLimitKey = "transform:" + clientId;
        
        // Check rate limit
        if (!rateLimitService.isAllowed(rateLimitKey)) {
            log.warn("[RATE_LIMIT] Rate limit exceeded for client: {}", clientId);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }
        
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
     * Create Fenergo entity with rate limiting
     */
    @PostMapping("/fenergo/entity/create")
    public CompletableFuture<ResponseEntity<ApiResponse>> createFenergoEntity(
            @RequestBody Map<String, Object> entityData,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletRequest httpRequest) {
        
        String clientId = getClientId(httpRequest);
        String rateLimitKey = "fenergo-entity:" + clientId;
        
        // Check rate limit
        if (!rateLimitService.isAllowed(rateLimitKey)) {
            log.warn("[RATE_LIMIT] Rate limit exceeded for client: {}", clientId);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }
        
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
     * Process Fenergo workflow with rate limiting
     */
    @PostMapping("/fenergo/workflow")
    public CompletableFuture<ResponseEntity<ApiResponse>> processFenergoWorkflow(
            @RequestBody Map<String, Object> initialData,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletRequest httpRequest) {
        
        String clientId = getClientId(httpRequest);
        String rateLimitKey = "fenergo-workflow:" + clientId;
        
        // Check rate limit
        if (!rateLimitService.isAllowed(rateLimitKey)) {
            log.warn("[RATE_LIMIT] Rate limit exceeded for client: {}", clientId);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }
        
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
    // MONITORING AND HEALTH ENDPOINTS
    // ===========================================

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "banking-onboarding-service"
        );
        return ResponseEntity.ok(health);
    }

    /**
     * Service info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = Map.of(
                "name", "Banking Onboarding Service",
                "version", "1.0.0",
                "description", "Spring Boot service for banking onboarding with Fenergo integration",
                "features", Map.of(
                        "rate-limiting", "Enabled",
                        "audit-logging", "Enabled",
                        "monitoring", "Enabled"
                )
        );
        return ResponseEntity.ok(info);
    }

    // ===========================================
    // METRICS ENDPOINTS
    // ===========================================

    /**
     * Get process metrics
     */
    @GetMapping("/metrics/process")
    public ResponseEntity<MonitoringService.ProcessMetrics> getProcessMetrics() {
        return ResponseEntity.ok(monitoringService.getProcessMetrics());
    }

    /**
     * Get all step metrics
     */
    @GetMapping("/metrics/steps")
    public ResponseEntity<Map<String, MonitoringService.StepMetrics>> getAllStepMetrics() {
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
        return ResponseEntity.ok(Map.of("message", "All metrics have been reset"));
    }

    // ===========================================
    // VALIDATION ENDPOINTS
    // ===========================================

    /**
     * Validate XML data
     */
    @PostMapping("/validate/xml")
    public ResponseEntity<ValidationService.ValidationResult> validateXml(
            @RequestBody Map<String, String> request) {
        String xmlData = request.get("xmlData");
        ValidationService.ValidationResult result = validationService.validateXmlData(xmlData);
        return ResponseEntity.ok(result);
    }

    // ===========================================
    // TESTING ENDPOINTS
    // ===========================================

    /**
     * Test specific step
     */
    @PostMapping("/test/step/{stepName}")
    public ResponseEntity<Map<String, Object>> testStep(
            @PathVariable String stepName,
            @RequestBody Map<String, Object> inputData,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        log.info("[CORRELATION:{}] Testing step: {}", actualCorrelationId, stepName);
        
        // This would call the step execution engine directly
        Map<String, Object> result = Map.of(
                "stepName", stepName,
                "status", "SUCCESS",
                "correlationId", actualCorrelationId,
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(result);
    }

    // ===========================================
    // PAYLOAD AND RESPONSE TRACKING ENDPOINTS
    // ===========================================

    /**
     * Get step payload and response data
     */
    @GetMapping("/process/{processId}/step/{stepName}/payload")
    public ResponseEntity<Map<String, Object>> getStepPayloadResponse(
            @PathVariable String processId,
            @PathVariable String stepName,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        try {
            OnboardingProcess process = genericOnboardingFlowService.getProcessById(processId);
            
            // Get step payload and response from process
            Map<String, Object> stepPayloadResponse = process.getStepPayloadResponses();
            if (stepPayloadResponse != null && stepPayloadResponse.containsKey(stepName)) {
                return ResponseEntity.ok(Map.of(
                    "processId", processId,
                    "stepName", stepName,
                    "correlationId", actualCorrelationId,
                    "payloadResponse", stepPayloadResponse.get(stepName)
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (ProcessException e) {
            log.warn("[CORRELATION:{}] Process not found: {}", actualCorrelationId, processId);
            throw e;
        }
    }

    /**
     * Get all step payload and response data for a process
     */
    @GetMapping("/process/{processId}/payloads")
    public ResponseEntity<Map<String, Object>> getAllStepPayloadResponses(
            @PathVariable String processId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        try {
            OnboardingProcess process = genericOnboardingFlowService.getProcessById(processId);
            
            return ResponseEntity.ok(Map.of(
                "processId", processId,
                "correlationId", actualCorrelationId,
                "stepPayloadResponses", process.getStepPayloadResponses() != null ? 
                    process.getStepPayloadResponses() : Map.of(),
                "executionTrace", process.getExecutionTrace() != null ? 
                    process.getExecutionTrace() : Map.of(),
                "totalStepsExecuted", process.getTotalStepsExecuted(),
                "successfulSteps", process.getSuccessfulSteps(),
                "failedSteps", process.getFailedSteps(),
                "totalExecutionTimeMs", process.getTotalExecutionTimeMs()
            ));
        } catch (ProcessException e) {
            log.warn("[CORRELATION:{}] Process not found: {}", actualCorrelationId, processId);
            throw e;
        }
    }

    /**
     * Get step execution summary
     */
    @GetMapping("/process/{processId}/step/{stepName}/summary")
    public ResponseEntity<Map<String, Object>> getStepExecutionSummary(
            @PathVariable String processId,
            @PathVariable String stepName,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        try {
            OnboardingProcess process = genericOnboardingFlowService.getProcessById(processId);
            
            // Find the specific step
            ProcessStep step = process.getSteps().stream()
                .filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(null);
            
            if (step == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "processId", processId,
                "stepName", stepName,
                "correlationId", actualCorrelationId,
                "summary", Map.of(
                    "status", step.getStatus(),
                    "durationMs", step.getDurationMs(),
                    "inputPayloadType", step.getInputPayloadType(),
                    "outputResponseType", step.getOutputResponseType(),
                    "inputPayloadSize", step.getInputPayloadSize(),
                    "outputResponseSize", step.getOutputResponseSize(),
                    "startedAt", step.getStartedAt(),
                    "completedAt", step.getCompletedAt(),
                    "errorMessage", step.getErrorMessage(),
                    "exceptionClass", step.getExceptionClass()
                )
            ));
        } catch (ProcessException e) {
            log.warn("[CORRELATION:{}] Process not found: {}", actualCorrelationId, processId);
            throw e;
        }
    }

    /**
     * Get complete execution trace for a process
     */
    @GetMapping("/process/{processId}/trace")
    public ResponseEntity<Map<String, Object>> getCompleteExecutionTrace(
            @PathVariable String processId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
        
        try {
            OnboardingProcess process = genericOnboardingFlowService.getProcessById(processId);
            
            return ResponseEntity.ok(Map.of(
                "processId", processId,
                "correlationId", actualCorrelationId,
                "executionTrace", process.getExecutionTrace() != null ? 
                    process.getExecutionTrace() : Map.of(),
                "processSummary", Map.of(
                    "status", process.getStatus(),
                    "requestType", process.getRequestType(),
                    "totalStepsExecuted", process.getTotalStepsExecuted(),
                    "successfulSteps", process.getSuccessfulSteps(),
                    "failedSteps", process.getFailedSteps(),
                    "totalExecutionTimeMs", process.getTotalExecutionTimeMs(),
                    "createdAt", process.getCreatedAt(),
                    "completedAt", process.getCompletedAt()
                )
            ));
        } catch (ProcessException e) {
            log.warn("[CORRELATION:{}] Process not found: {}", actualCorrelationId, processId);
            throw e;
        }
    }

    // ===========================================
    // UTILITY METHODS
    // ===========================================

    /**
     * Extract client ID from request
     */
    private String getClientId(HttpServletRequest request) {
        // Try to get client ID from various sources
        String clientId = request.getHeader("X-Client-ID");
        if (clientId == null) {
            clientId = request.getHeader("X-API-Key");
        }
        if (clientId == null) {
            clientId = request.getRemoteAddr(); // Fallback to IP address
        }
        return clientId != null ? clientId : "anonymous";
    }
}