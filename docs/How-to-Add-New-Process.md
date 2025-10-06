# How to Add Another Process with Multiple Steps

## Overview

This guide demonstrates how to add a new process with multiple steps to the Banking Onboarding Service. We'll use the **Document Verification Process** as an example, which includes 5 sequential steps.

## Step-by-Step Guide

### Step 1: Define Constants

Add new constants to `OnboardingConstants.java`:

#### 1.1 Add Request Types
```java
// In RequestTypes class
public static final String DOCUMENT_VERIFICATION = "DOCUMENT_VERIFICATION";
public static final String IDENTITY_VERIFICATION = "IDENTITY_VERIFICATION";
public static final String ADDRESS_VERIFICATION = "ADDRESS_VERIFICATION";
public static final String INCOME_VERIFICATION = "INCOME_VERIFICATION";
```

#### 1.2 Add Step Names
```java
// In StepNames class
public static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
public static final String DOCUMENT_VALIDATION = "DOCUMENT_VALIDATION";
public static final String OCR_PROCESSING = "OCR_PROCESSING";
public static final String COMPLIANCE_CHECK = "COMPLIANCE_CHECK";
public static final String DOCUMENT_APPROVAL = "DOCUMENT_APPROVAL";
```

#### 1.3 Add Step Descriptions
```java
// In StepDescriptions class
public static final String DOCUMENT_UPLOAD = "Upload and validate document files";
public static final String DOCUMENT_VALIDATION = "Validate document format and content";
public static final String OCR_PROCESSING = "Extract text using OCR technology";
public static final String COMPLIANCE_CHECK = "Check document compliance requirements";
public static final String DOCUMENT_APPROVAL = "Approve document for processing";
```

#### 1.4 Add Step Priorities
```java
// In StepPriorities class
public static final int DOCUMENT_UPLOAD = 10;
public static final int DOCUMENT_VALIDATION = 20;
public static final int OCR_PROCESSING = 30;
public static final int COMPLIANCE_CHECK = 40;
public static final int DOCUMENT_APPROVAL = 50;
```

#### 1.5 Add Step Dependencies
```java
// In StepDependencies class
public static final String[] DOCUMENT_UPLOAD = {};
public static final String[] DOCUMENT_VALIDATION = {"DOCUMENT_UPLOAD"};
public static final String[] OCR_PROCESSING = {"DOCUMENT_VALIDATION"};
public static final String[] COMPLIANCE_CHECK = {"OCR_PROCESSING"};
public static final String[] DOCUMENT_APPROVAL = {"COMPLIANCE_CHECK"};
```

### Step 2: Create Step Implementations

Create individual step classes implementing `GenericStepExecutor`:

#### 2.1 DocumentUploadStep.java
```java
@Component
public class DocumentUploadStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Step implementation
        // Process document upload
        // Return success/failure result
    }
    
    @Override
    public String getStepName() {
        return StepNames.DOCUMENT_UPLOAD;
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description(StepDescriptions.DOCUMENT_UPLOAD)
                .dependencies(StepDependencies.DOCUMENT_UPLOAD)
                .properties(Map.of("priority", StepPriorities.DOCUMENT_UPLOAD))
                .build();
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getInputData() != null;
    }
}
```

#### 2.2 DocumentValidationStep.java
```java
@Component
public class DocumentValidationStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get result from previous step
        Object uploadResult = context.getStepResult(StepNames.DOCUMENT_UPLOAD);
        
        // Validate documents
        // Return validation result
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getStepResult(StepNames.DOCUMENT_UPLOAD) != null;
    }
}
```

#### 2.3 OCRProcessingStep.java
```java
@Component
public class OcrProcessingStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get validation result
        Object validationResult = context.getStepResult(StepNames.DOCUMENT_VALIDATION);
        
        // Perform OCR processing
        // Extract text and data
        // Return OCR result
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getStepResult(StepNames.DOCUMENT_VALIDATION) != null;
    }
}
```

#### 2.4 ComplianceCheckStep.java
```java
@Component
public class ComplianceCheckStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get OCR result
        Object ocrResult = context.getStepResult(StepNames.OCR_PROCESSING);
        
        // Perform compliance checks
        // Check against regulatory requirements
        // Return compliance result
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getStepResult(StepNames.OCR_PROCESSING) != null;
    }
}
```

#### 2.5 DocumentApprovalStep.java
```java
@Component
public class DocumentApprovalStep implements GenericStepExecutor {
    
    @Override
    public StepResult<Object> execute(GenericStepContext context) {
        // Get compliance result
        Object complianceResult = context.getStepResult(StepNames.COMPLIANCE_CHECK);
        
        // Consolidate all results
        // Make final approval decision
        // Return approval result
    }
    
    @Override
    public boolean canExecute(GenericStepContext context) {
        return context.getStepResult(StepNames.COMPLIANCE_CHECK) != null;
    }
}
```

