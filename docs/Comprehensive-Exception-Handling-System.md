# Comprehensive Exception Handling System

## Overview

The Banking Onboarding Service implements a comprehensive global exception handling system that provides detailed, structured error responses with proper correlation ID tracking, trace IDs, and helpful suggestions for API consumers.

## Architecture

### Exception Hierarchy

```
BusinessException (Base)
├── ValidationException
├── ProcessException
├── ExternalApiException
└── StepExecutionException
```

### Global Exception Handler

The `GlobalExceptionHandler` is annotated with `@RestControllerAdvice` and handles all exceptions globally across the application.

## Exception Types

### 1. Business Exceptions

#### BusinessException (Base)
- **Purpose**: Base class for all business-related errors
- **HTTP Status**: 400 Bad Request
- **Fields**: `errorCode`, `correlationId`
- **Usage**: General business logic violations

#### ValidationException
- **Purpose**: Input validation errors
- **HTTP Status**: 400 Bad Request
- **Usage**: XML/JSON validation failures, missing required fields
- **Example**: Empty XML data, invalid request format

#### ProcessException
- **Purpose**: Process-related errors
- **HTTP Status**: 422 Unprocessable Entity
- **Usage**: Process not found, process state violations
- **Example**: Process ID not found in database

#### ExternalApiException
- **Purpose**: External API call failures
- **HTTP Status**: 502 Bad Gateway
- **Fields**: `apiProvider`, `endpoint`, `httpStatus`
- **Usage**: Fenergo API failures, Apigee service errors
- **Example**: Fenergo API timeout, Apigee authentication failure

#### StepExecutionException
- **Purpose**: Step execution failures
- **HTTP Status**: 422 Unprocessable Entity
- **Fields**: `stepName`, `processId`
- **Usage**: Individual step failures in the onboarding flow
- **Example**: XML transformation failure, Fenergo entity creation failure

### 2. Spring Framework Exceptions

#### MethodArgumentNotValidException
- **Purpose**: @Valid annotation validation failures
- **HTTP Status**: 400 Bad Request
- **Details**: Field-level validation errors

#### BindException
- **Purpose**: Request binding failures
- **HTTP Status**: 400 Bad Request
- **Details**: Parameter binding errors

#### MissingServletRequestParameterException
- **Purpose**: Missing required parameters
- **HTTP Status**: 400 Bad Request
- **Details**: Parameter name and type information

#### MethodArgumentTypeMismatchException
- **Purpose**: Parameter type mismatches
- **HTTP Status**: 400 Bad Request
- **Details**: Expected vs actual parameter types

#### HttpMessageNotReadableException
- **Purpose**: Malformed request body
- **HTTP Status**: 400 Bad Request
- **Usage**: Invalid JSON/XML format

#### HttpRequestMethodNotSupportedException
- **Purpose**: Unsupported HTTP methods
- **HTTP Status**: 405 Method Not Allowed
- **Details**: Requested vs supported methods

#### NoHandlerFoundException
- **Purpose**: 404 Not Found
- **HTTP Status**: 404 Not Found
- **Usage**: Endpoint not found

### 3. General Exceptions

#### IllegalArgumentException
- **Purpose**: Invalid method arguments
- **HTTP Status**: 400 Bad Request

#### RuntimeException
- **Purpose**: Unexpected runtime errors
- **HTTP Status**: 500 Internal Server Error

#### Exception (Generic)
- **Purpose**: Catch-all for unhandled exceptions
- **HTTP Status**: 500 Internal Server Error

## Error Response Structure

### Enhanced ErrorResponse Model

