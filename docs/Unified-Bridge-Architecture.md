# Unified Bridge Architecture Design

## Current Understanding
- **Bridge** = Generic proxy service for ALL external API calls
- **Apigee Token Service** = For internal company APIs (via Apigee)
- **Fenergo Token Service** = For external company APIs (via Fenergo proxy)
- **Proxy** = External company's proxy (like Fenergo's proxy)

## Proposed Architecture

### 1. Generic Bridge Service
```java
@Service
public class GenericBridgeService {
    
    private final ApigeeTokenService apigeeTokenService;
    private final FenergoTokenService fenergoTokenService;
    private final RestClient restClient;
    
    public BridgeResponse callExternalApi(BridgeRequest request) {
        // Determine which token service to use based on API provider
        TokenService tokenService = getTokenService(request.getApiProvider());
        
        // Get cached token or fetch new one
        String token = tokenService.getToken(request.getAuthScope());
        
        // Make the API call through appropriate proxy
        return makeApiCall(request, token);
    }
}
```

### 2. Token Services
```java
// For Internal Company APIs (via Apigee)
@Service
public class ApigeeTokenService {
    public String getToken(String scope) {
        // Get Apigee token, cache it, return
    }
}

// For External Company APIs (via their proxy)
@Service  
public class FenergoTokenService {
    public String getToken(String scope) {
        // Get Fenergo token, cache it, return
    }
}
```

### 3. API Provider Configuration
```java
public enum ApiProvider {
    APIGEE("apigee", "Internal Company APIs"),
    FENERGO("fenergo", "External Company APIs");
}
```

## Benefits of This Approach
1. **Single Bridge** for all external API calls
2. **Multiple Token Services** for different providers
3. **Unified Interface** for all API integrations
4. **Easy to Extend** for new API providers
5. **Clear Separation** between internal (Apigee) and external (Fenergo) APIs

## Implementation Plan
1. Refactor `GenericHttpClient` to `GenericBridgeService`
2. Create separate token services for each provider
3. Update all API calls to use the unified bridge
4. Remove the separate `FenergoProxyService`
5. Update configuration for different API providers

Would you like me to implement this unified architecture?
