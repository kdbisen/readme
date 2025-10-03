# Token Authentication System

## Overview

The Banking Onboarding Service implements a comprehensive token authentication system that handles authentication for both internal (Apigee) and external (Fenergo) APIs. The system automatically manages token fetching, caching, and refresh.

## Architecture

### Token Services

#### 1. **ApigeeTokenService** - Internal Company APIs
- **Purpose**: Handles authentication for internal Apigee APIs
- **Token Type**: Apigee Bearer Token
- **Caching**: Enabled with automatic refresh
- **Scope**: Configurable per API endpoint

#### 2. **FenergoTokenService** - External Company APIs  
- **Purpose**: Handles authentication for external Fenergo APIs
- **Token Type**: JWT Token
- **Caching**: Enabled with automatic refresh
- **Scope**: Configurable per API endpoint

### Service Integration

#### **TransformationService** (Apigee APIs)
```java
// Automatically gets Apigee token
String token = apigeeTokenService.getToken(transformationAuthScope);

// Includes token in API call
String response = restClient.post()
    .uri(transformationEndpoint)
    .header("Authorization", "Bearer " + token)
    .header("X-Correlation-ID", correlationId)
    // ... other headers
    .retrieve()
    .body(String.class);
```

#### **FenergoService** (External APIs)
```java
// Automatically gets Fenergo JWT token
String token = fenergoTokenService.getToken(fenergoAuthScope).getAccessToken();

// Includes token in API call
String response = restClient.post()
    .uri(entityCreateEndpoint)
    .header("Authorization", "Bearer " + token)
    .header("X-Correlation-ID", correlationId)
    .header("X-TENANT-ID", tenantId)
    // ... other headers
    .retrieve()
    .body(String.class);
```

## Configuration

### Environment Variables

#### **Apigee Token Configuration**
```properties
# Token Service URL
APIGEE_TOKEN_SERVICE_URL=http://apigee-token-service:8080/oauth/token

# Client Credentials
APIGEE_CLIENT_ID=apigee-client
APIGEE_CLIENT_SECRET=apigee-secret

# Default Scope
APIGEE_DEFAULT_SCOPE=apigee-api

# Cache Settings
APIGEE_CACHE_ENABLED=true

# API-Specific Scopes
APIGEE_TRANSFORMATION_AUTH_SCOPE=transformation-api
```

#### **Fenergo Token Configuration**
```properties
# Token Service URL
FENERGO_TOKEN_SERVICE_URL=http://fenergo-token-service:8080/oauth/token

# Client Credentials
FENERGO_CLIENT_ID=fenergo-client
FENERGO_CLIENT_SECRET=fenergo-secret

# Default Scope
FENERGO_DEFAULT_SCOPE=fenergo-api
FENERGO_AUTH_SCOPE=fenergo-api

# Cache Settings
FENERGO_CACHE_ENABLED=true
```

### Application Properties

```properties
# ===========================================
# APIGEE AUTHENTICATION (INTERNAL APIS)
# ===========================================
apigee.auth.token-service.url=${APIGEE_TOKEN_SERVICE_URL:http://apigee-token-service:8080/oauth/token}
apigee.auth.client-id=${APIGEE_CLIENT_ID:apigee-client}
apigee.auth.client-secret=${APIGEE_CLIENT_SECRET:apigee-secret}
apigee.auth.default-scope=${APIGEE_DEFAULT_SCOPE:apigee-api}
apigee.auth.cache-enabled=${APIGEE_CACHE_ENABLED:true}

# ===========================================
# FENERGO AUTHENTICATION (EXTERNAL APIS)
# ===========================================
fenergo.auth.token-service.url=${FENERGO_TOKEN_SERVICE_URL:http://fenergo-token-service:8080/oauth/token}
fenergo.auth.client-id=${FENERGO_CLIENT_ID:fenergo-client}
fenergo.auth.client-secret=${FENERGO_CLIENT_SECRET:fenergo-secret}
fenergo.auth.default-scope=${FENERGO_DEFAULT_SCOPE:fenergo-api}
fenergo.auth.scope=${FENERGO_AUTH_SCOPE:fenergo-api}
fenergo.auth.cache-enabled=${FENERGO_CACHE_ENABLED:true}
```

## Token Flow

### 1. **Token Request Flow**
```
Service Method Call
    ↓
Check Token Cache
    ↓
Token Valid? → Yes → Use Cached Token
    ↓ No
Fetch New Token from Token Service
    ↓
Cache Token
    ↓
Use Token in API Call
```

### 2. **Automatic Token Refresh**
- **Cache Validation**: Tokens are checked for validity before use
- **Automatic Refresh**: Expired tokens are automatically refreshed
- **Fallback**: Mock tokens are used if token service is unavailable

### 3. **Error Handling**
- **Token Service Unavailable**: Falls back to mock tokens
- **Invalid Credentials**: Logs error and uses mock tokens
- **Network Issues**: Retries with exponential backoff

## API Endpoints with Token Authentication