```json
{
  "error": "VALIDATION_ERROR",
  "errorCode": "VALIDATION_ERROR",
  "message": "XML data is required and cannot be empty",
  "status": "BAD_REQUEST",
  "correlationId": "CORR-12345678",
  "traceId": "TRACE-87654321",
  "timestamp": "2024-01-15T10:30:45.123",
  "path": "/api/v1/onboarding/process-entity",
  "method": "POST",
  "details": {
    "fieldName": "xmlData",
    "validationError": "Field is required"
  },
  "metadata": {
    "stepName": "XML_TO_JSON_TRANSFORMATION",
    "processId": "PROC-12345678"
  },
  "suggestion": "Please check your input data and ensure all required fields are provided with valid values."
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `error` | String | Error category (e.g., "VALIDATION_ERROR") |
| `errorCode` | String | Specific error code for programmatic handling |
| `message` | String | Human-readable error message |
| `status` | HttpStatus | HTTP status code |
| `correlationId` | String | Request correlation ID for tracking |
| `traceId` | String | Unique trace ID for this error |
| `timestamp` | LocalDateTime | When the error occurred |
| `path` | String | API endpoint path |
| `method` | String | HTTP method |
| `details` | Map | Additional error details (field-level errors) |
| `metadata` | Map | Context-specific metadata (step info, API provider) |
| `suggestion` | String | Helpful suggestion for resolution |

## Usage Examples

### 1. Throwing Validation Exceptions

```java
// In Controller
if (xmlData == null || xmlData.trim().isEmpty()) {
    throw new ValidationException(
        "XML data is required and cannot be empty", 
        actualCorrelationId);
}
```

### 2. Throwing Process Exceptions

```java
// In Service
return processRepository.findById(processId)
    .orElseThrow(() -> new ProcessException(
        "Process not found with ID: " + processId));
