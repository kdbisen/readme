# Comprehensive Logging System Documentation

## Overview

The banking onboarding service includes a comprehensive logging system that provides:

1. **Request/Response Logging**: Automatic logging of all inbound requests and outbound responses
2. **Error Event Storage**: Database storage of all errors and exceptions with correlation IDs
3. **Business Event Logging**: Structured logging of business operations
4. **Performance Metrics**: Logging of operation performance and metrics
5. **External API Call Tracking**: Complete tracking of external service calls
6. **Correlation ID Tracking**: End-to-end request tracing

## Architecture

### Components

1. **LoggingEventService**: Core service for logging various types of events
2. **RequestResponseLoggingFilter**: Servlet filter for automatic request/response logging
3. **ErrorEventService**: Service for storing error events in the database
4. **GlobalExceptionHandler**: Exception handler that logs errors using the error event service
5. **ErrorEvent Model**: MongoDB document model for error events
6. **ErrorEventRepository**: Repository for error event operations

### Data Flow

```
Request → RequestResponseLoggingFilter → LoggingEventService → Logs
    ↓
Exception → GlobalExceptionHandler → ErrorEventService → MongoDB
```

## Features

### 1. Request/Response Logging

**Automatic Logging**: All HTTP requests and responses are automatically logged with:
- Method and URI
- Headers (sanitized)
- Request/Response body (sanitized)
- Correlation ID and Trace ID
- Duration
- Timestamp

**Filter Configuration**:
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter implements Filter
```

### 2. Error Event Storage

**Database Storage**: All errors and exceptions are stored in MongoDB with:
- Error type and message
- Correlation ID and Trace ID
- Stack trace
- Context data
- Service and method information
- Timestamp and environment

**Error Types**:
- `APPLICATION_ERROR`: General application errors
- `VALIDATION_ERROR`: Input validation errors
- `EXTERNAL_SERVICE_ERROR`: External API errors
- `DATABASE_ERROR`: Database operation errors
- `AUTHENTICATION_ERROR`: Authentication failures
- `AUTHORIZATION_ERROR`: Authorization failures
- `TIMEOUT_ERROR`: Operation timeout errors
- `CONFIGURATION_ERROR`: Configuration issues
- `BUSINESS_LOGIC_ERROR`: Business rule violations
- `SYSTEM_ERROR`: System-level errors

### 3. Business Event Logging

**Structured Logging**: Business operations are logged with:
- Event type and name
- Event data
- Correlation ID and Trace ID
- Timestamp

**Usage**:
```java
loggingEventService.logBusinessEvent(
    "BUSINESS_OPERATION",
    "CUSTOMER_ONBOARDING_STARTED",
    eventData,
    correlationId,
    traceId
);
```

### 4. Performance Metrics

**Performance Tracking**: Operation performance is logged with:
- Operation name
- Duration in milliseconds
- Performance metrics
- Correlation ID and Trace ID

**Usage**:
```java
loggingEventService.logPerformanceMetrics(
    "DATABASE_QUERY",
    durationMs,
    metrics,
    correlationId,
    traceId
);
```

### 5. External API Call Tracking

**Complete API Tracking**: External service calls are logged with:
- Method and URL
- Headers and body
- Response status and body
- Duration
- Correlation ID and Trace ID

**Usage**:
```java
// Log API call
loggingEventService.logExternalApiCall(method, url, headers, body, correlationId, traceId);

// Log API response
loggingEventService.logExternalApiResponse(method, url, statusCode, headers, body, correlationId, traceId, duration);
```

## Configuration

### Application Properties

```properties
# Logging Configuration
logging.level.com.banking.onboarding=INFO
logging.level.com.banking.onboarding.logging=DEBUG

# Environment variables
ENVIRONMENT=${ENVIRONMENT:dev}
APP_VERSION=${APP_VERSION:1.0.0}

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=banking_onboarding
```

### Logback Configuration

The system uses structured JSON logging with Logstash encoder for easy integration with ELK stack.

## Database Schema

### ErrorEvent Collection

```json
{
  "_id": "ObjectId",
  "errorType": "APPLICATION_ERROR",
  "errorMessage": "Error description",
  "correlationId": "CORR-12345678",
  "traceId": "TRACE-ABCDEFGH",
  "serviceName": "banking-onboarding-service",
  "methodName": "processOnboarding",
  "exceptionType": "RuntimeException",
  "stackTrace": "Full stack trace...",
  "contextData": {
    "additional": "context information"
  },
  "timestamp": "2025-01-01T10:00:00",
  "environment": "dev",
  "version": "1.0.0",
  "severity": "ERROR",
  "resolved": false,
  "resolvedAt": null,
  "resolvedBy": null,
  "resolutionNotes": null,
  "version": 1
}
```

## API Endpoints

### Logging Demo Endpoints

- `POST /api/v1/logging-demo/business-event`: Demonstrate business event logging
- `POST /api/v1/logging-demo/performance-metrics`: Demonstrate performance metrics logging
- `POST /api/v1/logging-demo/external-api-call`: Demonstrate external API call logging
- `POST /api/v1/logging-demo/error-logging`: Demonstrate error logging
- `POST /api/v1/logging-demo/timeout-error`: Demonstrate timeout error logging
- `POST /api/v1/logging-demo/external-service-error`: Demonstrate external service error logging
- `GET /api/v1/logging-demo/stats`: Get logging statistics

## Usage Examples

### 1. Logging Business Events

```java
@Autowired
private LoggingEventService loggingEventService;