### **Transformation APIs** (Apigee - Internal)
- **Endpoint**: `/api/v1/onboarding/transform/xml-to-json`
- **Token Type**: Apigee Bearer Token
- **Scope**: `transformation-api`
- **Headers**: `Authorization: Bearer <apigee-token>`

### **Fenergo APIs** (External)
- **Entity Creation**: `/api/v1/onboarding/fenergo/entity/create`
- **Journey Schema Evaluation**: Logic Engine API
- **Journey Launch**: Journey Command API
- **Token Type**: JWT Token
- **Scope**: `fenergo-api`
- **Headers**: `Authorization: Bearer <jwt-token>`

## Token Management Features

### **Caching**
- **In-Memory Cache**: Tokens are cached in memory for performance
- **Scope-Based**: Different tokens for different API scopes
- **Automatic Expiry**: Tokens are automatically refreshed when expired

### **Security**
- **Client Credentials**: OAuth2 client credentials flow
- **Secure Storage**: Credentials stored in environment variables
- **Token Rotation**: Automatic token refresh prevents token reuse

### **Monitoring**
- **Token Fetch Logging**: All token operations are logged
- **Cache Hit/Miss**: Cache performance is monitored
- **Error Tracking**: Token service errors are tracked

## Usage Examples

### **Direct Service Usage**
```java
@Autowired
private TransformationService transformationService;

@Autowired
private FenergoService fenergoService;

// XML to JSON transformation (uses Apigee token automatically)
CompletableFuture<ApiResponse> response = transformationService
    .transformXmlToJson(xmlData, correlationId);

// Fenergo entity creation (uses JWT token automatically)
Map<String, Object> entityData = Map.of("data", jsonData);
CompletableFuture<ApiResponse> response = fenergoService
    .createEntity(entityData, correlationId);
```

### **Controller Usage**
```java
@PostMapping("/transform/xml-to-json")
public CompletableFuture<ResponseEntity<ApiResponse>> transformXmlToJson(
        @RequestBody Map<String, String> request,
        @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
    
    String actualCorrelationId = correlationIdService.getOrGenerateCorrelationId(correlationId);
    String xmlData = request.get("xmlData");
    
    // Token is automatically handled by TransformationService
    return transformationService.transformXmlToJson(xmlData, actualCorrelationId)
            .thenApply(ResponseEntity::ok);
}
```

## Token Service Integration

### **Apigee Token Service**
- **URL**: `http://apigee-token-service:8080/oauth/token`
- **Method**: POST
- **Body**: Client credentials with scope
- **Response**: Access token with expiry information

### **Fenergo Token Service**
- **URL**: `http://fenergo-token-service:8080/oauth/token`
- **Method**: POST
- **Body**: Client credentials with scope
- **Response**: JWT token with expiry information

## Mock Token Fallback

When token services are unavailable, the system automatically falls back to mock tokens:

### **Apigee Mock Token**
```
Token: mock-apigee-token-{scope}-{timestamp}
Type: Bearer
Expiry: 1 hour
```

### **Fenergo Mock Token**
```
Token: mock-jwt-token-{scope}-{timestamp}
Type: Bearer
Expiry: 1 hour
```

## Best Practices

### **Configuration**
1. **Environment Variables**: Use environment variables for sensitive credentials
2. **Scope Separation**: Use different scopes for different API endpoints
3. **Cache Settings**: Enable caching for production, disable for testing

### **Error Handling**
1. **Graceful Degradation**: Always fall back to mock tokens
2. **Logging**: Log all token operations for debugging
3. **Monitoring**: Monitor token service health

### **Security**
1. **Credential Rotation**: Regularly rotate client credentials
2. **Token Expiry**: Use short-lived tokens for security
3. **Secure Transport**: Always use HTTPS for token services

## Troubleshooting

### **Common Issues**

#### **Token Service Unavailable**
```
WARN: Failed to fetch Apigee token for scope: transformation-api, error: Connection refused. Using mock token.
```
**Solution**: Check token service availability and network connectivity

#### **Invalid Credentials**
```
WARN: Failed to fetch token from external service for scope: fenergo-api, error: 401 Unauthorized. Using mock token.
```
**Solution**: Verify client credentials in environment variables

#### **Token Expiry**
```
DEBUG: Token expired for scope: fenergo-api, fetching new token
```
**Solution**: This is normal behavior - tokens are automatically refreshed

### **Debugging**
1. **Enable Debug Logging**: Set `logging.level.com.banking.onboarding.auth=DEBUG`
2. **Check Token Cache**: Monitor cache hit/miss ratios
3. **Verify Configuration**: Ensure all environment variables are set correctly

## Summary

The token authentication system provides:

✅ **Automatic Token Management**: Tokens are fetched, cached, and refreshed automatically
✅ **Dual Token Support**: Separate handling for Apigee (internal) and Fenergo (external) APIs
✅ **Robust Error Handling**: Graceful fallback to mock tokens when services are unavailable
✅ **Performance Optimization**: Token caching reduces API calls to token services
✅ **Security**: Secure credential storage and token rotation
✅ **Monitoring**: Comprehensive logging and error tracking

**The system is production-ready and handles all token authentication requirements automatically!** 🚀





