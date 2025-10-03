# Properties Structure Documentation

This document describes the organized folder structure for configuration properties in the Banking Onboarding Service.

## 📁 Folder Structure

```
src/main/resources/
├── application.properties                    # Main configuration file
└── config/                                  # Organized configuration directory
    ├── core/                                # Core application configuration
    │   ├── application-core.properties     # Application metadata and server config
    │   ├── database.properties             # Database configuration
    │   └── logging.properties              # Logging configuration
    ├── external/                            # External services configuration
    │   ├── fenergo.properties              # Fenergo API configuration
    │   └── apigee.properties               # Apigee API configuration
    ├── security/                           # Security configuration
    │   └── security.properties             # SSL, CORS, auth, validation
    ├── monitoring/                         # Monitoring and observability
    │   └── monitoring.properties           # Metrics, health checks, tracing
    ├── features/                           # Feature-specific configuration
    │   ├── steps.properties                # Step execution configuration
    │   ├── http-client.properties          # HTTP client configuration
    │   └── jackson.properties              # JSON serialization configuration
    └── environments/                        # Environment-specific overrides
        ├── dev/
        │   └── application-dev.properties  # Development environment
        ├── test/
        │   └── application-test.properties # Test environment
        ├── staging/
        │   └── application-staging.properties # Staging environment
        └── prod/
            └── application-prod.properties  # Production environment
```

## 🎯 Configuration Categories

### 1. Core Configuration (`config/core/`)

**Purpose**: Essential application configuration that's always loaded.

#### `application-core.properties`
- Application metadata (name, version, description)
- Server configuration (port, context path, session settings)
- Management endpoints configuration
- Spring Boot basic settings

#### `database.properties`
- MongoDB connection settings
- Connection pool configuration
- Write concern and read preference
- Index configuration

#### `logging.properties`
- Logging levels for different packages
- Log patterns and file configuration
- Environment information

### 2. External Services (`config/external/`)

**Purpose**: Configuration for external API integrations.

#### `fenergo.properties`
- Fenergo API URLs (entity, logic engine, journey command)
- Authentication configuration
- Retry and timeout settings
- Tenant and journey type configuration

#### `apigee.properties`
- Apigee transformation endpoints
- Authentication configuration
- Rate limiting settings
- Retry configuration

### 3. Security (`config/security/`)

**Purpose**: Security-related configuration.

#### `security.properties`
- SSL/TLS configuration
- CORS settings
- Authentication and authorization
- API security
- Rate limiting
- Input validation

### 4. Monitoring (`config/monitoring/`)

**Purpose**: Observability and monitoring configuration.

#### `monitoring.properties`
- Metrics configuration (Prometheus, JMX)
- Health check settings
- Tracing configuration (Zipkin)
- Performance monitoring
- Alerting configuration
- Custom metrics

### 5. Features (`config/features/`)

**Purpose**: Feature-specific configuration.

#### `steps.properties`
- Step definition and execution order
- Retry configuration
- Timeout settings
- Async execution settings
- Validation configuration

#### `http-client.properties`
- RestClient timeout settings
- Connection pool configuration
- Retry and circuit breaker settings
- Logging configuration

#### `jackson.properties`
- JSON serialization/deserialization settings
- Date/time formatting
- Property naming strategies
- Property inclusion rules

### 6. Environments (`config/environments/`)

**Purpose**: Environment-specific overrides and configurations.

Each environment directory contains:
- `application-{env}.properties` - Environment-specific overrides

## 🔄 Configuration Loading Order

The configuration files are loaded in the following order:

1. **Core Configuration** (Always loaded)
   - `config/core/application-core.properties`
   - `config/core/database.properties`
   - `config/core/logging.properties`

2. **External Services Configuration**
   - `config/external/fenergo.properties`
   - `config/external/apigee.properties`

3. **Security Configuration**
   - `config/security/security.properties`

4. **Monitoring Configuration**
   - `config/monitoring/monitoring.properties`

5. **Features Configuration**
   - `config/features/steps.properties`
   - `config/features/http-client.properties`
   - `config/features/jackson.properties`

6. **Environment-Specific Configuration** (Profile-based)
   - `config/environments/{active-profile}/application-{active-profile}.properties`

