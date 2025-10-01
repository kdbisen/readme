# Unified API Facade - Complete Implementation

## ✅ **COMPREHENSIVE API FACADE IMPLEMENTED**

### **Three Types of API Calls Supported:**

1. **🔓 Direct API Calls** - No authentication required
2. **🏢 Internal API Calls** - Via Apigee with token authentication  
3. **🌐 External API Calls** - Via Fenergo proxy with token authentication

## **Architecture Overview**

```
UnifiedApiFacade
├── Direct API Calls (No Token)
│   ├── callDirectApi()
│   └── Uses RestClient directly
├── Internal API Calls (Apigee + Token)
│   ├── callInternalApiViaApigee()
│   ├── callTransformationApi()
│   └── Uses GenericBridgeService + ApigeeTokenService
└── External API Calls (Fenergo Proxy + Token)
    ├── callExternalApiViaFenergoProxy()
    ├── callFenergoEntityCreate()
    ├── callFenergoJourneyInfo()
    └── Uses GenericBridgeService + FenergoTokenService
```

## **API Endpoints Available**

### **1. Direct API Calls (No Token)**
```bash
# Direct API call without authentication
POST /api/facade/direct/{endpoint}?method=POST
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "payload": "any data"
}
```

### **2. Internal API Calls (Apigee + Token)**
```bash
# Internal API call via Apigee
POST /api/facade/internal/{endpoint}?method=POST&authScope=apigee-api
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "payload": "any data"
}

# XML to JSON transformation
POST /api/facade/transform/xml-to-json
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "xmlData": "<xml>...</xml>"
}

# JSON to XML transformation
POST /api/facade/transform/json-to-xml
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "jsonData": "{\"key\": \"value\"}"
}
```

### **3. External API Calls (Fenergo Proxy + Token)**
```bash
# External API call via Fenergo proxy
POST /api/facade/external/fenergo?actualEndpoint=https://fenergo.com/api/v1/entities&method=POST&authScope=fenergo-api
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "payload": "any data"
}

# Fenergo Entity Create
POST /api/facade/fenergo/entity/create
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "entityData": {...}
}

# Fenergo Journey Info
POST /api/facade/fenergo/journey/info
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "journeyData": {...}
}
```

### **4. Chained Operations**
```bash
# Complete onboarding flow: XML -> JSON (Apigee) -> Fenergo Entity Create (Proxy)
POST /api/facade/onboarding/complete
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "xmlData": "<xml>...</xml>"
}

# Multi-step Fenergo flow: Entity Create -> Journey Info -> Journey Initiate
POST /api/facade/fenergo/multi-step
Content-Type: application/json
X-Correlation-ID: {correlation-id}

{
  "initialData": {...}
}
```

## **Usage Examples**

### **1. Direct API Call (No Authentication)**
```java
@Autowired
private UnifiedApiFacade unifiedApiFacade;

CompletableFuture<ApiFacadeResponse> response = unifiedApiFacade
    .callDirectApi(
        "https://external-api.com/data",
        "POST",
        payload,
        Map.of("Custom-Header", "value"),
        correlationId
    );
```

### **2. Internal API Call via Apigee (With Token)**
```java
CompletableFuture<ApiFacadeResponse> response = unifiedApiFacade
    .callInternalApiViaApigee(
        "https://internal-api.com/users",
        "POST",
        userData,
        "user-management-scope",
        Map.of("X-Service-Type", "user-management"),
        correlationId
    );
```

### **3. External API Call via Fenergo Proxy (With Token)**
```java
CompletableFuture<ApiFacadeResponse> response = unifiedApiFacade
    .callExternalApiViaFenergoProxy(
        "https://fenergo.com/api/v1/entities",
        "POST",
        entityData,
        "fenergo-entity-scope",
        Map.of("X-Service-Type", "entity-create"),
        correlationId
    );
```

### **4. Transformation API Call**
```java
CompletableFuture<ApiFacadeResponse> response = unifiedApiFacade
    .callTransformationApi(
        xmlData,
        "XML",
        "JSON",
        correlationId
    );
```

### **5. Complete Onboarding Flow**
```java
CompletableFuture<ApiFacadeResponse> response = unifiedApiFacade
    .processCompleteOnboardingFlow(xmlData, correlationId);
```

## **Response Format**

All API calls return a standardized `ApiFacadeResponse`:

```json
{
  "success": true,
  "statusCode": 200,
  "statusText": "OK",
  "body": "response data",
  "headers": {...},
  "errorMessage": null,
  "correlationId": "correlation-id",
  "timestamp": "2024-01-01T10:00:00",
  "responseTimeMs": 1500,
  "apiType": "INTERNAL_APIGEE",
  "endpoint": "https://api.example.com/endpoint"
}
```

## **API Types**

- **`DIRECT`** - Direct API call without authentication
- **`INTERNAL_APIGEE`** - Internal API call via Apigee with token
- **`EXTERNAL_FENERGO_PROXY`** - External API call via Fenergo proxy with token

## **Configuration Properties**

```properties
# Fenergo Proxy Configuration
fenergo.proxy.url=http://fenergo-proxy.com/api/v1/proxy

# Apigee Token Service
apigee.auth.token-service.url=http://apigee-token-service:8080/oauth/token
apigee.auth.client-id=apigee-client
apigee.auth.client-secret=apigee-secret
apigee.auth.default-scope=apigee-api

# Fenergo Token Service
fenergo.auth.token-service.url=http://fenergo-token-service:8080/oauth/token
fenergo.auth.client-id=fenergo-client
fenergo.auth.client-secret=fenergo-secret
fenergo.auth.default-scope=fenergo-api
```

## **Benefits**

1. **✅ Unified Interface** - Single facade for all API call types
2. **✅ Async Processing** - All calls are asynchronous with CompletableFuture
3. **✅ Token Management** - Automatic token handling for authenticated calls
4. **✅ Error Handling** - Comprehensive error handling and logging
5. **✅ Correlation Tracking** - Full correlation ID tracking across all operations
6. **✅ Chained Operations** - Support for multi-step workflows
7. **✅ Type Safety** - Strongly typed responses and API types
8. **✅ Flexibility** - Support for custom headers and parameters

## **Service Status**

```bash
GET /api/facade/status
```

Returns:
```json
{
  "service": "Unified API Facade",
  "status": "active",
  "async": true,
  "apiTypes": ["DIRECT", "INTERNAL_APIGEE", "EXTERNAL_FENERGO_PROXY"],
  "features": [
    "Direct API calls (no auth)",
    "Internal API calls via Apigee (with token)",
    "External API calls via Fenergo proxy (with token)",
    "Chained operations",
    "Transformation services"
  ],
  "timestamp": 1704110400000
}
```

The facade provides a **complete, unified solution** for all your API integration needs with proper async processing, token management, and comprehensive error handling! 🎉
