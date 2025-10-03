# Multi-Environment Configuration Guide

This banking onboarding service supports multiple environments with environment-specific configurations.

## 🌍 Supported Environments

- **Development (dev)** - Local development environment
- **Test (test)** - Testing environment
- **Staging (staging)** - Pre-production environment
- **Production (prod)** - Production environment

## 🚀 How to Run Different Environments

### Development Environment
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Using JAR
java -jar banking-onboarding-service.jar --spring.profiles.active=dev

# Using Environment Variable
export SPRING_PROFILES_ACTIVE=dev
java -jar banking-onboarding-service.jar
```

### Test Environment
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Using JAR
java -jar banking-onboarding-service.jar --spring.profiles.active=test
```

### Staging Environment
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Using JAR
java -jar banking-onboarding-service.jar --spring.profiles.active=staging
```

### Production Environment
```bash
# Using JAR
java -jar banking-onboarding-service.jar --spring.profiles.active=prod

# With environment variables for sensitive data
export APIGEE_CLIENT_ID=your-prod-client-id
export APIGEE_CLIENT_SECRET=your-prod-client-secret
export FENERGO_CLIENT_ID=your-prod-fenergo-client-id
export FENERGO_CLIENT_SECRET=your-prod-fenergo-client-secret
java -jar banking-onboarding-service.jar --spring.profiles.active=prod
```

## 📁 Configuration Structure

```
src/main/resources/
├── application.properties                 # Main configuration
├── environments/                          # Environment-specific configs
│   ├── dev/
│   │   └── application-dev.properties    # Development config
│   ├── test/
│   │   └── application-test.properties   # Test config
│   ├── staging/
│   │   └── application-staging.properties # Staging config
│   └── prod/
│       └── application-prod.properties   # Production config
├── config/core/                          # Core configuration
│   ├── application-core.properties      # Server config
│   ├── database.properties              # MongoDB config
│   └── logging.properties               # Logging config
└── ... (other fallback properties)
```

## 🔧 Configuration Properties

### Environment-Specific Properties

Each environment has its own properties file with:

- **Server Configuration**: Port, context path
- **Database Configuration**: Host, port, database name
- **Logging Configuration**: Log levels, logback config
- **External Services**: Fenergo, Apigee URLs and credentials
- **Step Configuration**: Retry settings, timeouts
- **Security Configuration**: SSL, certificates (prod only)
- **Monitoring Configuration**: Metrics, health checks

### Key Differences Between Environments

| Property | Development | Test | Staging | Production |
|----------|-------------|------|---------|------------|
| **Log Level** | DEBUG | INFO | INFO | WARN |
| **Retry Attempts** | 3 | 2 | 3 | 5 |
| **Timeout** | 30s | 20s | 30s | 60s |
| **SSL** | Disabled | Disabled | Enabled | Enabled |
| **Credentials** | Hardcoded | Hardcoded | Hardcoded | Environment Variables |

## 🔐 Security Considerations

### Production Environment
- Uses environment variables for sensitive credentials
- SSL/TLS enabled
- Certificate validation enabled
- Restricted logging levels
- Health check endpoints secured

### Development Environment
- Debug logging enabled
- Mock credentials for testing
- SSL disabled for local development
- All endpoints exposed for debugging

## 🐳 Docker Support

### Using Docker with Environment Profiles

```dockerfile
# Dockerfile
FROM openjdk:17-jre-slim
COPY target/banking-onboarding-service.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Run with different environments
docker run -e SPRING_PROFILES_ACTIVE=dev banking-onboarding-service
docker run -e SPRING_PROFILES_ACTIVE=test banking-onboarding-service
docker run -e SPRING_PROFILES_ACTIVE=staging banking-onboarding-service
docker run -e SPRING_PROFILES_ACTIVE=prod \
  -e APIGEE_CLIENT_ID=your-client-id \
  -e APIGEE_CLIENT_SECRET=your-client-secret \
  banking-onboarding-service
```

## 🔍 Environment Detection

The service automatically detects the environment and logs relevant information:

```
=== Environment Information ===
Active Profile: dev
Active Profiles: [dev]
Environment: development
App Version: 1.0.0-dev
Is Development: true
Is Test: false
Is Staging: false
Is Production: false
================================
```

## 🛠️ Customizing Environments

### Adding a New Environment

1. Create a new directory: `src/main/resources/environments/your-env/`
2. Create properties file: `application-your-env.properties`
3. Add environment-specific configuration
4. Update `application.properties` if needed
5. Create profile configuration class if needed

### Modifying Existing Environment

1. Edit the environment-specific properties file
2. Restart the application with the profile
3. Check logs for configuration validation

## 📊 Monitoring and Health Checks

### Environment-Specific Monitoring

- **Development**: Full debug information, all endpoints exposed
- **Test**: Basic monitoring, test-specific endpoints
- **Staging**: Production-like monitoring, limited endpoints
- **Production**: Minimal monitoring, secured endpoints only

### Health Check Endpoints

- `/actuator/health` - Basic health check
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics (prod only)

## 🚨 Troubleshooting

### Common Issues

1. **Profile not found**: Check if the properties file exists in the correct directory
2. **Configuration not loaded**: Verify the `spring.config.import` in `application.properties`
3. **Environment variables not set**: Ensure environment variables are set for production
4. **SSL issues**: Check certificate configuration for staging/production

### Debug Commands

```bash
# Check active profiles
curl http://localhost:8080/actuator/env | jq '.propertySources[].properties."spring.profiles.active"'

# Check configuration
curl http://localhost:8080/actuator/configprops | jq '.contexts.application.beans.environmentConfig'

# Check health
curl http://localhost:8080/actuator/health
```

## 📝 Best Practices

1. **Never commit sensitive data** to version control
2. **Use environment variables** for production credentials
3. **Test all environments** before deployment
4. **Monitor environment-specific logs** for issues
5. **Keep environment configurations** in sync
6. **Document environment-specific requirements**
7. **Use consistent naming conventions** for profiles
8. **Validate configurations** on startup