public void processCustomerOnboarding(Customer customer) {
    String correlationId = correlationIdService.getCurrentCorrelationId();
    String traceId = generateTraceId();
    
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("customerId", customer.getId());
    eventData.put("onboardingType", "NEW_CUSTOMER");
    
    loggingEventService.logBusinessEvent(
        "CUSTOMER_ONBOARDING",
        "ONBOARDING_STARTED",
        eventData,
        correlationId,
        traceId
    );
}
```

### 2. Logging Errors

```java
@Autowired
private ErrorEventService errorEventService;

public void handleExternalServiceError(String serviceName, String endpoint, Exception e) {
    String correlationId = correlationIdService.getCurrentCorrelationId();
    String traceId = generateTraceId();
    
    errorEventService.logExternalServiceError(
        serviceName,
        endpoint,
        500,
        "External service error: " + e.getMessage(),
        correlationId,
        traceId,
        e
    );
}
```

### 3. Logging Performance Metrics

```java
@Autowired
private LoggingEventService loggingEventService;

public void logDatabaseOperation(String operation, long durationMs) {
    String correlationId = correlationIdService.getCurrentCorrelationId();
    String traceId = generateTraceId();
    
    Map<String, Object> metrics = new HashMap<>();
    metrics.put("operation", operation);
    metrics.put("durationMs", durationMs);
    metrics.put("timestamp", System.currentTimeMillis());
    
    loggingEventService.logPerformanceMetrics(
        operation,
        durationMs,
        metrics,
        correlationId,
        traceId
    );
}
```

## Security Considerations

### Data Sanitization

The logging system automatically sanitizes sensitive data:

1. **Headers**: Authorization, password, secret, and token headers are redacted
2. **Request/Response Bodies**: Sensitive fields like passwords, SSNs, credit card numbers are redacted
3. **Context Data**: Sensitive information in context data is filtered

### Sanitization Examples

```java
// Headers
"Authorization: Bearer token123" → "Authorization: ***REDACTED***"

// JSON Body
{
  "username": "john.doe",
  "password": "secret123",
  "email": "john@example.com"
}
→
{
  "username": "john.doe", 
  "password": "***REDACTED***",
  "email": "john@example.com"
}
```

## Monitoring and Alerting

### Log Analysis

The structured JSON logs can be easily analyzed using:
- **ELK Stack**: Elasticsearch, Logstash, Kibana
- **Splunk**: Enterprise log analysis
- **CloudWatch**: AWS cloud monitoring
- **Grafana**: Visualization and alerting

### Key Metrics to Monitor

1. **Error Rates**: Track error frequency by type and service
2. **Response Times**: Monitor API response times
3. **External Service Health**: Track external API call success rates
4. **Business Event Flow**: Monitor business process completion rates

### Alerting Rules

```yaml
# Example alerting rules
alerts:
  - name: "High Error Rate"
    condition: "error_count > 100 in 5 minutes"
    severity: "critical"
    
  - name: "Slow Response Time"
    condition: "avg_response_time > 5000ms in 5 minutes"
    severity: "warning"
    
  - name: "External Service Down"
    condition: "external_api_error_rate > 50% in 2 minutes"
    severity: "critical"
```

## Best Practices

### 1. Correlation ID Usage

- Always use correlation IDs for request tracing
- Pass correlation IDs to external services
- Include correlation IDs in error reports

### 2. Error Logging

- Log errors immediately when they occur
- Include sufficient context for debugging
- Use appropriate error severity levels

### 3. Performance Logging

- Log performance metrics for critical operations
- Include relevant business context
- Monitor performance trends over time

### 4. Security

- Never log sensitive data
- Use data sanitization features
- Regularly audit logged data

## Troubleshooting

### Common Issues

1. **Missing Correlation IDs**: Ensure correlation ID service is properly configured
2. **Log Volume**: Adjust log levels to control log volume
3. **Database Performance**: Monitor MongoDB performance for error event storage
4. **Memory Usage**: Monitor memory usage for request/response body logging

### Debugging

1. **Enable Debug Logging**: Set `logging.level.com.banking.onboarding.logging=DEBUG`
2. **Check MongoDB**: Verify error events are being stored
3. **Monitor Logs**: Use log analysis tools to identify patterns
4. **Test Endpoints**: Use demo endpoints to verify logging functionality

## Future Enhancements

1. **Real-time Dashboards**: Create real-time monitoring dashboards
2. **Machine Learning**: Implement anomaly detection for error patterns
3. **Automated Alerting**: Set up automated alerting based on log patterns
4. **Performance Optimization**: Optimize logging performance for high-volume scenarios
5. **Data Retention**: Implement automated data retention policies