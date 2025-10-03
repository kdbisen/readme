# Properties Configuration - Clean & Simple

## Overview

The Banking Onboarding Service now uses a **clean and minimal configuration** approach with only essential properties.

## 📁 Configuration Files

### **Main Configuration**
- **`application.properties`** - Main application configuration
- **`logback-spring.xml`** - Logging configuration
- **`docker-compose.yml`** - Docker deployment configuration

### **Test Configuration**
- **`application-test.properties`** - Test-specific configuration

## 🗑️ Removed Files

The following **unwanted properties files** have been removed:

- ❌ `application-dev.properties` - Redundant dev configuration
- ❌ `application-prod.properties` - Redundant prod configuration  
- ❌ `application-test.properties` (main) - Redundant test configuration
- ❌ `endpoints.properties` - Redundant endpoint configuration
- ❌ `endpoints-test.properties` - Redundant test endpoint configuration

## ⚙️ Current Configuration Structure

### **1. Server Configuration**
```properties
server.port=8080
server.servlet.context-path=/api/v1
```

### **2. MongoDB Configuration**
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=banking_onboarding
spring.data.mongodb.authentication-database=admin
```

### **3. Step Configuration (Fenergo Flow)**
```properties
onboarding.steps.definition=XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH
onboarding.steps.execution.order=PRIORITY
onboarding.steps.retry.enabled=true
onboarding.steps.retry.max-attempts=3
onboarding.steps.retry.delay-ms=1000
onboarding.steps.retry.backoff-multiplier=2.0
onboarding.steps.timeout-ms=30000
```

### **4. Fenergo API Configuration**
```properties
fenergo.entity.api.url=https://fenergo.example.com/entity
fenergo.logic.engine.url=https://fenergo.example.com/journeylogicengine/api/engine/evaluate-journey-schema
fenergo.journey.command.url=https://fenergo.example.com/api/journey-instance/launch-journey
fenergo.tenant.id=default-tenant
fenergo.journey.type.filter=Client Onboarding
```

### **5. Apigee Configuration (Internal APIs)**
```properties
apigee.transformation.endpoint=https://apigee-transformation-service.com/api/v1/transform
apigee.transformation.auth-scope=transformation-api
apigee.transformation.timeout=30000
apigee.transformation.retry-count=3
```

### **6. Authentication Configuration**
```properties
# Apigee Authentication
apigee.auth.token-service.url=http://apigee-token-service:8080/oauth/token
apigee.auth.client-id=apigee-client
apigee.auth.client-secret=apigee-secret
apigee.auth.default-scope=apigee-api
apigee.auth.cache-enabled=true

