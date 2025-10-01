# RestAssured End-to-End Test Suite Documentation

## Overview

This document describes the comprehensive RestAssured test suite for the Banking Onboarding Service. The test suite covers all API endpoints, request types, error scenarios, and performance testing.

## Test Structure

### Test Classes

1. **`OnboardingServiceE2ETest`** - Main end-to-end integration tests
2. **`OnboardingServiceComprehensiveTest`** - Comprehensive test coverage with nested test classes
3. **`TestUtils`** - Utility class for common test operations
4. **`TestDataFactory`** - Factory for creating test data

### Test Categories

#### 1. End-to-End Flow Tests
- Complete onboarding flow for all request types
- Process initiation to completion
- Step-by-step validation
- Journey details verification

#### 2. Request Type Tests
- `ADD_KYC` - Add new KYC information
- `UPDATE_KYC` - Update existing KYC information
- `VERIFY_KYC` - Verify KYC information
- `RENEW_KYC` - Renew KYC information
- `SUSPEND_KYC` - Suspend KYC information
- `REACTIVATE_KYC` - Reactivate KYC information

#### 3. Error Handling Tests
- Invalid request types
- Empty/null payloads
- Malformed XML/JSON
- Large payload handling
- Network timeout scenarios

#### 4. Performance Tests
- Response time validation
- Concurrent process creation
- Load testing scenarios
- Memory usage validation

#### 5. Data Validation Tests
- Special characters handling
- Complex nested objects
- Unicode support
- Data type validation

#### 6. Step Validation Tests
- Step execution order
- Step timing information
- Step status validation
- Error step handling

## Test Configuration

### Dependencies

```xml
<!-- RestAssured for API Testing -->
<dependency>
    <groupId>io.restassured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.2</version>
    <scope>test</scope>
</dependency>

<!-- TestContainers for Integration Testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>

<!-- Awaitility for async testing -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <version>4.2.0</version>
    <scope>test</scope>
</dependency>
```

### Test Properties

```properties
# Test Configuration
server.port=0
spring.data.mongodb.uri=mongodb://localhost:27017/banking_onboarding_test
logging.level.com.banking.onboarding=DEBUG
test.max-wait-time=30000
test.poll-interval=1000
```

## Test Execution

### Running Tests

```bash
# Run all tests
mvn test

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Run specific test class
mvn test -Dtest="OnboardingServiceE2ETest"

# Run with specific profile
mvn test -Dspring.profiles.active=test
```

### Test Reports

The tests generate detailed reports including:
- Test execution results
- Performance metrics
- Error details
- Coverage reports

## Test Scenarios

### 1. Complete Onboarding Flow

```java
@Test
void testCompleteOnboardingFlow_ADD_KYC() {
    // 1. Initiate Process
    Response processResponse = given()
            .header("X-Correlation-ID", correlationId)
            .contentType(ContentType.TEXT)
            .body(xmlPayload)
            .when()
            .post("/api/v1/onboarding/process-entity/ADD_KYC")
            .then()
            .statusCode(202)
            .body("processId", notNullValue())
            .body("status", equalTo("PROCESSING"))
            .extract()
            .response();

    // 2. Wait for completion
    waitForProcessCompletion(processId);

    // 3. Verify final status
    given()
            .header("X-Correlation-ID", correlationId)
            .when()
            .get("/api/v1/onboarding/status/{processId}", processId)
            .then()
            .statusCode(200)
            .body("status", oneOf("COMPLETED", "FAILED"));
}
```

### 2. Error Handling

```java
@Test
void testInvalidRequestType() {
    given()
            .header("X-Correlation-ID", correlationId)
            .contentType(ContentType.TEXT)
            .body(xmlPayload)
            .when()
            .post("/api/v1/onboarding/process-entity/INVALID_TYPE")
            .then()
            .statusCode(400);
}
```

### 3. Performance Testing

```java
@Test
void testProcessInitiationResponseTime() {
    long startTime = System.currentTimeMillis();
    
    given()
            .header("X-Correlation-ID", correlationId)
            .contentType(ContentType.TEXT)
            .body(xmlPayload)
            .when()
            .post("/api/v1/onboarding/process-entity/ADD_KYC")
            .then()
            .statusCode(202)
            .time(lessThan(2000L)); // Should respond within 2 seconds

    long responseTime = System.currentTimeMillis() - startTime;
    assertTrue(responseTime < 2000, "Process initiation should be fast");
}
```

