# Endpoint Configuration System

## 🎯 Overview

The endpoint configuration system allows you to manage API endpoints through property files instead of hardcoded Java code. This provides flexibility, maintainability, and environment-specific configurations.

## 📁 Configuration Files

### 1. Main Configuration File
**File**: `src/main/resources/endpoints.properties`

This is the main configuration file containing all endpoint definitions.

### 2. Environment-Specific Files
- **Development**: `src/main/resources/endpoints-dev.properties`
- **Production**: `src/main/resources/endpoints-prod.properties`

These files override or extend the main configuration for specific environments.

## 🔧 Configuration Format

### Property Format
```properties
# Format: endpoint.{endpointName}.{property}=value

# Basic endpoint configuration
endpoint.CREATE_ENTITY.name=CREATE_ENTITY
endpoint.CREATE_ENTITY.method=POST
endpoint.CREATE_ENTITY.path=/entities
endpoint.CREATE_ENTITY.description=Create new entity
endpoint.CREATE_ENTITY.authRequired=true
endpoint.CREATE_ENTITY.authScope=fenergo-entity-write
endpoint.CREATE_ENTITY.authType=JWT
endpoint.CREATE_ENTITY.timeoutMs=30000
endpoint.CREATE_ENTITY.retryAttempts=3
```

### Supported Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | String | Yes | Endpoint name (must match property prefix) |
| `method` | String | Yes | HTTP method (GET, POST, PUT, DELETE) |
| `path` | String | Yes | API path (relative or absolute URL) |
| `description` | String | No | Human-readable description |
| `authRequired` | Boolean | No | Whether authentication is required (default: true) |
| `authScope` | String | No | JWT token scope for authentication |
| `authType` | String | No | Authentication type (JWT, API_KEY, BASIC) |
| `timeoutMs` | Integer | No | Request timeout in milliseconds (default: 30000) |
| `retryAttempts` | Integer | No | Number of retry attempts (default: 3) |

## 🚀 Usage Examples

### 1. Basic Endpoint Configuration

```properties
# Simple GET endpoint
endpoint.GET_ENTITY.name=GET_ENTITY
endpoint.GET_ENTITY.method=GET
endpoint.GET_ENTITY.path=/entities/{entityId}
endpoint.GET_ENTITY.description=Get entity by ID
endpoint.GET_ENTITY.authRequired=true
endpoint.GET_ENTITY.authScope=fenergo-entity-read
```

### 2. Public Endpoint (No Authentication)

```properties
# Public health check endpoint
endpoint.HEALTH_CHECK.name=HEALTH_CHECK
endpoint.HEALTH_CHECK.method=GET
endpoint.HEALTH_CHECK.path=/health
endpoint.HEALTH_CHECK.description=Health check endpoint
endpoint.HEALTH_CHECK.authRequired=false
endpoint.HEALTH_CHECK.timeoutMs=5000
endpoint.HEALTH_CHECK.retryAttempts=1
```

### 3. External API Endpoint

```properties
# External API with full URL
endpoint.EXTERNAL_API_CALL.name=EXTERNAL_API_CALL
endpoint.EXTERNAL_API_CALL.method=GET
endpoint.EXTERNAL_API_CALL.path=https://external-api.com/data
endpoint.EXTERNAL_API_CALL.description=Call external API
endpoint.EXTERNAL_API_CALL.authRequired=true
endpoint.EXTERNAL_API_CALL.authScope=external-api-read
endpoint.EXTERNAL_API_CALL.authType=API_KEY
```

### 4. Long-Running Operation

```properties
# Compliance check with extended timeout
endpoint.RUN_COMPLIANCE_CHECK.name=RUN_COMPLIANCE_CHECK
endpoint.RUN_COMPLIANCE_CHECK.method=POST
endpoint.RUN_COMPLIANCE_CHECK.path=/compliance/check
endpoint.RUN_COMPLIANCE_CHECK.description=Run compliance check
endpoint.RUN_COMPLIANCE_CHECK.authRequired=true
endpoint.RUN_COMPLIANCE_CHECK.authScope=fenergo-compliance-write
endpoint.RUN_COMPLIANCE_CHECK.timeoutMs=60000
endpoint.RUN_COMPLIANCE_CHECK.retryAttempts=2
```

## 🌍 Environment-Specific Configuration

### Development Environment (`endpoints-dev.properties`)

