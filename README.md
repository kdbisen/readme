# Banking Onboarding Service

A Spring Boot microservice for banking entity onboarding with Fenergo integration.

## Features

- **Async Entity Processing**: Accepts XML data and processes entities asynchronously
- **Fenergo Integration**: Integrates with Fenergo API for entity onboarding
- **MongoDB Persistence**: Stores process data and entity information
- **Spring Integration**: Uses Spring Integration for async processing flows
- **Comprehensive Logging**: Detailed logging for monitoring and debugging
- **Error Handling**: Robust error handling with custom exceptions
- **Health Monitoring**: Health check endpoints for monitoring

## API Endpoints

### Process Entity
```
POST /api/v1/onboarding/process-entity/{requestType}
Content-Type: application/xml or application/json

<entity>
  <entityId>ENT-001</entityId>
  <entityName>Acme Corporation</entityName>
  ...
</entity>
```

Where `requestType` can be: `ADD_KYC`, `UPDATE_KYC`, `VERIFY_KYC`, `RENEW_KYC`, `SUSPEND_KYC`, `REACTIVATE_KYC`

### Get Process Status
```
GET /api/v1/onboarding/status/{processId}
```

### Get Process Status by Correlation ID
```
GET /api/v1/onboarding/status/correlation/{correlationId}
```

### Health Check
```
GET /api/v1/onboarding/health
```

## Process Flow

1. **Receive**: XML data is received via `/process-entity` endpoint
2. **Transform**: XML is transformed to JSON and EntityData object
3. **Validate**: Entity data is validated for completeness and compliance
4. **Fenergo**: Entity is submitted to Fenergo API for onboarding
5. **Complete**: Process status is updated and stored in MongoDB

## Configuration

### Development
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Production
```bash
java -jar banking-onboarding-service.jar --spring.profiles.active=prod
```

### Environment Variables (Production)
- `MONGODB_HOST`: MongoDB host
- `MONGODB_PORT`: MongoDB port
- `MONGODB_DATABASE`: MongoDB database name
- `MONGODB_USERNAME`: MongoDB username
- `MONGODB_PASSWORD`: MongoDB password
- `FENERGO_API_URL`: Fenergo API base URL
- `FENERGO_API_TIMEOUT`: Fenergo API timeout
- `PROCESS_TIMEOUT`: Process timeout
- `MAX_CONCURRENT_PROCESSES`: Maximum concurrent processes

## Dependencies

- Spring Boot 3.2.0
- Spring Integration 6.2.0
- Spring Data MongoDB
- Lombok
- Jackson (XML/JSON processing)
- WebFlux (HTTP client)
- Spring Boot Actuator

## Sample Usage

```bash
# Process an entity (ADD_KYC)
curl -X POST http://localhost:8080/api/v1/onboarding/process-entity/ADD_KYC \
  -H "Content-Type: application/xml" \
  -d '<entity><entityId>ENT-001</entityId><entityName>Acme Corp</entityName></entity>'

# Check process status
curl http://localhost:8080/api/v1/onboarding/status/{processId}

# Health check
curl http://localhost:8080/api/v1/onboarding/health
```

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/banking-onboarding-service-1.0.0.jar
```

## Testing

```bash
mvn test
```

## Monitoring

The service exposes several monitoring endpoints:

- `/actuator/health` - Health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/api/v1/onboarding/health` - Custom health check with process statistics