# Fenergo Authentication
fenergo.auth.token-service.url=http://fenergo-token-service:8080/oauth/token
fenergo.auth.client-id=fenergo-client
fenergo.auth.client-secret=fenergo-secret
fenergo.auth.default-scope=fenergo-api
fenergo.auth.cache-enabled=true
```

### **7. HTTP Client Configuration**
```properties
restclient.timeout.connect=10000
restclient.timeout.read=30000
restclient.timeout.write=30000
```

### **8. Logging Configuration**
```properties
logging.level.com.banking.onboarding=INFO
logging.level.com.banking.onboarding.logging=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.springframework.data.mongodb=INFO
logging.config=classpath:logback-spring.xml
```

### **9. Jackson Configuration**
```properties
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.default-property-inclusion=NON_NULL
```

## 🌍 Environment Variables

All configuration supports **environment variable overrides**:

### **Database**
- `MONGODB_URI` - MongoDB connection string
- `ENVIRONMENT` - Environment name (dev/prod)
- `APP_VERSION` - Application version

### **Fenergo APIs**
- `FENERGO_ENTITY_API_URL` - Entity API URL
- `FENERGO_LOGIC_ENGINE_URL` - Logic Engine URL
- `FENERGO_JOURNEY_COMMAND_URL` - Journey Command URL
- `FENERGO_TENANT_ID` - Tenant ID
- `FENERGO_JOURNEY_TYPE_FILTER` - Journey type filter

### **Apigee APIs**
- `APIGEE_TRANSFORMATION_ENDPOINT` - Transformation endpoint
- `APIGEE_TRANSFORMATION_AUTH_SCOPE` - Auth scope
- `APIGEE_TRANSFORMATION_TIMEOUT` - Timeout
- `APIGEE_TRANSFORMATION_RETRY_COUNT` - Retry count

### **Authentication**
- `APIGEE_TOKEN_SERVICE_URL` - Apigee token service URL
- `APIGEE_CLIENT_ID` - Apigee client ID
- `APIGEE_CLIENT_SECRET` - Apigee client secret
- `FENERGO_TOKEN_SERVICE_URL` - Fenergo token service URL
- `FENERGO_CLIENT_ID` - Fenergo client ID
- `FENERGO_CLIENT_SECRET` - Fenergo client secret

### **Step Configuration**
- `ONBOARDING_STEPS_DEFINITION` - Step definitions
- `ONBOARDING_STEPS_EXECUTION_ORDER` - Execution order (PRIORITY/ORDER)
- `ONBOARDING_STEPS_RETRY_ENABLED` - Retry enabled
- `ONBOARDING_STEPS_RETRY_MAX_ATTEMPTS` - Max retry attempts
- `ONBOARDING_STEPS_RETRY_DELAY_MS` - Retry delay
- `ONBOARDING_STEPS_RETRY_BACKOFF_MULTIPLIER` - Backoff multiplier
- `ONBOARDING_STEPS_TIMEOUT_MS` - Step timeout

## 🎯 Benefits of Clean Configuration

### **1. Simplicity**
- ✅ **Single source of truth** - One main properties file
- ✅ **No redundancy** - Removed duplicate configurations
- ✅ **Clear structure** - Well-organized sections

### **2. Maintainability**
- ✅ **Easy to modify** - All config in one place
- ✅ **Version control friendly** - Clean diffs
- ✅ **Environment agnostic** - Uses environment variables

### **3. Flexibility**
- ✅ **Environment variable overrides** - Easy deployment
- ✅ **Docker ready** - Works with docker-compose
- ✅ **Test friendly** - Separate test configuration

### **4. Performance**
- ✅ **Minimal configuration** - Faster startup
- ✅ **No unused properties** - Cleaner memory usage
- ✅ **Optimized logging** - Efficient log configuration

## 🚀 Usage Examples

### **Local Development**
```bash
# Use defaults
java -jar banking-onboarding-service.jar

# Override specific values
MONGODB_URI=mongodb://localhost:27017/my_db \
FENERGO_ENTITY_API_URL=https://my-fenergo.com/entity \
java -jar banking-onboarding-service.jar
```

### **Docker Deployment**
```bash
# Use docker-compose with environment variables
docker-compose up -d
```

### **Production Deployment**
```bash
# Set production environment variables
export ENVIRONMENT=prod
export MONGODB_URI=mongodb://prod-mongo:27017/banking_onboarding
export FENERGO_ENTITY_API_URL=https://prod-fenergo.com/entity
export APIGEE_TRANSFORMATION_ENDPOINT=https://prod-apigee.com/transform

java -jar banking-onboarding-service.jar
```

## 📋 Configuration Checklist

- ✅ **Server configuration** - Port and context path
- ✅ **Database configuration** - MongoDB settings
- ✅ **Step configuration** - Fenergo flow steps
- ✅ **API configuration** - Fenergo and Apigee endpoints
- ✅ **Authentication** - Token services and scopes
- ✅ **HTTP client** - RestClient timeouts
- ✅ **Logging** - Logback configuration
- ✅ **Jackson** - JSON serialization settings
- ✅ **Management** - Health and metrics endpoints

---

**The configuration is now clean, simple, and maintainable!** 🎉