### Step 3: Create Process Service

Create a service to orchestrate the entire process:

#### 3.1 DocumentVerificationService.java
```java
@Service
@RequiredArgsConstructor
public class DocumentVerificationService {

    private final GenericStepExecutionEngine stepExecutionEngine;
    private final OnboardingProcessRepository processRepository;

    public OnboardingProcess executeDocumentVerificationProcess(
            String documentData, String requestType, String correlationId) {
        
        // Create process record
        OnboardingProcess process = createInitialProcess(processId, correlationId, requestType, documentData);
        processRepository.save(process);
        
        try {
            // Create step context
            GenericStepContext context = GenericStepContext.create(correlationId, processId, documentData);
            
            // Execute all steps in sequence
            StepResult<Object> result = executeDocumentVerificationSteps(context);
            
            if (result.isSuccess()) {
                process.setStatus(OnboardingProcess.ProcessStatus.COMPLETED);
                process.setCompletedAt(LocalDateTime.now());
            } else {
                process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
                process.setErrorMessage(result.getErrorMessage());
            }
            
        } catch (Exception e) {
            process.setStatus(OnboardingProcess.ProcessStatus.FAILED);
            process.setErrorMessage("Process failed: " + e.getMessage());
        }
        
        processRepository.save(process);
        return process;
    }

    private StepResult<Object> executeDocumentVerificationSteps(GenericStepContext context) {
        // Execute steps in sequence
        StepResult<Object> uploadResult = stepExecutionEngine.executeStep(StepNames.DOCUMENT_UPLOAD, context);
        if (!uploadResult.isSuccess()) return uploadResult;
        
        StepResult<Object> validationResult = stepExecutionEngine.executeStep(StepNames.DOCUMENT_VALIDATION, context);
        if (!validationResult.isSuccess()) return validationResult;
        
        StepResult<Object> ocrResult = stepExecutionEngine.executeStep(StepNames.OCR_PROCESSING, context);
        if (!ocrResult.isSuccess()) return ocrResult;
        
        StepResult<Object> complianceResult = stepExecutionEngine.executeStep(StepNames.COMPLIANCE_CHECK, context);
        if (!complianceResult.isSuccess()) return complianceResult;
        
        StepResult<Object> approvalResult = stepExecutionEngine.executeStep(StepNames.DOCUMENT_APPROVAL, context);
        if (!approvalResult.isSuccess()) return approvalResult;
        
        // Return consolidated result
        Map<String, Object> finalResult = Map.of(
            "processType", "DOCUMENT_VERIFICATION",
            "totalSteps", 5,
            "completedSteps", 5,
            "finalStatus", "APPROVED",
            "stepResults", Map.of(
                "upload", uploadResult.getData(),
                "validation", validationResult.getData(),
                "ocr", ocrResult.getData(),
                "compliance", complianceResult.getData(),
                "approval", approvalResult.getData()
            )
        );
        
        return StepResult.success(finalResult, "DOCUMENT_VERIFICATION_COMPLETE", context.getCorrelationId());
    }
}
```

### Step 4: Update Step Configuration

Add new steps to `StepConfigurationLoader.java`:

```java
private static final Map<String, StepInfo> STEPS = Map.of(
    // Existing Entity Onboarding Steps
    StepNames.XML_TO_JSON_TRANSFORMATION, new StepInfo(...),
    StepNames.FENERGO_ENTITY_CREATION, new StepInfo(...),
    StepNames.FENERGO_JOURNEY_SCHEMA_EVALUATION, new StepInfo(...),
    StepNames.FENERGO_JOURNEY_LAUNCH, new StepInfo(...),
    
    // New Document Verification Steps
    StepNames.DOCUMENT_UPLOAD, 
        new StepInfo(StepPriorities.DOCUMENT_UPLOAD,
                    StepDescriptions.DOCUMENT_UPLOAD,
                    StepDependencies.DOCUMENT_UPLOAD),
    
    StepNames.DOCUMENT_VALIDATION,
        new StepInfo(StepPriorities.DOCUMENT_VALIDATION,
                    StepDescriptions.DOCUMENT_VALIDATION,
                    StepDependencies.DOCUMENT_VALIDATION),
    
    StepNames.OCR_PROCESSING,
        new StepInfo(StepPriorities.OCR_PROCESSING,
                    StepDescriptions.OCR_PROCESSING,
                    StepDependencies.OCR_PROCESSING),
    
    StepNames.COMPLIANCE_CHECK,
        new StepInfo(StepPriorities.COMPLIANCE_CHECK,
                    StepDescriptions.COMPLIANCE_CHECK,
                    StepDependencies.COMPLIANCE_CHECK),
    
    StepNames.DOCUMENT_APPROVAL,
        new StepInfo(StepPriorities.DOCUMENT_APPROVAL,
                    StepDescriptions.DOCUMENT_APPROVAL,
                    StepDependencies.DOCUMENT_APPROVAL)
);
```

