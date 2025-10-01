# Fenergo API Proxy Integration Documentation

## Overview

The Banking Onboarding Service now integrates with actual Fenergo APIs through a proxy service. This document explains how the proxy calls work and how to configure them.

## Architecture

```
Client Request → Banking Onboarding Service → Proxy Service → Fenergo/Apigee APIs
```

The proxy service acts as an intermediary that:
1. Handles authentication (JWT tokens)
2. Routes requests to the correct Fenergo endpoints
3. Manages headers and request/response transformation
4. Provides error handling and logging

## Service Integration

### 1. ApigeeService
**Purpose**: Transforms XML payloads to JSON using Apigee API

**Proxy Call**:
```java
ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
    apigeeProxyUrl,                    // Proxy URL
    apigeeEndpointUrl,                 // Full Apigee endpoint URL
    HttpMethod.POST,                   // HTTP method
    xmlPayload,                        // XML payload
    "JWT",                            // Auth type
    "apigee.transform"                // Auth scope
);
```

**Configuration**:
```properties
apigee.proxy.url=http://localhost:8080/api/v1/proxy
apigee.endpoint.url=https://apigee.example.com/transform
```

### 2. FenergoEntityService
**Purpose**: Creates entities in Fenergo system

**Proxy Call**:
```java
ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
    fenergoProxyUrl,                   // Proxy URL
    fenergoEntityCreateUrl,            // Full Fenergo entity create URL
    HttpMethod.POST,                   // HTTP method
    jsonPayload,                       // JSON payload
    "JWT",                            // Auth type
    "fenergo.entity.create"           // Auth scope
);
```

**Configuration**:
```properties
fenergo.proxy.url=http://localhost:8080/api/v1/proxy
fenergo.entity.create.url=https://fenergo.example.com/api/v1/entities
```

### 3. FenergoJourneyService
**Purpose**: Manages Fenergo journey lifecycle

**Three main operations**:

#### a) Get Journey Info
```java
ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
    fenergoProxyUrl,
    fenergoJourneyInfoUrl,
    HttpMethod.POST,
    payload,
    "JWT",
    "fenergo.journey.info"
);
```

#### b) Initiate Journey
```java
ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
    fenergoProxyUrl,
    fenergoJourneyInitiateUrl,
    HttpMethod.POST,
    payload,
    "JWT",
    "fenergo.journey.initiate"
);
```

#### c) Get Journey Details
```java
ProxyResponse proxyResponse = fenergoProxyService.callFenergoApi(
    fenergoProxyUrl,
    fenergoJourneyDetailsUrl,
    HttpMethod.POST,
    payload,
    "JWT",
    "fenergo.journey.details"
);
```

**Configuration**:
```properties
fenergo.journey.info.url=https://fenergo.example.com/api/v1/journeys/info
fenergo.journey.initiate.url=https://fenergo.example.com/api/v1/journeys/initiate
fenergo.journey.details.url=https://fenergo.example.com/api/v1/journeys/details
```

## Proxy Service Details

### FenergoProxyService
The proxy service handles:

1. **Authentication**: Automatically adds JWT tokens based on auth type and scope
2. **Header Management**: Adds required headers including the full Fenergo endpoint URL
3. **Request Routing**: Routes requests to the correct external API
4. **Response Handling**: Returns standardized response format
5. **Error Handling**: Comprehensive error handling with detailed logging

### ProxyResponse
Standardized response format:
```java
public class ProxyResponse {
    private boolean success;           // Request success status
    private int statusCode;            // HTTP status code
    private String responseBody;      // Response body
    private String fenergoEndpoint;   // Full endpoint URL called
    private String errorMessage;       // Error message if failed
    private long responseTimeMs;       // Response time in milliseconds
}
```

## Configuration

### Environment Variables
All URLs can be configured via environment variables:

```bash
# Proxy URLs
export FENERGO_PROXY_URL="http://your-proxy-server:8080/api/v1/proxy"
export APIGEE_PROXY_URL="http://your-proxy-server:8080/api/v1/proxy"

# Fenergo API URLs
export FENERGO_ENTITY_CREATE_URL="https://your-fenergo-instance.com/api/v1/entities"
export FENERGO_JOURNEY_INFO_URL="https://your-fenergo-instance.com/api/v1/journeys/info"
export FENERGO_JOURNEY_INITIATE_URL="https://your-fenergo-instance.com/api/v1/journeys/initiate"
export FENERGO_JOURNEY_DETAILS_URL="https://your-fenergo-instance.com/api/v1/journeys/details"

# Apigee API URLs
export APIGEE_ENDPOINT_URL="https://your-apigee-instance.com/transform"
```

### Application Properties
Default values are defined in `application.properties`:

```properties
# Proxy Configuration
fenergo.proxy.url=${FENERGO_PROXY_URL:http://localhost:8080/api/v1/proxy}
apigee.proxy.url=${APIGEE_PROXY_URL:http://localhost:8080/api/v1/proxy}

# Fenergo API Endpoints
fenergo.entity.create.url=${FENERGO_ENTITY_CREATE_URL:https://fenergo.example.com/api/v1/entities}
fenergo.journey.info.url=${FENERGO_JOURNEY_INFO_URL:https://fenergo.example.com/api/v1/journeys/info}
fenergo.journey.initiate.url=${FENERGO_JOURNEY_INITIATE_URL:https://fenergo.example.com/api/v1/journeys/initiate}
fenergo.journey.details.url=${FENERGO_JOURNEY_DETAILS_URL:https://fenergo.example.com/api/v1/journeys/details}

# Apigee API Endpoints
apigee.endpoint.url=${APIGEE_ENDPOINT_URL:https://apigee.example.com/transform}
```

## Error Handling

### Service-Level Error Handling
Each service implements comprehensive error handling:

1. **Timeout Handling**: Configurable timeouts for each API call
2. **Retry Logic**: Automatic retry with exponential backoff
3. **Error Logging**: Detailed logging with correlation IDs
4. **Graceful Degradation**: Returns meaningful error responses

### Error Response Format
```java
{
    "success": false,
    "errorMessage": "Detailed error message",
    "duration": 1500,
    "metadata": {
        "processId": "process-123",
        "statusCode": 500,
        "fenergoEndpoint": "https://fenergo.example.com/api/v1/entities",
        "error": "ConnectionTimeoutException"
    }
}
```

## Logging

### Correlation ID Tracking
All proxy calls include correlation ID tracking:

```
[CORRELATION:process-123] Calling Fenergo Entity Create API via proxy
[CORRELATION:process-123] Fenergo entity creation successful - Duration: 2000ms
```

### Log Levels
- **INFO**: Successful API calls with timing information
- **ERROR**: Failed API calls with detailed error information
- **DEBUG**: Request/response details (configurable)

## Testing

### Unit Tests
Mock the `FenergoProxyService` for unit testing:

```java
@MockBean
private FenergoProxyService fenergoProxyService;

@Test
void testEntityCreation() {
    // Mock successful response
    when(fenergoProxyService.callFenergoApi(any(), any(), any(), any(), any(), any()))
        .thenReturn(ProxyResponse.success("{\"entityId\":\"123\"}", "https://fenergo.com/api/entities"));
    
    // Test service method
    FenergoEntityResponse response = fenergoEntityService.createEntity(payload, processId);
    
    assertTrue(response.isSuccess());
    assertEquals("123", response.getEntityId());
}
```

### Integration Tests
Use TestContainers or wire mock for integration testing:

```java
@Test
void testEndToEndFlow() {
    // Start wire mock server
    stubFor(post(urlEqualTo("/api/v1/entities"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"entityId\":\"123\",\"clientId\":\"456\"}")));
    
    // Test complete flow
    // ...
}
```

## Monitoring and Metrics

### Response Time Monitoring
Each service tracks response times:

```java
long startTime = System.currentTimeMillis();
// ... API call ...
long duration = System.currentTimeMillis() - startTime;
```

### Health Checks
Monitor proxy service health:

```java
@GetMapping("/health/proxy")
public ResponseEntity<Map<String, Object>> checkProxyHealth() {
    // Check proxy service connectivity
    // Return health status
}
```

## Security Considerations

### JWT Token Management
- Tokens are automatically refreshed when expired
- Different scopes for different API endpoints
- Secure token storage and transmission

### Network Security
- HTTPS for all external API calls
- Certificate validation
- Network timeouts and retry limits

## Troubleshooting

### Common Issues

1. **Connection Timeouts**
   - Check network connectivity
   - Verify proxy service is running
   - Increase timeout values if needed

2. **Authentication Failures**
   - Verify JWT token service is accessible
   - Check client credentials
   - Validate token scopes

3. **API Endpoint Errors**
   - Verify endpoint URLs are correct
   - Check API documentation for changes
   - Validate request payload format

### Debug Mode
Enable debug logging for detailed troubleshooting:

```properties
logging.level.com.banking.onboarding.proxy=DEBUG
logging.level.com.banking.onboarding.service=DEBUG
```

## Future Enhancements

1. **Circuit Breaker Pattern**: Implement circuit breaker for external API calls
2. **Rate Limiting**: Add rate limiting for API calls
3. **Caching**: Implement response caching for frequently accessed data
4. **Metrics Collection**: Add Prometheus metrics for monitoring
5. **Distributed Tracing**: Implement distributed tracing with OpenTelemetry

## Conclusion

The proxy integration provides a robust, scalable, and maintainable way to integrate with external Fenergo and Apigee APIs. The standardized approach ensures consistent error handling, logging, and monitoring across all external API calls.