```

### 3. Throwing External API Exceptions

```java
// In Service
if (!response.isSuccess()) {
    throw new ExternalApiException(
        "Fenergo API call failed: " + response.getErrorMessage(),
        "FENERGO",
        "/api/entity/create",
        response.getStatusCode(),
        correlationId);
}
```

### 4. Throwing Step Execution Exceptions

```java
// In Step Implementation
if (inputData == null) {
    throw new StepExecutionException(
        "No valid input data for step execution",
        "XML_TO_JSON_TRANSFORMATION",
        processId,
        correlationId);
}
```

## Error Response Examples

### 1. Validation Error

```json
{
  "error": "VALIDATION_ERROR",
  "errorCode": "VALIDATION_ERROR",
  "message": "XML data is required and cannot be empty",
  "status": "BAD_REQUEST",
  "correlationId": "CORR-12345678",
  "traceId": "TRACE-87654321",
  "timestamp": "2024-01-15T10:30:45.123",
  "path": "/api/v1/onboarding/process-entity",
  "method": "POST",
  "suggestion": "Please check your input data and ensure all required fields are provided with valid values."
}
```

### 2. External API Error

```json
{
  "error": "EXTERNAL_API_ERROR",
  "errorCode": "EXTERNAL_API_ERROR",
  "message": "Fenergo API call failed: Connection timeout",
  "status": "BAD_GATEWAY",
  "correlationId": "CORR-12345678",
  "traceId": "TRACE-87654321",
  "timestamp": "2024-01-15T10:30:45.123",
  "path": "/api/v1/onboarding/process-entity",
  "method": "POST",
  "metadata": {
    "apiProvider": "FENERGO",
    "endpoint": "/api/entity/create",
    "httpStatus": 504
  },
  "suggestion": "External service is temporarily unavailable. Please try again later."
}
```

### 3. Step Execution Error

```json
{
  "error": "STEP_EXECUTION_ERROR",
  "errorCode": "STEP_EXECUTION_ERROR",
  "message": "XML transformation failed: Invalid XML format",
  "status": "UNPROCESSABLE_ENTITY",
  "correlationId": "CORR-12345678",
  "traceId": "TRACE-87654321",
  "timestamp": "2024-01-15T10:30:45.123",
  "path": "/api/v1/onboarding/process-entity",
  "method": "POST",
  "metadata": {
    "stepName": "XML_TO_JSON_TRANSFORMATION",
    "processId": "PROC-12345678"
  },
  "suggestion": "Step execution failed. Please check the process status and retry if necessary."
}
```

### 4. Process Not Found Error

```json
{
  "error": "PROCESS_ERROR",
  "errorCode": "PROCESS_ERROR",
  "message": "Process not found with ID: PROC-INVALID",
  "status": "UNPROCESSABLE_ENTITY",
  "correlationId": "CORR-12345678",
  "traceId": "TRACE-87654321",
  "timestamp": "2024-01-15T10:30:45.123",
  "path": "/api/v1/onboarding/status/PROC-INVALID",
  "method": "GET",
  "suggestion": "Please check the process status and retry if necessary."
}
```

## Benefits

### 1. **Consistent Error Format**
- All errors follow the same structure
- Easy to parse programmatically
- Consistent field naming

### 2. **Detailed Error Information**
- Correlation ID for request tracking
- Trace ID for error tracking
- Context-specific metadata
- Helpful suggestions

### 3. **Proper HTTP Status Codes**
- Business errors: 400 Bad Request
- Process errors: 422 Unprocessable Entity
- External API errors: 502 Bad Gateway
- Not found: 404 Not Found

### 4. **Comprehensive Logging**
- All errors are logged with correlation ID
- Stack traces for debugging
- Contextual information

### 5. **Developer-Friendly**
- Clear error messages
- Helpful suggestions
- Detailed field-level validation errors

## Configuration

### Logging Configuration

All exceptions are logged with correlation ID tracking:

```properties
# Logback configuration includes correlation ID in all log messages
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [CORRELATION:%X{correlationId}] %logger{36} - %msg%n
```

### Error Response Customization

Error responses can be customized by modifying the `GlobalExceptionHandler` or creating specific exception handlers for custom scenarios.

## Testing

### Unit Testing Exceptions

```java
@Test
void shouldThrowValidationExceptionForEmptyXmlData() {
    // Given
    Map<String, String> request = Map.of("xmlData", "");
    
    // When & Then
    assertThrows(ValidationException.class, () -> {
        controller.processEntity(request, "CORR-123");
    });
}
```

### Integration Testing Error Responses

```java
@Test
void shouldReturnValidationErrorForEmptyXmlData() {
    // Given
    Map<String, String> request = Map.of("xmlData", "");
    
    // When
    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        "/api/v1/onboarding/process-entity", 
        request, 
        ErrorResponse.class
    );
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getError()).isEqualTo("VALIDATION_ERROR");
    assertThat(response.getBody().getCorrelationId()).isNotNull();
}
```

## Best Practices

### 1. **Use Specific Exceptions**
- Prefer specific exceptions over generic ones
- Include relevant context information
- Provide helpful error messages

### 2. **Include Correlation ID**
- Always include correlation ID in exceptions
- Use for request tracking and debugging

### 3. **Provide Helpful Suggestions**
- Include actionable suggestions in error responses
- Guide users toward resolution

### 4. **Log Appropriately**
- Log errors with appropriate levels
- Include correlation ID in all log messages
- Don't log sensitive information

### 5. **Test Exception Scenarios**
- Write unit tests for exception throwing
- Write integration tests for error responses
- Verify error response structure

## Monitoring and Alerting

### Error Metrics

The system tracks:
- Error rates by exception type
- Error rates by endpoint
- Error rates by correlation ID
- Response times for error scenarios

### Alerting

Set up alerts for:
- High error rates
- External API failures
- Step execution failures
- Process failures

## Future Enhancements

### 1. **Error Recovery**
- Automatic retry for transient errors
- Circuit breaker pattern for external APIs
- Fallback mechanisms

### 2. **Error Analytics**
- Error trend analysis
- Root cause analysis
- Performance impact analysis

### 3. **Enhanced Suggestions**
- AI-powered error suggestions
- Context-aware recommendations
- Automated error resolution

---

*This comprehensive exception handling system ensures robust error management with detailed, actionable error responses that help both developers and API consumers understand and resolve issues quickly.*
