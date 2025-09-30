# Banking Onboarding Service - API Bridge Architecture

## 🏗️ Architecture Overview

This banking onboarding service implements a **modern API bridge architecture** that provides a flexible, scalable, and maintainable way to integrate with external APIs (like Fenergo) without hard dependencies. The system uses **Java 8 functional programming**, **Spring Boot**, and **WebClient** for optimal performance.

## 🎯 Key Features

- **Generic API Bridge**: Call any external API without hardcoded dependencies
- **JWT Token Management**: Automatic authentication with token caching and refresh
- **Functional Processing Pipeline**: Chain of responsibility pattern with Java 8 functions
- **Correlation ID Tracking**: End-to-end request tracing
- **Modern HTTP Client**: WebClient for non-blocking, reactive operations
- **Flexible Configuration**: Per-endpoint authentication and behavior settings
- **Async Processing**: Spring `@Async` for non-blocking operations

## 📁 Project Structure

```
banking-onboarding-service/
├── src/main/java/com/banking/onboarding/
│   ├── auth/                           # JWT Authentication
│   │   ├── JwtToken.java              # Token data model
│   │   ├── TokenRequest.java          # Token request model
│   │   ├── JwtTokenService.java       # Token management service
│   │   └── JwtTokenController.java    # Token REST API
│   │
│   ├── bridge/                        # API Bridge System
│   │   ├── ApiRequest.java            # Generic API request
│   │   ├── ApiResponse.java           # Generic API response
│   │   ├── ApiEndpoint.java           # Endpoint configuration
│   │   ├── ApiEndpointRegistry.java  # Endpoint management
│   │   ├── ApiBridgeService.java      # Main bridge service
│   │   ├── GenericHttpClient.java     # WebClient implementation
│   │   └── ApiBridgeController.java   # Bridge REST API
│   │
│   ├── context/                       # Processing Context
│   │   └── ProcessingContext.java     # Generic processing context
│   │
│   ├── function/                      # Functional Processing
│   │   ├── ProcessingFunction.java    # Function interface
│   │   ├── ProcessingChain.java       # Chain of responsibility
│   │   ├── TransformFunction.java     # XML/JSON transformation
│   │   ├── ValidateFunction.java      # Data validation
│   │   ├── FenergoFunction.java       # Fenergo API integration
│   │   └── CompleteFunction.java      # Process completion
│   │
│   ├── controller/                    # REST Controllers
│   │   ├── OnboardingController.java  # Main onboarding API
│   │   └── FunctionalOnboardingController.java # Functional API
│   │
│   ├── service/                       # Business Services
│   │   ├── FunctionalOnboardingService.java # Async processing
│   │   ├── OnboardingProcessService.java    # Process management
│   │   └── CorrelationIdService.java        # Correlation tracking
│   │
│   ├── model/                         # Data Models
│   │   ├── OnboardingProcess.java     # Process entity
│   │   ├── EntityData.java            # Entity data model
│   │   └── RequestType.java           # Request type enum
│   │
│   ├── config/                        # Configuration
│   │   ├── ApiBridgeConfig.java       # Bridge configuration
│   │   ├── FunctionalAsyncConfig.java # Async configuration
│   │   └── WebConfig.java             # Web configuration
│   │
│   └── interceptor/                   # Cross-cutting Concerns
│       └── CorrelationIdInterceptor.java # Correlation ID handling
│
└── src/main/resources/
    └── application.properties         # Configuration
```

## 🚀 Quick Start

### 1. Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB (for process storage)
- External Auth Service (for JWT tokens)

### 2. Configuration

Update `application.properties`:

```properties
# Server Configuration
server.port=8080

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=banking_onboarding

# Fenergo API Configuration
fenergo.api.base-url=http://localhost:8081/fenergo/api
fenergo.api.timeout=30000
fenergo.api.retry-attempts=3

# JWT Token Service Configuration
auth.token-service.url=http://localhost:8080/auth/token
auth.token-service.client-id=banking-onboarding-service
auth.token-service.client-secret=secret
auth.token-service.default-scope=fenergo-api
auth.token-service.cache-enabled=true

# HTTP Client Configuration
http.client.type=webclient
webclient.timeout=30000
webclient.max-in-memory-size=10485760

# Process Configuration
process.async.timeout=300000
process.max-concurrent=10
```

