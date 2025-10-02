# Generic Step Pattern - Banking Onboarding Service

## Overview

The Banking Onboarding Service now uses a **Generic Step Pattern** that provides:

- **Type-agnostic data sharing** between steps
- **Config-driven step definitions**
- **Easy testing and debugging**
- **Flexible step orchestration**
- **Automatic step registration**

## Key Components

### 1. GenericStepContext
The core data container that holds:
- Input data (any type)
- Step results (any type)
- Metadata
- Correlation ID and Process ID

```java
GenericStepContext context = GenericStepContext.create(correlationId, processId, xmlData);
```

### 2. GenericStepExecutor Interface
All step implementations must implement this interface:

```java
public interface GenericStepExecutor {
    CompletableFuture<StepResult<Object>> execute(GenericStepContext context);
    StepConfig getConfig();
    String getStepName();
    boolean canExecute(GenericStepContext context);
}
```

### 3. GenericStepExecutionEngine
Orchestrates step execution with:
- Automatic step registration
- Sequential step execution
- Generic data sharing
- Error handling

### 4. StepConfig
Configuration-driven step definitions:

```java
StepConfig.builder()
    .stepName("XML_TO_JSON_TRANSFORMATION")
    .description("Transform XML data to JSON format via Apigee")
    .retryEnabled(true)
    .maxRetries(3)
    .dependencies(new String[]{"PREVIOUS_STEP"})
    .build();
```

## Data Sharing Between Steps

### How It Works

1. **Step 1** executes and stores its result in the context
2. **Step 2** automatically gets the result from Step 1 as input
3. **Step 3** gets the result from Step 2, and so on...

### Example Flow

```java
// Step 1: XML to JSON Transformation
GenericStepContext context = GenericStepContext.create(correlationId, processId, xmlData);
StepResult<Object> result1 = executeStep("XML_TO_JSON_TRANSFORMATION", context);
context.addStepResult("XML_TO_JSON_TRANSFORMATION", result1.getData());

// Step 2: Fenergo Entity Creation (automatically gets JSON from Step 1)
StepResult<Object> result2 = executeStep("FENERGO_ENTITY_CREATION", context);
context.addStepResult("FENERGO_ENTITY_CREATION", result2.getData());

// Step 3: Journey Info (automatically gets entity data from Step 2)
StepResult<Object> result3 = executeStep("FENERGO_JOURNEY_INFO", context);
```

### Data Type Flexibility

The system handles any data type:

```java
// Step can receive String, JSON, XML, Object, etc.
Object inputData = context.getStepResult("PREVIOUS_STEP");
String jsonData = context.getStepResult("PREVIOUS_STEP", String.class);
```

## Configuration

### Step Definitions
Configure steps in `application.properties`:

```properties
# Step Configuration
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_INFO,FENERGO_JOURNEY_INITIATE,FENERGO_JOURNEY_DETAILS
onboarding.steps.retry.enabled=true
onboarding.steps.retry.max-attempts=3
onboarding.steps.retry.delay-ms=1000
onboarding.steps.retry.backoff-multiplier=2.0
onboarding.steps.timeout-ms=30000
```

### Environment Variables
Override configuration via environment variables:

```bash
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION"
export ONBOARDING_STEPS_RETRY_MAX_ATTEMPTS=5
```

## API Endpoints

### Main Onboarding Flow
```http
POST /api/v1/onboarding/process-entity
Content-Type: application/json
X-Correlation-ID: your-correlation-id

{
    "xmlData": "<customer><name>John Doe</name><email>john@example.com</email></customer>",
    "requestType": "ADD_KYC"
}
```

### Test Individual Steps
```http
POST /api/v1/onboarding/test/step/XML_TO_JSON_TRANSFORMATION
Content-Type: application/json
X-Correlation-ID: test-correlation-id

{
    "inputData": "<customer><name>John Doe</name></customer>"
}
```

### Get Process Status
```http
GET /api/v1/onboarding/status/PROC-ABC12345
```

### Monitoring and Metrics
```http
GET /api/v1/onboarding/metrics/process
GET /api/v1/onboarding/metrics/steps
GET /api/v1/onboarding/metrics/steps/XML_TO_JSON_TRANSFORMATION
```

## Step Implementation Example

```java
@Component
@RequiredArgsConstructor
public class GenericXmlToJsonTransformationStep implements GenericStepExecutor {
    
    private final TransformationService transformationService;
    
    @Override
    public CompletableFuture<StepResult<Object>> execute(GenericStepContext context) {
        // Get input data from previous step or initial input
        Object inputData = getInputData(context);
        String xmlData = convertToString(inputData);
        
        return transformationService.transformXmlToJson(xmlData, context.getCorrelationId())
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        // Store result for next steps
                        context.addStepResult(getStepName(), response.getBody());
                        return StepResult.success(response.getBody(), getStepName(), context.getCorrelationId());
                    } else {
                        return StepResult.failure(response.getErrorMessage(), getStepName(), context.getCorrelationId());
                    }
                });
    }
    
    @Override
    public StepConfig getConfig() {
        return StepConfig.builder()
                .stepName(getStepName())
                .description("Transform XML data to JSON format via Apigee")
                .retryEnabled(true)
                .maxRetries(3)
                .build();
    }
    
    @Override
    public String getStepName() {
        return "XML_TO_JSON_TRANSFORMATION";
    }
}
```

## Benefits

### 1. **Type Safety with Flexibility**
- Steps can handle any data type
- Automatic type conversion where needed
- Type information preserved in logs

### 2. **Easy Testing**
- Test individual steps in isolation
- Mock data sharing between steps
- Clear step boundaries

### 3. **Easy Debugging**
- Each step's input/output is logged
- Data type information in logs
- Step execution timing
- Clear error messages

### 4. **Configuration Driven**
- Steps defined in properties
- Environment-specific configurations
- Easy to add/remove steps

### 5. **Automatic Registration**
- Steps auto-register on startup
- No manual wiring required
- Spring Boot integration

## Usage Examples

### Adding a New Step

1. **Create Step Implementation**:
```java
@Component
public class MyCustomStep implements GenericStepExecutor {
    // Implementation
}
```

2. **Add to Configuration**:
```properties
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,MY_CUSTOM_STEP
```

3. **Step is automatically registered and available**

### Testing Steps

```java
@Test
public void testXmlToJsonStep() {
    GenericStepContext context = GenericStepContext.create("test-correlation", "test-process", xmlData);
    StepResult<Object> result = stepExecutionEngine.executeStep("XML_TO_JSON_TRANSFORMATION", context);
    
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isNotNull();
}
```

## Architecture Benefits

- **Clean Separation**: Each step is independent
- **Reusable**: Steps can be used in different flows
- **Testable**: Easy unit testing
- **Maintainable**: Clear interfaces and contracts
- **Scalable**: Easy to add new steps
- **Debuggable**: Clear logging and error handling

This generic step pattern provides a robust, flexible, and maintainable foundation for complex business processes while maintaining simplicity and ease of use.