### Step 5: Add Controller Endpoint

Add new endpoint to `OnboardingController.java`:

```java
@PostMapping("/verify-documents")
public ResponseEntity<OnboardingProcess> verifyDocuments(
        @RequestBody Map<String, String> request,
        @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

    // Process correlation ID
    CorrelationIdStrategyService.SimpleCorrelationResult correlationResult = 
        correlationIdStrategyService.processCorrelationId(correlationId, request);
    
    String actualCorrelationId = correlationResult.getCorrelationId();
    String documentData = request.get("documentData");
    String requestType = request.getOrDefault("requestType", RequestTypes.DOCUMENT_VERIFICATION);
    
    // Validate input
    if (documentData == null || documentData.trim().isEmpty()) {
        throw new ValidationException("Document data is required and cannot be empty", actualCorrelationId);
    }
    
    // Execute process
    OnboardingProcess process = documentVerificationService.executeDocumentVerificationProcess(
            documentData, requestType, actualCorrelationId);
    
    return ResponseEntity.ok(process);
}
```

## Process Flow Example

### Document Verification Process Flow

```
1. Document Upload (Priority: 10)
   ↓
2. Document Validation (Priority: 20, Depends on: Document Upload)
   ↓
3. OCR Processing (Priority: 30, Depends on: Document Validation)
   ↓
4. Compliance Check (Priority: 40, Depends on: OCR Processing)
   ↓
5. Document Approval (Priority: 50, Depends on: Compliance Check)
```

### API Usage Example

#### Request
```bash
POST /api/v1/onboarding/verify-documents
Content-Type: application/json
X-Correlation-ID: DOC-VERIFY-001

{
  "documentData": "base64-encoded-document-data",
  "requestType": "DOCUMENT_VERIFICATION"
}
```

#### Response
```json
{
  "processId": "PROC-ABC12345",
  "correlationId": "DOC-VERIFY-001",
  "requestType": "DOCUMENT_VERIFICATION",
  "status": "COMPLETED",
  "createdAt": "2024-01-15T10:30:00",
  "completedAt": "2024-01-15T10:35:00",
  "steps": [
    {
      "stepName": "DOCUMENT_UPLOAD",
      "status": "COMPLETED",
      "result": {...}
    },
    {
      "stepName": "DOCUMENT_VALIDATION", 
      "status": "COMPLETED",
      "result": {...}
    }
    // ... other steps
  ]
}
```

## Key Benefits

### 1. **Modular Design**
- Each step is independent and reusable
- Easy to modify individual steps without affecting others
- Clear separation of concerns

### 2. **Dependency Management**
- Steps execute in correct order based on dependencies
- Automatic validation of step prerequisites
- Flexible execution order

### 3. **Error Handling**
- Individual step failure handling
- Process-level error tracking
- Detailed error messages and logging

### 4. **Monitoring & Tracking**
- Complete process traceability
- Step-by-step execution monitoring
- Performance metrics collection

### 5. **Scalability**
- Easy to add new steps
- Support for parallel execution (future enhancement)
- Configurable step priorities

## Best Practices

### 1. **Step Design**
- Keep steps focused on single responsibility
- Use descriptive step names and descriptions
- Implement proper error handling

### 2. **Dependency Management**
- Define clear dependencies between steps
- Use meaningful priority values
- Avoid circular dependencies

### 3. **Data Flow**
- Pass data between steps using context
- Store intermediate results appropriately
- Maintain data consistency

### 4. **Error Handling**
- Implement comprehensive error handling
- Provide meaningful error messages
- Log errors with correlation IDs

### 5. **Testing**
- Test individual steps in isolation
- Test complete process flows
- Test error scenarios

## Future Enhancements

### 1. **Parallel Execution**
- Execute independent steps in parallel
- Improve overall process performance
- Maintain dependency constraints

### 2. **Dynamic Step Configuration**
- Runtime step configuration
- Environment-specific step variations
- A/B testing capabilities

### 3. **Step Versioning**
- Multiple versions of same step
- Gradual rollout capabilities
- Rollback mechanisms

### 4. **Advanced Monitoring**
- Real-time process monitoring
- Performance analytics
- Predictive failure detection

This architecture provides a robust, scalable foundation for adding new processes with multiple steps while maintaining code quality and operational excellence.

