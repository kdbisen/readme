# Complete Fenergo Integration Flow - Banking Onboarding Service

## Overview

The Banking Onboarding Service now implements a **complete 4-step Fenergo integration flow** that follows the exact API patterns and best practices you provided. The flow is **synchronous**, **config-driven**, and **easily testable**.

## Flow Architecture

```
XML Input → Step 0: XML→JSON (Apigee) → Step 1: Create Entity → Step 2: Evaluate Schema → Step 3: Launch Journey
```

### Step 0: XML to JSON Transformation via Apigee (Internal API)
- **Purpose**: Transform XML input data to JSON format
- **API**: Internal Apigee transformation service
- **Input**: XML data (any format)
- **Output**: JSON data
- **Data Sharing**: JSON data passed to Step 1

### Step 1: Create Entity via Fenergo Entity API
- **Purpose**: Create entity in Fenergo system
- **API**: `POST /entity`
- **Input**: JSON data from Step 0
- **Output**: `entityId` (UUID)
- **Data Sharing**: `entityId` passed to Step 2

### Step 2: Evaluate Journey Schema via Logic Engine
- **Purpose**: Determine correct journey schema based on entity attributes
- **API**: `POST /journeylogicengine/api/engine/evaluate-journey-schema`
- **Input**: `entityId` from Step 1
- **Output**: `journeySchemaId`, `journeySchemaVersion`, `schemaName`
- **Data Sharing**: Schema information passed to Step 3

### Step 3: Launch Journey via Journey Command API
- **Purpose**: Launch the journey using Direct Launch approach
- **API**: `POST /api/journey-instance/launch-journey`
- **Input**: Schema information from Step 2
- **Output**: Journey launch confirmation
- **Data Sharing**: Complete journey information stored

## API Request/Response Patterns

### Step 1: Entity Creation
```http
POST /entity
Headers:
  Authorization: Bearer <token>
  X-TENANT-ID: <tenant-uuid>
  X-CORRELATION-ID: <uuid>

Body:
{
  "data": {
    "type": "Company",
    "targetEntity": "Client",
    "properties": {
      "name": { "type": "Single", "value": "Acme Ltd" },
      "jurisdiction": { "type": "Single", "value": "US" }
    },
    "policyJurisdictions": ["US"]
  }
}

Response:
{
  "data": {
    "entityId": "58e4b43a-..."
  }
}
```

### Step 2: Schema Evaluation
```http
POST /journeylogicengine/api/engine/evaluate-journey-schema?journeyTypeFilter=Client Onboarding
Headers:
  Authorization: Bearer <token>
  X-TENANT-ID: <tenant-uuid>
  X-CORRELATION-ID: <uuid>

Body:
{
  "data": {
    "entityType": "Company",
    "jurisdiction": "US",
    "entityId": "58e4b43a-..."
  }
}

Response:
{
  "data": [
    {
      "journeySchemaId": "118313c6-...",
      "journeySchemaVersion": 2,
      "name": "US Corporate Onboarding",
      "journeyType": "Client Onboarding"
    }
  ]
}
```

### Step 3: Journey Launch
```http
POST /api/journey-instance/launch-journey
Headers:
  Authorization: Bearer <token>
  X-TENANT-ID: <tenant-uuid>
  X-CORRELATION-ID: <uuid>

Body:
{
  "data": {
    "entityId": "58e4b43a-...",
    "journeyType": "Client Onboarding",
    "journeySchemaId": "118313c6-...",
    "journeySchemaVersionNumber": 2,
    "jurisdictions": ["US"]
  }
}

Response:
{
  "data": {
    "journeyInstanceId": "abc123-...",
    "status": "LAUNCHED"
  }
}
```

## Configuration

### Environment Variables
```bash
# Step Configuration
export ONBOARDING_STEPS_DEFINITION="XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH"

# Fenergo API URLs
export FENERGO_ENTITY_API_URL="https://fenergo.example.com/entity"
export FENERGO_LOGIC_ENGINE_URL="https://fenergo.example.com/journeylogicengine/api/engine/evaluate-journey-schema"
export FENERGO_JOURNEY_COMMAND_URL="https://fenergo.example.com/api/journey-instance/launch-journey"

# Fenergo Configuration
export FENERGO_TENANT_ID="your-tenant-uuid"
export FENERGO_JOURNEY_TYPE_FILTER="Client Onboarding"
```

### Application Properties
```properties
# Step Configuration - Complete Fenergo Flow
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH
onboarding.steps.retry.enabled=true
onboarding.steps.retry.max-attempts=3
onboarding.steps.retry.delay-ms=1000
onboarding.steps.retry.backoff-multiplier=2.0
onboarding.steps.timeout-ms=30000

# Fenergo API Configuration
fenergo.entity.api.url=https://fenergo.example.com/entity
fenergo.logic.engine.url=https://fenergo.example.com/journeylogicengine/api/engine/evaluate-journey-schema
fenergo.journey.command.url=https://fenergo.example.com/api/journey-instance/launch-journey
fenergo.tenant.id=your-tenant-uuid
fenergo.journey.type.filter=Client Onboarding
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

## Data Flow Between Steps

### Step 0 → Step 1
```java
// Step 0 outputs JSON data
String jsonData = (String) context.getStepResult("XML_TO_JSON_TRANSFORMATION");

