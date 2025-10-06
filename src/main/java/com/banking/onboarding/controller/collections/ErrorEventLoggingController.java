package com.banking.onboarding.controller.collections;

import com.banking.onboarding.model.collections.ErrorEvent;
import com.banking.onboarding.service.collections.ErrorEventLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error Event Logging Controller
 * Provides REST endpoints for error event management and analysis
 */
@Slf4j
@RestController
@RequestMapping("/api/error-events")
@RequiredArgsConstructor
public class ErrorEventLoggingController {

    private final ErrorEventLoggingService errorEventLoggingService;

    /**
     * Log a new error event
     */
    @PostMapping("/log")
    public ResponseEntity<ErrorEvent> logError(@RequestBody ErrorEventLoggingService.ErrorEventRequest request) {
        log.info("[CORRELATION:{}] Logging error event via API", request.getCorrelationId());
        
        try {
            ErrorEvent errorEvent = errorEventLoggingService.logError(request);
            return ResponseEntity.ok(errorEvent);
        } catch (Exception e) {
            log.error("Failed to log error event: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Log error from exception
     */
    @PostMapping("/log-exception")
    public ResponseEntity<ErrorEvent> logErrorFromException(
            @RequestParam String processId,
            @RequestParam String stepId,
            @RequestParam String correlationId,
            @RequestBody Exception exception,
            @RequestBody(required = false) Map<String, Object> context) {
        
        log.info("[CORRELATION:{}] Logging error from exception via API", correlationId);
        
        try {
            ErrorEvent errorEvent = errorEventLoggingService.logErrorFromException(
                    processId, stepId, correlationId, exception, context);
            return ResponseEntity.ok(errorEvent);
        } catch (Exception e) {
            log.error("Failed to log error from exception: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update error event with retry information
     */
    @PutMapping("/{errorEventId}/retry")
    public ResponseEntity<ErrorEvent> updateErrorWithRetry(
            @PathVariable String errorEventId,
            @RequestParam int retryCount,
            @RequestParam String retryResult) {
        
        log.info("Updating error event {} with retry via API", errorEventId);
        
        try {
            ErrorEvent errorEvent = errorEventLoggingService.updateErrorWithRetry(
                    errorEventId, retryCount, retryResult);
            if (errorEvent == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(errorEvent);
        } catch (Exception e) {
            log.error("Failed to update error with retry: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolve error event
     */
    @PutMapping("/{errorEventId}/resolve")
    public ResponseEntity<ErrorEvent> resolveError(
            @PathVariable String errorEventId,
            @RequestParam String resolvedBy,
            @RequestParam String resolutionAction,
            @RequestParam(required = false) String resolutionNotes) {
        
        log.info("Resolving error event {} via API", errorEventId);
        
        try {
            ErrorEvent errorEvent = errorEventLoggingService.resolveError(
                    errorEventId, resolvedBy, resolutionAction, resolutionNotes);
            if (errorEvent == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(errorEvent);
        } catch (Exception e) {
            log.error("Failed to resolve error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get error analysis for a process
     */
    @GetMapping("/analysis/process/{processId}")
    public ResponseEntity<ErrorEventLoggingService.ErrorAnalysis> getErrorAnalysis(
            @PathVariable String processId) {
        
        log.info("Getting error analysis for process {} via API", processId);
        
        try {
            ErrorEventLoggingService.ErrorAnalysis analysis = 
                    errorEventLoggingService.getErrorAnalysis(processId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Failed to get error analysis: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get unresolved errors
     */
    @GetMapping("/unresolved")
    public ResponseEntity<List<ErrorEvent>> getUnresolvedErrors() {
        log.info("Getting unresolved errors via API");
        
        try {
            List<ErrorEvent> errors = errorEventLoggingService.getUnresolvedErrors();
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Failed to get unresolved errors: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get critical errors
     */
    @GetMapping("/critical")
    public ResponseEntity<List<ErrorEvent>> getCriticalErrors() {
        log.info("Getting critical errors via API");
        
        try {
            List<ErrorEvent> errors = errorEventLoggingService.getCriticalErrors();
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Failed to get critical errors: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user-facing errors
     */
    @GetMapping("/user-facing")
    public ResponseEntity<List<ErrorEvent>> getUserFacingErrors() {
        log.info("Getting user-facing errors via API");
        
        try {
            List<ErrorEvent> errors = errorEventLoggingService.getUserFacingErrors();
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Failed to get user-facing errors: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get errors by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<ErrorEvent>> getErrorsByCorrelationId(
            @PathVariable String correlationId) {
        
        log.info("[CORRELATION:{}] Getting errors via API", correlationId);
        
        try {
            List<ErrorEvent> errors = errorEventLoggingService.getErrorsByCorrelationId(correlationId);
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            log.error("Failed to get errors by correlation ID: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get error statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ErrorEventLoggingService.ErrorStatistics> getErrorStatistics(
            @RequestParam(required = false) String since) {
        
        log.info("Getting error statistics via API");
        
        try {
            LocalDateTime sinceDate = since != null ? 
                    LocalDateTime.parse(since) : 
                    LocalDateTime.now().minusDays(7); // Default to last 7 days
            
            ErrorEventLoggingService.ErrorStatistics statistics = 
                    errorEventLoggingService.getErrorStatistics(sinceDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Failed to get error statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get error event by ID
     */
    @GetMapping("/{errorEventId}")
    public ResponseEntity<ErrorEvent> getErrorEvent(@PathVariable String errorEventId) {
        log.info("Getting error event {} via API", errorEventId);
        
        try {
            // This would need to be implemented in the service
            // For now, return a placeholder response
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to get error event: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ErrorEventLoggingService",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