```properties
# Development-specific endpoints
endpoint.DEV_TEST_ENDPOINT.name=DEV_TEST_ENDPOINT
endpoint.DEV_TEST_ENDPOINT.method=POST
endpoint.DEV_TEST_ENDPOINT.path=/dev/test
endpoint.DEV_TEST_ENDPOINT.description=Development test endpoint
endpoint.DEV_TEST_ENDPOINT.authRequired=false
endpoint.DEV_TEST_ENDPOINT.timeoutMs=5000
endpoint.DEV_TEST_ENDPOINT.retryAttempts=1

# Override production endpoints for development
endpoint.SUBMIT_KYC.timeoutMs=10000
endpoint.SUBMIT_KYC.retryAttempts=1
endpoint.GET_KYC_STATUS.timeoutMs=5000
endpoint.GET_KYC_STATUS.retryAttempts=1
```

### Production Environment (`endpoints-prod.properties`)

```properties
# Production-specific endpoints
endpoint.PROD_MONITORING.name=PROD_MONITORING
endpoint.PROD_MONITORING.method=GET
endpoint.PROD_MONITORING.path=/monitoring/health
endpoint.PROD_MONITORING.description=Production monitoring endpoint
endpoint.PROD_MONITORING.authRequired=true
endpoint.PROD_MONITORING.authScope=fenergo-monitoring-read
endpoint.PROD_MONITORING.timeoutMs=5000
endpoint.PROD_MONITORING.retryAttempts=1

# Production-optimized timeouts
endpoint.SUBMIT_KYC.timeoutMs=60000
endpoint.SUBMIT_KYC.retryAttempts=3
endpoint.RUN_COMPLIANCE_CHECK.timeoutMs=120000
endpoint.RUN_COMPLIANCE_CHECK.retryAttempts=2
```

## 🔧 Application Configuration

### application.properties

```properties
# Endpoint Configuration
endpoint.config.file=classpath:endpoints.properties
endpoint.config.enabled=true

# Environment-specific configuration
spring.profiles.active=dev
```

### Environment-Specific Properties

```properties
# application-dev.properties
endpoint.config.file=classpath:endpoints-dev.properties

# application-prod.properties
endpoint.config.file=classpath:endpoints-prod.properties
```

## 🚀 API Usage

### 1. List All Endpoints

```bash
curl http://localhost:8080/api/config/endpoints
```

**Response**:
```json
{
  "CREATE_ENTITY": {
    "name": "CREATE_ENTITY",
    "method": "POST",
    "path": "/entities",
    "description": "Create new entity",
    "authRequired": true,
    "authScope": "fenergo-entity-write",
    "authType": "JWT",
    "timeoutMs": 30000,
    "retryAttempts": 3
  },
  "GET_ENTITY": {
    "name": "GET_ENTITY",
    "method": "GET",
    "path": "/entities/{entityId}",
    "description": "Get entity by ID",
    "authRequired": true,
    "authScope": "fenergo-entity-read",
    "authType": "JWT",
    "timeoutMs": 15000,
    "retryAttempts": 2
  }
}
```

### 2. Get Specific Endpoint

```bash
curl http://localhost:8080/api/config/endpoints/SUBMIT_KYC
```

### 3. Check Endpoint Exists

```bash
curl http://localhost:8080/api/config/endpoints/SUBMIT_KYC/exists
```

**Response**:
```json
{
  "exists": true,
  "endpoint": "SUBMIT_KYC"
}
```

### 4. Reload Configuration

```bash
curl -X POST http://localhost:8080/api/config/endpoints/reload
```

**Response**:
```json
{
  "message": "Endpoints reloaded successfully"
}
```

### 5. Get Configuration Statistics

```bash
curl http://localhost:8080/api/config/stats
```

**Response**:
```json
{
  "totalEndpoints": 12,
  "authRequiredEndpoints": 10,
  "publicEndpoints": 2,
  "configFile": "classpath:endpoints.properties",
  "configEnabled": true,
  "registeredEndpoints": 12,
  "baseUrl": "http://localhost:8081/fenergo/api"
}
```

### 6. Register New Endpoint Dynamically

```bash
curl -X POST http://localhost:8080/api/config/endpoints \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CUSTOM_ENDPOINT",
    "method": "POST",
    "path": "/custom/operation",
    "description": "Custom business operation",
    "authRequired": true,
    "authScope": "fenergo-custom-write",
    "authType": "JWT",
    "timeoutMs": 25000,
    "retryAttempts": 2
  }'
```

## 🔄 Dynamic Configuration Updates

### 1. Hot Reload

The configuration system supports hot reloading without application restart:

```bash
# Update endpoints.properties file
# Then trigger reload
curl -X POST http://localhost:8080/api/config/endpoints/reload
```

### 2. Runtime Registration

You can register new endpoints at runtime:

```java
@RestController
public class CustomController {
    
    @Autowired
    private ApiEndpointRegistry endpointRegistry;
    
    @PostMapping("/register-endpoint")
    public ResponseEntity<String> registerEndpoint() {
        ApiEndpoint endpoint = ApiEndpoint.builder()
                .name("RUNTIME_ENDPOINT")
                .method("POST")
                .path("/runtime/endpoint")
                .description("Runtime registered endpoint")
                .authRequired(true)
                .authScope("fenergo-runtime-write")
                .build();
        
        endpointRegistry.registerEndpoint(endpoint);
        return ResponseEntity.ok("Endpoint registered");
    }
}
```

## 🧪 Testing Configuration

### 1. Unit Testing

```java
@Test
void testEndpointConfiguration() {
    // Test endpoint loading
    Map<String, EndpointConfig> configs = configLoader.getAllEndpointConfigs();
    assertThat(configs).isNotEmpty();
    
    // Test specific endpoint
    Optional<EndpointConfig> config = configLoader.getEndpointConfig("SUBMIT_KYC");
    assertThat(config).isPresent();
    assertThat(config.get().getMethod()).isEqualTo("POST");
    assertThat(config.get().getAuthRequired()).isTrue();
}
```

### 2. Integration Testing

```java
@Test
void testEndpointRegistry() {
    // Test endpoint registration
    ApiEndpoint endpoint = ApiEndpoint.builder()
            .name("TEST_ENDPOINT")
            .method("GET")
            .path("/test")
            .build();
    
    endpointRegistry.registerEndpoint(endpoint);
    assertThat(endpointRegistry.hasEndpoint("TEST_ENDPOINT")).isTrue();
    
    // Test URL building
    String url = endpointRegistry.buildUrl("TEST_ENDPOINT");
    assertThat(url).isEqualTo("http://localhost:8081/fenergo/api/test");
}
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Configuration Not Loading

**Problem**: Endpoints not loaded from properties file

**Solution**:
```properties
# Check configuration is enabled
endpoint.config.enabled=true

# Check file path
endpoint.config.file=classpath:endpoints.properties
```

#### 2. Missing Required Properties

**Problem**: Endpoint configuration incomplete

**Solution**:
```properties
# Ensure required properties are set
endpoint.MY_ENDPOINT.name=MY_ENDPOINT
endpoint.MY_ENDPOINT.method=POST
endpoint.MY_ENDPOINT.path=/my/endpoint
```

#### 3. Environment-Specific Overrides Not Working

**Problem**: Environment-specific properties not applied

**Solution**:
```properties
# Check active profile
spring.profiles.active=dev

# Check environment-specific file exists
endpoint.config.file=classpath:endpoints-dev.properties
```

### Debug Commands

```bash
# Check configuration status
curl http://localhost:8080/api/config/info

# List all endpoints
curl http://localhost:8080/api/config/endpoints

# Check specific endpoint
curl http://localhost:8080/api/config/endpoints/SUBMIT_KYC

# Get statistics
curl http://localhost:8080/api/config/stats
```

## 📋 Best Practices

### 1. Naming Conventions

```properties
# Use UPPER_CASE for endpoint names
endpoint.SUBMIT_KYC.name=SUBMIT_KYC
endpoint.GET_ENTITY_STATUS.name=GET_ENTITY_STATUS
endpoint.RUN_COMPLIANCE_CHECK.name=RUN_COMPLIANCE_CHECK
```

### 2. Environment-Specific Settings

```properties
# Development: Shorter timeouts, fewer retries
endpoint.SUBMIT_KYC.timeoutMs=10000
endpoint.SUBMIT_KYC.retryAttempts=1

# Production: Longer timeouts, more retries
endpoint.SUBMIT_KYC.timeoutMs=60000
endpoint.SUBMIT_KYC.retryAttempts=3
```

### 3. Security Configuration

```properties
# Always specify auth requirements explicitly
endpoint.SENSITIVE_OPERATION.authRequired=true
endpoint.SENSITIVE_OPERATION.authScope=fenergo-sensitive-write
endpoint.SENSITIVE_OPERATION.authType=JWT

# Public endpoints should be explicit
endpoint.HEALTH_CHECK.authRequired=false
```

### 4. Documentation

```properties
# Always provide descriptions
endpoint.COMPLEX_OPERATION.description=Complex business operation that processes multiple entities
endpoint.COMPLEX_OPERATION.timeoutMs=120000
endpoint.COMPLEX_OPERATION.retryAttempts=2
```

## 🎯 Benefits

1. **Flexibility**: Change endpoints without code changes
2. **Environment-Specific**: Different configs for dev/prod
3. **Maintainability**: Centralized configuration management
4. **Hot Reload**: Update endpoints without restart
5. **Runtime Registration**: Add endpoints dynamically
6. **Validation**: Built-in configuration validation
7. **Monitoring**: Configuration statistics and health checks

This configuration system provides a powerful, flexible way to manage API endpoints in your banking onboarding service! 🚀