// Step 1 receives JSON data and creates entity
Map<String, Object> entityPayload = buildEntityPayload(jsonData);
String entityId = createEntity(entityPayload);
```

### Step 1 → Step 2
```java
// Step 1 outputs entityId
String entityId = (String) context.getStepResult("FENERGO_ENTITY_CREATION");

// Step 2 receives entityId and evaluates schema
Map<String, Object> evaluationPayload = buildEvaluationPayload(entityId);
Map<String, Object> schemaInfo = evaluateSchema(evaluationPayload);
```

### Step 2 → Step 3
```java
// Step 2 outputs schema information
Map<String, Object> schemaInfo = context.getStepResult("FENERGO_JOURNEY_SCHEMA_EVALUATION", Map.class);
String entityId = (String) schemaInfo.get("entityId");
String journeySchemaId = (String) schemaInfo.get("journeySchemaId");

// Step 3 receives schema info and launches journey
Map<String, Object> launchPayload = buildLaunchPayload(entityId, journeySchemaId);
Map<String, Object> launchResult = launchJourney(launchPayload);
```

## Error Handling & Best Practices

### Schema Selection Rules
When Logic Engine returns multiple schemas, the system applies these rules:

1. **Filter by `journeyType`** (if supplied)
2. **Prefer highest `journeySchemaVersion`**
3. **Prefer explicit jurisdiction match**
4. **Respect `channelType`** (internal vs external)

### Error Handling
- **400 Bad Request**: Missing/invalid payload
- **401 Unauthorized**: Missing/invalid token
- **403 Forbidden**: Missing permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Version conflict
- **500 Internal Server Error**: Backend error

### Retry Logic
- **Max Retries**: 3 attempts per step
- **Retry Delay**: 1000ms base delay
- **Backoff Multiplier**: 2.0 (exponential backoff)
- **Timeout**: 30 seconds per step

## Testing

### Unit Tests
```java
@Test
public void testCompleteFenergoFlow() {
    GenericStepContext context = GenericStepContext.create("test-correlation", "test-process", xmlData);
    
    // Test Step 0
    StepResult<Object> result0 = stepExecutionEngine.executeStep("XML_TO_JSON_TRANSFORMATION", context);
    assertThat(result0.isSuccess()).isTrue();
    
    // Test Step 1
    StepResult<Object> result1 = stepExecutionEngine.executeStep("FENERGO_ENTITY_CREATION", context);
    assertThat(result1.isSuccess()).isTrue();
    
    // Test Step 2
    StepResult<Object> result2 = stepExecutionEngine.executeStep("FENERGO_JOURNEY_SCHEMA_EVALUATION", context);
    assertThat(result2.isSuccess()).isTrue();
    
    // Test Step 3
    StepResult<Object> result3 = stepExecutionEngine.executeStep("FENERGO_JOURNEY_LAUNCH", context);
    assertThat(result3.isSuccess()).isTrue();
}
```

### Integration Tests
```bash
# Test complete flow
curl -X POST "http://localhost:8080/api/v1/onboarding/process-entity" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-$(date +%s)" \
  -d '{
    "xmlData": "<customer><name>Test Company</name><jurisdiction>US</jurisdiction></customer>",
    "requestType": "ADD_KYC"
  }'
```

## Monitoring & Observability

### Logging
Each step logs:
- **Correlation ID**: For tracing across steps
- **Step execution time**: Performance monitoring
- **Data type information**: Type safety verification
- **Success/failure status**: Step health monitoring

### Metrics
- **Step execution count**: How many times each step runs
- **Step success rate**: Percentage of successful executions
- **Step duration**: Average execution time per step
- **Error rate**: Failed step executions

### Correlation Tracking
```java
log.info("[CORRELATION:{}] Step {} completed successfully. Data shared: {}", 
        context.getCorrelationId(), stepName, 
        result.getData() != null ? result.getData().getClass().getSimpleName() : "null");
```

## Benefits of This Implementation

### 1. **Follows Fenergo Best Practices**
- Uses exact API patterns you provided
- Implements proper schema selection rules
- Handles all required headers and payloads

### 2. **Synchronous & Simple**
- No async complexity
- Easy to debug with clear stack traces
- Direct method calls for testing

### 3. **Config-Driven**
- Steps defined in properties
- Environment-specific configurations
- Easy to modify flow without code changes

### 4. **Generic Data Sharing**
- Any data type flows between steps
- Type information preserved in logs
- Flexible input/output handling

### 5. **Production Ready**
- Comprehensive error handling
- Retry logic with exponential backoff
- Monitoring and observability
- Correlation ID tracking

This implementation provides a robust, maintainable, and production-ready solution for Fenergo integration while maintaining simplicity and ease of use.






