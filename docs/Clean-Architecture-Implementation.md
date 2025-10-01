# Clean Architecture Implementation - Complete Guide

## ✅ **CLEAN ARCHITECTURE IMPLEMENTED**

### **Architecture Overview**

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
├─────────────────────────────────────────────────────────────┤
│  Controllers (CleanApiController)                          │
│  DTOs (ApiRequestDto, TransformationRequestDto, etc.)      │
│  Exception Handling (GlobalExceptionHandler)               │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                       │
├─────────────────────────────────────────────────────────────┤
│  Services (ApiService, TransformationService, etc.)        │
│  Orchestration (WorkflowOrchestrationService)              │
│  Factory (ApiCallStrategyFactory)                         │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                           │
├─────────────────────────────────────────────────────────────┤
│  Domain Models (ApiRequest, ApiResponse, AuthConfig)       │
│  Value Objects (ApiType, HttpMethod)                        │
│  Business Logic                                             │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                      │
├─────────────────────────────────────────────────────────────┤
│  Strategy Implementations (DirectApiCallStrategy, etc.)     │
│  External Services (ApigeeTokenService, FenergoTokenService)│
│  HTTP Clients (RestClient)                                 │
└─────────────────────────────────────────────────────────────┘
```

## **Design Patterns Implemented**

### **1. Strategy Pattern**
- **Interface**: `ApiCallStrategy`
- **Implementations**: 
  - `DirectApiCallStrategy` - No authentication
  - `InternalApiCallStrategy` - Apigee token authentication
  - `ExternalApiCallStrategy` - Fenergo proxy authentication
- **Benefits**: Easy to add new API call types, clean separation of concerns

### **2. Factory Pattern**
- **Factory**: `ApiCallStrategyFactory`
- **Purpose**: Automatically selects the appropriate strategy based on API request
- **Benefits**: Centralized strategy selection, easy to extend

### **3. Builder Pattern**
- **Usage**: All domain models use `@Builder` for clean object construction
- **Benefits**: Immutable objects, fluent API, optional parameters

### **4. Facade Pattern**
- **Services**: `ApiService`, `TransformationService`, `FenergoService`
- **Purpose**: Provide simple interfaces to complex subsystems
- **Benefits**: Simplified client code, better abstraction

## **Clean Architecture Principles**

### **✅ Dependency Inversion**
- High-level modules don't depend on low-level modules
- Both depend on abstractions (interfaces)
- Strategy pattern ensures proper abstraction

### **✅ Single Responsibility Principle**
- Each class has one reason to change
- Controllers handle HTTP, Services handle business logic
- Strategies handle specific API call types

### **✅ Open/Closed Principle**
- Open for extension (new strategies), closed for modification
- Easy to add new API call types without changing existing code

### **✅ Interface Segregation**
- Small, focused interfaces
- `ApiCallStrategy` interface is minimal and focused

### **✅ Dependency Injection**
- All dependencies injected via constructor
- Easy to test and mock

## **API Endpoints**

### **Direct API Calls**
```bash
POST /api/v1/direct
{
  "endpoint": "https://api.example.com/data",
  "method": "POST",
  "payload": {...},
  "headers": {...}
}
```

### **Internal API Calls (Apigee)**
```bash
POST /api/v1/internal
{
  "endpoint": "https://internal-api.com/users",
  "method": "POST",
  "payload": {...},
  "authScope": "user-management"
}
```

### **External API Calls (Fenergo)**
```bash
POST /api/v1/external/fenergo
{
  "endpoint": "https://fenergo.com/api/v1/entities",
  "method": "POST",
  "payload": {...},
  "authScope": "fenergo-entity"
}
```

### **Transformation Services**
```bash
POST /api/v1/transform/xml-to-json
{
  "inputData": "<xml>...</xml>",
  "inputFormat": "XML",
  "outputFormat": "JSON"
}

POST /api/v1/transform/json-to-xml
{
  "inputData": "{\"key\": \"value\"}",
  "inputFormat": "JSON",
  "outputFormat": "XML"
}

POST /api/v1/transform/custom
{
  "inputData": "...",
  "inputFormat": "CSV",
  "outputFormat": "JSON"
}
```

### **Fenergo Services**
```bash
POST /api/v1/fenergo/entity/create
{
  "entityData": {...}
}

POST /api/v1/fenergo/journey/info
{
  "journeyData": {...}
}
```

### **Workflow Orchestration**
```bash
POST /api/v1/workflow/onboarding
{
  "inputData": "<xml>...</xml>",
  "workflowType": "onboarding"
}