### 4. Concurrent Testing

```java
@Test
void testConcurrentProcessCreation() {
    int concurrentProcesses = 10;
    String[] processIds = new String[concurrentProcesses];

    // Create multiple processes concurrently
    for (int i = 0; i < concurrentProcesses; i++) {
        Response response = given()
                .header("X-Correlation-ID", correlationId + "-" + i)
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/ADD_KYC")
                .then()
                .statusCode(202)
                .extract()
                .response();

        processIds[i] = response.jsonPath().getString("processId");
    }

    // Verify all process IDs are unique
    // Wait for all processes to complete
}
```

## Test Data

### XML Test Data

```xml
<?xml version="1.0" encoding="UTF-8"?>
<customer>
    <personalInfo>
        <firstName>John</firstName>
        <lastName>Doe</lastName>
        <email>john.doe@example.com</email>
        <phone>+1234567890</phone>
    </personalInfo>
    <address>
        <street>123 Main St</street>
        <city>New York</city>
        <state>NY</state>
        <zipCode>10001</zipCode>
        <country>USA</country>
    </address>
    <kycInfo>
        <documentType>PASSPORT</documentType>
        <documentNumber>P123456789</documentNumber>
        <issueDate>2020-01-01</issueDate>
        <expiryDate>2030-01-01</expiryDate>
    </kycInfo>
</customer>
```

### JSON Test Data

```json
{
    "customerId": "CUST-12345",
    "personalInfo": {
        "firstName": "Jane",
        "lastName": "Smith",
        "email": "jane.smith@example.com",
        "phone": "+1987654321"
    },
    "address": {
        "street": "456 Oak Ave",
        "city": "Los Angeles",
        "state": "CA",
        "zipCode": "90210",
        "country": "USA"
    },
    "kycInfo": {
        "documentType": "DRIVER_LICENSE",
        "documentNumber": "DL987654321",
        "issueDate": "2021-06-15",
        "expiryDate": "2026-06-15"
    }
}
```

## Assertions

### Response Validation

```java
.then()
    .statusCode(202)
    .contentType(ContentType.JSON)
    .body("processId", notNullValue())
    .body("correlationId", equalTo(correlationId))
    .body("requestType", equalTo("ADD_KYC"))
    .body("status", equalTo("PROCESSING"))
    .body("message", containsString("initiated"))
    .body("timestamp", notNullValue())
```

### Step Validation

```java
.then()
    .statusCode(200)
    .body("steps", hasSize(5))
    .body("steps.stepOrder", contains(1, 2, 3, 4, 5))
    .body("steps.stepName", contains(
        "APIGEE_TRANSFORM",
        "ENTITY_CREATE",
        "JOURNEY_INFO",
        "JOURNEY_INITIATE",
        "JOURNEY_DETAILS"
    ))
    .body("steps.status", everyItem(oneOf("COMPLETED", "FAILED")))
    .body("steps.durationMs", everyItem(greaterThan(0L)))
```

## Best Practices

### 1. Test Isolation
- Each test uses unique correlation IDs
- Tests don't depend on each other
- Clean up after each test

### 2. Data Management
- Use factory methods for test data creation
- Generate unique identifiers
- Handle special characters properly

### 3. Error Handling
- Test both success and failure scenarios
- Validate error responses
- Test timeout scenarios

### 4. Performance Testing
- Measure response times
- Test concurrent scenarios
- Validate resource usage

### 5. Maintainability
- Use utility classes for common operations
- Create reusable test data
- Document test scenarios clearly

## Troubleshooting

### Common Issues

1. **Test Timeouts**
   - Increase wait time in test configuration
   - Check service performance
   - Verify async processing

2. **Data Conflicts**
   - Use unique identifiers
   - Clean up test data
   - Isolate test environments

3. **Environment Issues**
   - Verify MongoDB connection
   - Check service availability
   - Validate configuration

### Debug Tips

1. Enable debug logging
2. Use correlation IDs for tracing
3. Check test reports for details
4. Verify test data validity

## Conclusion

This comprehensive test suite ensures the Banking Onboarding Service works correctly across all scenarios, providing confidence in the system's reliability and performance. The tests cover end-to-end flows, error handling, performance validation, and data integrity.