### 3. Run the Application

```bash
cd banking-onboarding-service
mvn spring-boot:run
```

## 🔧 API Usage

### 1. Process Entity (Main Endpoint)

**Endpoint**: `POST /onboarding/process-entity/{requestType}`

**Request**:
```bash
curl -X POST http://localhost:8080/onboarding/process-entity/ADD_KYC \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: custom-correlation-id" \
  -d '{
    "entityId": "ENT-12345",
    "entityName": "John Doe",
    "entityType": "INDIVIDUAL",
    "primaryContact": {
      "email": "john.doe@example.com",
      "phone": "+1234567890"
    },
    "riskProfile": "MEDIUM",
    "complianceInfo": {
      "kycStatus": "PENDING",
      "documents": ["passport", "utility_bill"]
    }
  }'
```

**Response**:
```json
{
  "processId": "proc-12345-67890",
  "correlationId": "custom-correlation-id",
  "status": "RECEIVED",
  "message": "Entity processing started successfully",
  "estimatedCompletionTime": "5-10 minutes"
}
```

### 2. Check Process Status

**Endpoint**: `GET /onboarding/status/{processId}`

**Request**:
```bash
curl http://localhost:8080/onboarding/status/proc-12345-67890
```

**Response**:
```json
{
  "processId": "proc-12345-67890",
  "correlationId": "custom-correlation-id",
  "status": "COMPLETED",
  "progressPercentage": 100,
  "errorMessage": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:35:00",
  "completedAt": "2024-01-15T10:35:00"
}
```

### 3. Direct API Bridge Calls

**Endpoint**: `POST /api/bridge/call/{endpointName}`

**Request**:
```bash
curl -X POST http://localhost:8080/api/bridge/call/SUBMIT_KYC \
  -H "Content-Type: application/json" \
  -d '{
    "entityId": "ENT-12345",
    "kycData": {
      "documents": ["passport", "utility_bill"],
      "verificationStatus": "PENDING"
    }
  }'
```

### 4. JWT Token Management

**Get Token**:
```bash
curl http://localhost:8080/api/auth/token/fenergo-kyc-write
```

**Refresh Token**:
```bash
curl -X POST http://localhost:8080/api/auth/token/fenergo-kyc-write/refresh
```

**Clear Cache**:
```bash
curl -X DELETE http://localhost:8080/api/auth/cache
```

## 🏛️ Architecture Components

### 1. API Bridge System

The API Bridge provides a generic way to call external APIs without hard dependencies.

#### Core Components:

- **ApiRequest**: Generic request wrapper
- **ApiResponse**: Generic response wrapper
- **ApiEndpoint**: Endpoint configuration with auth settings
- **ApiEndpointRegistry**: Dynamic endpoint management
- **GenericHttpClient**: WebClient-based HTTP client
- **ApiBridgeService**: Main service interface

#### Usage Patterns:

```java
// Pattern 1: Named Endpoints
ApiResponse response = bridgeService.callApi("SUBMIT_KYC", entityData);

// Pattern 2: Direct URL Calls
ApiResponse response = bridgeService.callApiDirect(
    "https://fenergo.com/api/custom-endpoint", 
    "POST", 
    payload
);

// Pattern 3: REST API Bridge
curl -X POST http://localhost:8080/api/bridge/call/SUBMIT_KYC \
  -H "Content-Type: application/json" \
  -d '{"entityId": "123"}'
```

### 2. JWT Token Service

Automatic authentication management with intelligent caching.

#### Features:

- **Automatic Token Fetching**: Gets tokens from external auth service
- **Token Caching**: Caches tokens with expiration handling
- **Per-Scope Management**: Different tokens for different scopes
- **Automatic Refresh**: Refreshes tokens before expiry
- **Fallback Support**: Falls back to default scope if needed

#### Authentication Flow:

```
1. API call to Fenergo endpoint
2. Check if endpoint requires auth (authRequired=true)
3. Get token for endpoint's scope (authScope)
4. Check if cached token is valid
5. If not valid, fetch new token from auth service
6. Add "Authorization: Bearer <token>" header
7. Execute request to Fenergo
8. Cache token for future use
```

#### Configuration:

```properties
# Token service settings
auth.token-service.url=http://localhost:8080/auth/token
auth.token-service.client-id=banking-onboarding-service
auth.token-service.client-secret=secret
auth.token-service.default-scope=fenergo-api
auth.token-service.cache-enabled=true

# Endpoint-specific auth
endpoint.SUBMIT_KYC.authRequired=true
endpoint.SUBMIT_KYC.authScope=fenergo-kyc-write
endpoint.HEALTH_CHECK.authRequired=false
```

### 3. Functional Processing Pipeline

Chain of responsibility pattern using Java 8 functional interfaces.

#### Processing Functions:

1. **TransformFunction**: Converts XML/JSON to EntityData
2. **ValidateFunction**: Validates entity data
3. **FenergoFunction**: Calls Fenergo API using bridge
4. **CompleteFunction**: Marks process as complete

#### Processing Chain:

```java
ProcessingChain<EntityData> chain = ProcessingChain.<EntityData>builder()
    .addFunction(transformFunction)
    .addFunction(validateFunction)
    .addFunction(fenergoFunction)
    .addFunction(completeFunction)
    .build();

ProcessingContext<EntityData> result = chain.execute(context);
```

#### Async Processing:

```java
@Async("functionalTaskExecutor")
public void processEntityAsync(OnboardingProcess process) {
    // Process runs asynchronously
    ProcessingContext<EntityData> result = chain.execute(context);
    handleFinalResult(result);
}
```

### 4. Processing Context

Generic context for passing data through the processing pipeline.

#### Features:

- **Type Safety**: Generic type support
- **Correlation Tracking**: Built-in correlation ID support
- **Step Data**: Store intermediate results
- **Error Handling**: Built-in error management
- **Tracing**: Trace ID for distributed tracing

#### Usage:

```java
ProcessingContext<EntityData> context = ProcessingContext.createOnboardingContext(
    processId, correlationId, rawPayload, requestType
);

// Add data
context.addResult("transformResult", transformedData);
context.addStepData("validationResult", validationData);

// Get data
EntityData entityData = context.getProcessedData();
Map<String, Object> results = context.getResults();
```

## 🔧 Configuration Guide

### 1. Endpoint Configuration

Configure API endpoints with authentication settings:

```java
registerEndpoint(ApiEndpoint.builder()
    .name("SUBMIT_KYC")
    .method("POST")
    .path("/kyc/submit")
    .description("Submit KYC documents")
    .authRequired(true)                    // Requires authentication
    .authScope("fenergo-kyc-write")       // Specific scope
    .timeoutMs(30000)                     // 30 second timeout
    .retryAttempts(3)                     // 3 retry attempts
    .build());
```

### 2. Authentication Configuration

Configure authentication per endpoint:

```properties
# Default authentication
auth.token-service.url=http://auth-service:8080/token
auth.token-service.client-id=banking-onboarding-service
auth.token-service.client-secret=your-secret
auth.token-service.default-scope=fenergo-api

# Endpoint-specific authentication
endpoint.SUBMIT_KYC.authRequired=true
endpoint.SUBMIT_KYC.authScope=fenergo-kyc-write
endpoint.GET_KYC_STATUS.authRequired=true
endpoint.GET_KYC_STATUS.authScope=fenergo-kyc-read
endpoint.HEALTH_CHECK.authRequired=false
```

### 3. HTTP Client Configuration

Configure WebClient settings:

```properties
# WebClient configuration
webclient.timeout=30000
webclient.max-in-memory-size=10485760

# Connection settings
webclient.max-connections=100
webclient.max-connections-per-route=20
```

### 4. Async Processing Configuration

Configure async processing:

```properties
# Async processing
process.async.timeout=300000
process.max-concurrent=10
process.thread-pool.core-size=5
process.thread-pool.max-size=20
process.thread-pool.queue-capacity=100
```

## 🧪 Testing

### 1. Unit Tests

```bash
mvn test
```

### 2. Integration Tests

```bash
mvn test -Dtest=*IntegrationTest
```

### 3. Manual Testing

#### Test JWT Token Service:

```bash
# Get token
curl http://localhost:8080/api/auth/token/fenergo-kyc-write

# Test token validity
curl http://localhost:8080/api/auth/token/fenergo-kyc-write/validate
```

#### Test API Bridge:

```bash
# List available endpoints
curl http://localhost:8080/api/bridge/endpoints

# Test endpoint call
curl -X POST http://localhost:8080/api/bridge/call/HEALTH_CHECK
```

#### Test Processing Pipeline:

```bash
# Submit entity for processing
curl -X POST http://localhost:8080/onboarding/process-entity/ADD_KYC \
  -H "Content-Type: application/json" \
  -d '{"entityId": "TEST-123", "entityName": "Test Entity"}'

# Check status
curl http://localhost:8080/onboarding/status/{processId}
```

## 🚀 Deployment

### 1. Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/banking-onboarding-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 2. Docker Compose

```yaml
version: '3.8'
services:
  banking-onboarding-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_MONGODB_HOST=mongodb
      - AUTH_TOKEN_SERVICE_URL=http://auth-service:8080/token
    depends_on:
      - mongodb
      - auth-service

  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"

  auth-service:
    image: auth-service:latest
    ports:
      - "8081:8080"
```

### 3. Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: banking-onboarding-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: banking-onboarding-service
  template:
    metadata:
      labels:
        app: banking-onboarding-service
    spec:
      containers:
      - name: banking-onboarding-service
        image: banking-onboarding-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATA_MONGODB_HOST
          value: "mongodb-service"
        - name: AUTH_TOKEN_SERVICE_URL
          value: "http://auth-service:8080/token"
```

## 🔍 Monitoring & Observability

### 1. Health Check

```bash
curl http://localhost:8080/onboarding/health
```

### 2. Correlation ID Tracking

All requests include correlation IDs for end-to-end tracing:

```bash
curl -X POST http://localhost:8080/onboarding/process-entity/ADD_KYC \
  -H "X-Correlation-ID: trace-12345" \
  -d '{"entityId": "ENT-123"}'
```

### 3. Logging

Structured logging with correlation IDs:

```properties
logging.level.com.banking.onboarding=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] %msg%n
```

### 4. Metrics

Built-in metrics for:
- Process completion rates
- API response times
- Token cache hit rates
- Error rates

## 🔧 Troubleshooting

### Common Issues:

1. **Token Service Unavailable**
   - Check auth service connectivity
   - Verify credentials in configuration
   - Check token service URL

2. **Fenergo API Errors**
   - Verify Fenergo API URL
   - Check authentication tokens
   - Review API endpoint configuration

3. **Processing Failures**
   - Check MongoDB connectivity
   - Review processing logs
   - Verify entity data format

4. **Performance Issues**
   - Adjust thread pool settings
   - Review timeout configurations
   - Monitor memory usage

### Debug Commands:

```bash
# Check application health
curl http://localhost:8080/onboarding/health

# List available endpoints
curl http://localhost:8080/api/bridge/endpoints

# Test token service
curl http://localhost:8080/api/auth/token/fenergo-api/validate

# Clear token cache
curl -X DELETE http://localhost:8080/api/auth/cache
```

## 📚 API Reference

### Onboarding API

- `POST /onboarding/process-entity/{requestType}` - Process entity
- `GET /onboarding/status/{processId}` - Get process status
- `GET /onboarding/health` - Health check

### API Bridge

- `POST /api/bridge/call/{endpointName}` - Call named endpoint
- `POST /api/bridge/call-direct` - Call direct URL
- `GET /api/bridge/endpoints` - List endpoints
- `POST /api/bridge/endpoints` - Register endpoint

### JWT Token Service

- `GET /api/auth/token` - Get default token
- `GET /api/auth/token/{scope}` - Get scope-specific token
- `POST /api/auth/token/{scope}/refresh` - Refresh token
- `GET /api/auth/token/{scope}/validate` - Validate token
- `DELETE /api/auth/cache` - Clear cache

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🎯 Summary

This banking onboarding service provides a **modern, scalable, and maintainable** architecture for integrating with external APIs. Key benefits:

- **Zero Hard Dependencies**: No tight coupling to external API structures
- **Automatic Authentication**: JWT token management with caching
- **Flexible Configuration**: Per-endpoint behavior settings
- **Modern HTTP Client**: WebClient for optimal performance
- **Functional Processing**: Clean, testable processing pipeline
- **Comprehensive Monitoring**: Correlation ID tracking and health checks

The system is production-ready and can handle high-volume processing with automatic scaling and fault tolerance.