POST /api/v1/workflow/fenergo
{
  "initialData": {...}
}
```

## **Configuration Properties**

```properties
# Fenergo Configuration
fenergo.proxy.url=http://fenergo-proxy.com/api/v1/proxy
fenergo.entity.create.endpoint=https://fenergo.com/api/v1/entities
fenergo.entity.create.auth-scope=fenergo-entity-create
fenergo.journey.info.endpoint=https://fenergo.com/api/v1/journeys/info
fenergo.journey.info.auth-scope=fenergo-journey-info
fenergo.journey.initiate.endpoint=https://fenergo.com/api/v1/journeys/initiate
fenergo.journey.initiate.auth-scope=fenergo-journey-initiate

# Apigee Configuration
apigee.transformation.endpoint=https://apigee-transformation-service.com/api/v1/transform
apigee.transformation.auth-scope=transformation-api
apigee.auth.token-service.url=http://apigee-token-service:8080/oauth/token
apigee.auth.client-id=apigee-client
apigee.auth.client-secret=apigee-secret

# Fenergo Token Configuration
fenergo.auth.token-service.url=http://fenergo-token-service:8080/oauth/token
fenergo.auth.client-id=fenergo-client
fenergo.auth.client-secret=fenergo-secret
```

## **Error Handling**

### **Validation Errors**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "status": "BAD_REQUEST",
  "correlationId": "correlation-id",
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/direct",
  "details": {
    "endpoint": "Endpoint is required",
    "method": "Method is required"
  }
}
```

### **Runtime Errors**
```json
{
  "error": "RUNTIME_ERROR",
  "message": "An unexpected error occurred",
  "status": "INTERNAL_SERVER_ERROR",
  "correlationId": "correlation-id",
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/internal"
}
```

## **Benefits of Clean Architecture**

### **✅ Maintainability**
- Clear separation of concerns
- Easy to understand and modify
- Well-organized code structure

### **✅ Testability**
- Easy to unit test with dependency injection
- Mock external dependencies
- Isolated business logic

### **✅ Extensibility**
- Easy to add new API call types
- Strategy pattern allows new implementations
- Factory pattern handles strategy selection

### **✅ Reliability**
- Comprehensive error handling
- Input validation
- Proper logging with correlation IDs

### **✅ Performance**
- Async processing with CompletableFuture
- Efficient strategy selection
- Minimal overhead

## **Usage Examples**

### **Service Layer Usage**
```java
@Autowired
private ApiService apiService;

// Direct API call
CompletableFuture<ApiResponse> response = apiService.callDirectApi(
    "https://api.example.com/data",
    ApiRequest.HttpMethod.POST,
    payload,
    headers,
    correlationId
);

// Internal API call
CompletableFuture<ApiResponse> response = apiService.callInternalApi(
    "https://internal-api.com/users",
    ApiRequest.HttpMethod.POST,
    userData,
    "user-management",
    headers,
    correlationId
);

// External API call
CompletableFuture<ApiResponse> response = apiService.callExternalApi(
    "https://fenergo.com/api/v1/entities",
    ApiRequest.HttpMethod.POST,
    entityData,
    "fenergo-entity",
    proxyUrl,
    headers,
    correlationId
);
```

### **Workflow Usage**
```java
@Autowired
private WorkflowOrchestrationService workflowService;

// Complete onboarding workflow
CompletableFuture<ApiResponse> response = workflowService
    .processOnboardingWorkflow(xmlData, correlationId);

// Multi-step Fenergo workflow
CompletableFuture<ApiResponse> response = workflowService
    .processFenergoWorkflow(initialData, correlationId);
```

## **Testing Strategy**

### **Unit Tests**
- Test each strategy independently
- Mock external dependencies
- Test business logic in isolation

### **Integration Tests**
- Test complete workflows
- Test API endpoints
- Test error scenarios

### **Contract Tests**
- Test API contracts
- Validate request/response formats
- Test error responses

## **Monitoring and Observability**

### **Logging**
- Structured logging with correlation IDs
- Request/response logging
- Error logging with stack traces

### **Metrics**
- API call success/failure rates
- Response times
- Error rates by strategy

### **Tracing**
- Correlation ID tracking across services
- Request flow tracing
- Performance monitoring

This clean architecture implementation provides a **robust, maintainable, and extensible** foundation for API integration with proper separation of concerns, design patterns, and comprehensive error handling! 🎉