## 🚀 Usage Examples

### Running with Different Environments

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Test
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Staging
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Production
java -jar banking-onboarding-service.jar --spring.profiles.active=prod
```

### Environment-Specific Overrides

Each environment can override any property from the base configuration:

**Development (`config/environments/dev/application-dev.properties`)**:
```properties
# Override logging for development
logging.level.com.banking.onboarding=DEBUG

# Override database for development
spring.data.mongodb.database=banking_onboarding_dev

# Override external service URLs
fenergo.entity.api.url=https://dev-fenergo.example.com/entity
```

**Production (`config/environments/prod/application-prod.properties`)**:
```properties
# Override security for production
security.ssl.enabled=true
security.auth.enabled=true

# Use environment variables for sensitive data
fenergo.auth.client-id=${FENERGO_CLIENT_ID}
fenergo.auth.client-secret=${FENERGO_CLIENT_SECRET}

# Override monitoring for production
monitoring.alerts.enabled=true
```

## 🔧 Configuration Management

### Adding New Configuration

1. **Core Configuration**: Add to appropriate file in `config/core/`
2. **External Service**: Add to appropriate file in `config/external/`
3. **Security**: Add to `config/security/security.properties`
4. **Monitoring**: Add to `config/monitoring/monitoring.properties`
5. **Feature**: Add to appropriate file in `config/features/`
6. **Environment-Specific**: Add to environment-specific file

### Best Practices

1. **Separation of Concerns**: Keep related configurations together
2. **Environment Variables**: Use for sensitive data in production
3. **Documentation**: Document all configuration properties
4. **Validation**: Validate configuration on startup
5. **Defaults**: Provide sensible defaults
6. **Override Order**: Environment-specific overrides base configuration

### Configuration Validation

The application validates configuration on startup:

```java
@ConfigurationProperties(prefix = "banking.onboarding")
@Validated
public class EnvironmentConfig {
    @NotBlank
    private String environment;
    
    @Min(1)
    @Max(65535)
    private int serverPort;
    
    // ... other properties
}
```

## 📊 Configuration Monitoring

### Viewing Configuration

```bash
# View all configuration properties
curl http://localhost:8080/actuator/configprops

# View environment-specific properties
curl http://localhost:8080/actuator/env

# View active profiles
curl http://localhost:8080/actuator/env | jq '.propertySources[].properties."spring.profiles.active"'
```

### Configuration Health Check

```bash
# Check configuration health
curl http://localhost:8080/actuator/health/config
```

## 🛠️ Troubleshooting

### Common Issues

1. **Configuration not loaded**: Check file path and naming
2. **Environment override not working**: Verify profile is active
3. **Property not found**: Check property name and prefix
4. **Validation errors**: Check property values and constraints

### Debug Commands

```bash
# Check loaded configuration
curl http://localhost:8080/actuator/configprops | jq '.contexts.application.beans'

# Check environment variables
curl http://localhost:8080/actuator/env | jq '.propertySources[]'

# Check active profiles
curl http://localhost:8080/actuator/env | jq '.propertySources[].properties."spring.profiles.active"'
```

## 📝 Migration Guide

### From Old Structure

If migrating from the old properties structure:

1. **Move files** to appropriate new directories
2. **Update imports** in `application.properties`
3. **Test each environment** to ensure configuration loads correctly
4. **Update documentation** to reflect new structure

### Example Migration

**Old Structure**:
```
src/main/resources/
├── application.properties
├── config/core/
│   ├── application-core.properties
│   ├── database.properties
│   └── logging.properties
└── config/external/
    └── fenergo.properties
```

**New Structure**:
```
src/main/resources/
├── application.properties
└── config/
    ├── core/
    │   ├── application-core.properties
    │   ├── database.properties
    │   └── logging.properties
    ├── external/
    │   └── fenergo.properties
    ├── security/
    │   └── security.properties
    ├── monitoring/
    │   └── monitoring.properties
    ├── features/
    │   ├── steps.properties
    │   ├── http-client.properties
    │   └── jackson.properties
    └── environments/
        ├── dev/
        ├── test/
        ├── staging/
        └── prod/
```

This organized structure provides better maintainability, clearer separation of concerns, and easier environment management.
