# Proxy-Based Fenergo API Integration

## 🎯 **Overview**

The service now uses a **proxy-based architecture** for Fenergo API calls with comprehensive error handling using Spring AOP.

## 🏗️ **Architecture**

```
Banking Service → Proxy Service → Fenergo API
     ↓              ↓              ↓
   JWT Token    Header Routing   OCIN/JWT
   Validation   Error Handling   Authentication
```

## ⚙️ **Configuration**

### **Environment Variables**

```bash
# Proxy Configuration
export PROXY_URL=http://localhost:8081
export FENERGO_KYC_URL=https://fenergo-api.com/kyc/submit
export FENERGO_KYC_STATUS_URL=https://fenergo-api.com/kyc/{entityId}/status
export FENERGO_COMPLIANCE_URL=https://fenergo-api.com/compliance/check
```

### **Proxy Headers**

The proxy service receives these headers:

```http
X-Fenergo-Endpoint: https://fenergo-api.com/kyc/submit
Authorization: Bearer <JWT_TOKEN>
X-Proxy-Service: banking-onboarding-service
X-Request-Source: fenergo-proxy
```

## 🚀 **Usage**

### **1. Check Proxy Status**

```bash
# Check if proxy is available
curl http://localhost:8080/api/proxy/health

# Get proxy configuration
curl http://localhost:8080/api/proxy/config
```

### **2. Test Proxy Call**

```bash
# Test KYC submission via proxy
curl -X POST http://localhost:8080/api/proxy/test/SUBMIT_KYC \
  -H "Content-Type: application/json" \
  -d '{"entityId": "123", "name": "John Doe"}'
```

### **3. Use in Application**

```java
// The FenergoFunction automatically uses proxy calls
ApiResponse response = apiBridgeService.callApiViaProxy("SUBMIT_KYC", payload);
```

## 🛡️ **Error Handling with Spring AOP**

### **Automatic Error Handling**

The `ProxyErrorHandlingAdvice` automatically:

✅ **Logs all proxy calls** with timing information  
✅ **Handles exceptions** gracefully  
✅ **Monitors response times** (warns for slow responses)  
✅ **Provides detailed error messages**  
✅ **Tracks correlation IDs** for tracing  

### **Error Response Format**

```json
{
  "success": false,
  "statusCode": 500,
  "responseBody": "Proxy Error",
  "fenergoEndpoint": "https://fenergo-api.com/kyc/submit",
  "errorMessage": "Connection timeout",
  "responseTimeMs": 1234567890
}
```

## 🔧 **Proxy Service Requirements**

Your proxy service should:

1. **Accept the headers** mentioned above
2. **Route to Fenergo** using the `X-Fenergo-Endpoint` header
3. **Handle authentication** using JWT or OCIN tokens
4. **Return responses** in the expected format
5. **Provide health check** endpoint at `/health`

### **Example Proxy Service**

```java
@RestController
public class FenergoProxyController {
    
    @PostMapping("/**")
    public ResponseEntity<String> proxyCall(
            @RequestHeader("X-Fenergo-Endpoint") String fenergoEndpoint,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Object payload) {
        
        // Forward to Fenergo API
        return forwardToFenergo(fenergoEndpoint, authHeader, payload);
    }
}
```

## 📊 **Monitoring**

### **Proxy Metrics**

```bash
# Get proxy status
curl http://localhost:8080/api/proxy/status

# Response includes:
{
  "available": true,
  "proxyUrl": "http://localhost:8081",
  "healthCheckUrl": "http://localhost:8081/health",
  "configuredEndpoints": 3
}
```

### **AOP Logging**

All proxy calls are automatically logged with:

- **Correlation ID** for tracing
- **Execution time** for performance monitoring
- **Success/failure status**
- **Error details** for debugging

## 🎯 **Benefits**

✅ **Security**: Centralized authentication and authorization  
✅ **Monitoring**: Comprehensive logging and metrics  
✅ **Error Handling**: Automatic exception handling with AOP  
✅ **Flexibility**: Easy to switch between direct and proxy calls  
✅ **Tracing**: Full request tracing with correlation IDs  
✅ **Performance**: Response time monitoring and alerts  

## 🔄 **Fallback Strategy**

If proxy is unavailable, the service automatically falls back to direct API calls:

```java
// Automatic fallback
if (fenergoConfig == null) {
    log.warn("No Fenergo configuration found, using direct call");
    return callApi(endpointName, payload);
}
```

## 🎉 **Summary**

The proxy-based architecture provides:

- **Better security** with centralized token management
- **Comprehensive error handling** using Spring AOP
- **Easy monitoring** with built-in metrics
- **Flexible deployment** with environment-specific configuration
- **Automatic fallback** for high availability

**The service now automatically routes Fenergo calls through your proxy with full error handling and monitoring!** 🚀
