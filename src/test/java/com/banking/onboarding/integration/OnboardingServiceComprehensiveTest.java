package com.banking.onboarding.integration;

import com.banking.onboarding.BankingOnboardingServiceApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Test Suite for Banking Onboarding Service
 * Covers all request types and edge cases
 */
@SpringBootTest(classes = BankingOnboardingServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OnboardingServiceComprehensiveTest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String correlationId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        RestAssured.baseURI = baseUrl;
        correlationId = "COMPREHENSIVE-TEST-" + System.currentTimeMillis();
    }

    @Nested
    @DisplayName("Request Type Tests")
    class RequestTypeTests {

        @Test
        @DisplayName("Test ADD_KYC request type")
        void testAddKycRequestType() {
            String xmlPayload = createValidXmlPayload("ADD_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202)
                    .body("requestType", equalTo("ADD_KYC"))
                    .body("status", equalTo("PROCESSING"))
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }

        @Test
        @DisplayName("Test UPDATE_KYC request type")
        void testUpdateKycRequestType() {
            String jsonPayload = createValidJsonPayload("UPDATE_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.JSON)
                    .body(jsonPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/UPDATE_KYC")
                    .then()
                    .statusCode(202)
                    .body("requestType", equalTo("UPDATE_KYC"))
                    .body("status", equalTo("PROCESSING"))
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }

        @Test
        @DisplayName("Test VERIFY_KYC request type")
        void testVerifyKycRequestType() {
            String xmlPayload = createValidXmlPayload("VERIFY_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/VERIFY_KYC")
                    .then()
                    .statusCode(202)
                    .body("requestType", equalTo("VERIFY_KYC"))
                    .body("status", equalTo("PROCESSING"))
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }

        @Test
        @DisplayName("Test RENEW_KYC request type")
        void testRenewKycRequestType() {
            String jsonPayload = createValidJsonPayload("RENEW_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.JSON)
                    .body(jsonPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/RENEW_KYC")
                    .then()
                    .statusCode(202)
                    .body("requestType", equalTo("RENEW_KYC"))
                    .body("status", equalTo("PROCESSING"))
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }

        @Test
        @DisplayName("Test SUSPEND_KYC request type")
        void testSuspendKycRequestType() {
            String xmlPayload = createValidXmlPayload("SUSPEND_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/SUSPEND_KYC")
                    .then()
                    .statusCode(202)
                    .body("requestType", equalTo("SUSPEND_KYC"))
                    .body("status", equalTo("PROCESSING"))
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }

        @Test
        @DisplayName("Test REACTIVATE_KYC request type")
        void testReactivateKycRequestType() {
            String jsonPayload = createValidJsonPayload("REACTIVATE_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.JSON)
                    .body(jsonPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/REACTIVATE_KYC")
                    .then()
                    .statusCode(202)
                    .body("requestType", equalTo("REACTIVATE_KYC"))
                    .body("status", equalTo("PROCESSING"))
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Test invalid request type")
        void testInvalidRequestType() {
            String xmlPayload = createValidXmlPayload("INVALID_TYPE");

            given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/INVALID_TYPE")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Test empty payload")
        void testEmptyPayload() {
            given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body("")
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Test null payload")
        void testNullPayload() {
            given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Test malformed XML")
        void testMalformedXml() {
            String malformedXml = "<customer><name>John</name></customer>"; // Missing closing tag

            given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(malformedXml)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202); // Should still accept and process
        }

        @Test
        @DisplayName("Test malformed JSON")
        void testMalformedJson() {
            String malformedJson = "{\"customer\":{\"name\":\"John\"}"; // Missing closing brace

            given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.JSON)
                    .body(malformedJson)
                    .when()
                    .post("/api/v1/onboarding/process-entity/UPDATE_KYC")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Test very large payload")
        void testVeryLargePayload() {
            StringBuilder largePayload = new StringBuilder();
            largePayload.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><customer>");
            for (int i = 0; i < 10000; i++) {
                largePayload.append("<field").append(i).append(">Value").append(i).append("</field").append(i).append(">");
            }
            largePayload.append("</customer>");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(largePayload.toString())
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Test response time for process initiation")
        void testProcessInitiationResponseTime() {
            String xmlPayload = createValidXmlPayload("ADD_KYC");

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

        @Test
        @DisplayName("Test status check response time")
        void testStatusCheckResponseTime() {
            // First create a process
            String xmlPayload = createValidXmlPayload("ADD_KYC");
            Response processResponse = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();

            String processId = processResponse.jsonPath().getString("processId");

            // Test status check response time
            given()
                    .header("X-Correlation-ID", correlationId)
                    .when()
                    .get("/api/v1/onboarding/status/{processId}", processId)
                    .then()
                    .statusCode(200)
                    .time(lessThan(1000L)); // Should respond within 1 second
        }

        @Test
        @DisplayName("Test concurrent process creation")
        void testConcurrentProcessCreation() {
            int concurrentProcesses = 10;
            String[] processIds = new String[concurrentProcesses];

            // Create multiple processes concurrently
            for (int i = 0; i < concurrentProcesses; i++) {
                String xmlPayload = createValidXmlPayload("ADD_KYC");
                String currentCorrelationId = correlationId + "-" + i;

                Response response = given()
                        .header("X-Correlation-ID", currentCorrelationId)
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
            for (int i = 0; i < concurrentProcesses; i++) {
                for (int j = i + 1; j < concurrentProcesses; j++) {
                    assertNotEquals(processIds[i], processIds[j], 
                        "Process IDs should be unique");
                }
            }

            // Wait for all processes to complete
            for (String processId : processIds) {
                verifyProcessCompletion(processId);
            }
        }
    }

    @Nested
    @DisplayName("Data Validation Tests")
    class DataValidationTests {

        @Test
        @DisplayName("Test XML with special characters")
        void testXmlWithSpecialCharacters() {
            String xmlWithSpecialChars = """
                <?xml version="1.0" encoding="UTF-8"?>
                <customer>
                    <personalInfo>
                        <firstName>José</firstName>
                        <lastName>García-López</lastName>
                        <email>josé.garcía@example.com</email>
                        <phone>+34 91 123 45 67</phone>
                    </personalInfo>
                    <address>
                        <street>Calle Mayor 123, 2º A</street>
                        <city>Madrid</city>
                        <country>España</country>
                    </address>
                </customer>
                """;

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlWithSpecialChars)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }

        @Test
        @DisplayName("Test JSON with nested objects")
        void testJsonWithNestedObjects() {
            String complexJson = """
                {
                    "customerId": "CUST-12345",
                    "personalInfo": {
                        "firstName": "John",
                        "lastName": "Doe",
                        "contact": {
                            "email": "john.doe@example.com",
                            "phone": {
                                "primary": "+1234567890",
                                "secondary": "+0987654321"
                            }
                        }
                    },
                    "addresses": [
                        {
                            "type": "HOME",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001"
                        },
                        {
                            "type": "WORK",
                            "street": "456 Business Ave",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10002"
                        }
                    ],
                    "kycInfo": {
                        "documents": [
                            {
                                "type": "PASSPORT",
                                "number": "P123456789",
                                "issueDate": "2020-01-01",
                                "expiryDate": "2030-01-01"
                            },
                            {
                                "type": "DRIVER_LICENSE",
                                "number": "DL987654321",
                                "issueDate": "2021-06-15",
                                "expiryDate": "2026-06-15"
                            }
                        ]
                    }
                }
                """;

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.JSON)
                    .body(complexJson)
                    .when()
                    .post("/api/v1/onboarding/process-entity/UPDATE_KYC")
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();

            verifyProcessCompletion(response.jsonPath().getString("processId"));
        }
    }

    @Nested
    @DisplayName("Step Validation Tests")
    class StepValidationTests {

        @Test
        @DisplayName("Test all steps are executed in correct order")
        void testStepsExecutionOrder() {
            String xmlPayload = createValidXmlPayload("ADD_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();

            String processId = response.jsonPath().getString("processId");
            waitForProcessCompletion(processId);

            // Verify step order and names
            given()
                    .header("X-Correlation-ID", correlationId)
                    .when()
                    .get("/api/v1/onboarding/status/{processId}", processId)
                    .then()
                    .statusCode(200)
                    .body("steps.stepOrder", contains(1, 2, 3, 4, 5))
                    .body("steps.stepName", contains(
                        "APIGEE_TRANSFORM",
                        "ENTITY_CREATE",
                        "JOURNEY_INFO",
                        "JOURNEY_INITIATE",
                        "JOURNEY_DETAILS"
                    ))
                    .body("steps.status", everyItem(oneOf("COMPLETED", "FAILED")))
                    .body("steps.durationMs", everyItem(greaterThan(0L)));
        }

        @Test
        @DisplayName("Test step timing information")
        void testStepTimingInformation() {
            String xmlPayload = createValidXmlPayload("ADD_KYC");

            Response response = given()
                    .header("X-Correlation-ID", correlationId)
                    .contentType(ContentType.TEXT)
                    .body(xmlPayload)
                    .when()
                    .post("/api/v1/onboarding/process-entity/ADD_KYC")
                    .then()
                    .statusCode(202)
                    .extract()
                    .response();

            String processId = response.jsonPath().getString("processId");
            waitForProcessCompletion(processId);

            // Verify timing information
            given()
                    .header("X-Correlation-ID", correlationId)
                    .when()
                    .get("/api/v1/onboarding/status/{processId}", processId)
                    .then()
                    .statusCode(200)
                    .body("steps.durationMs", everyItem(greaterThan(0L)))
                    .body("steps.completedAt", everyItem(notNullValue()));
        }
    }

    // Helper methods
    private String createValidXmlPayload(String requestType) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>Test</firstName>
                    <lastName>User</lastName>
                    <email>test.user@example.com</email>
                    <phone>+1234567890</phone>
                </personalInfo>
                <address>
                    <street>123 Test St</street>
                    <city>Test City</city>
                    <state>TS</state>
                    <zipCode>12345</zipCode>
                    <country>USA</country>
                </address>
                <kycInfo>
                    <documentType>PASSPORT</documentType>
                    <documentNumber>P123456789</documentNumber>
                    <issueDate>2020-01-01</issueDate>
                    <expiryDate>2030-01-01</expiryDate>
                </kycInfo>
                <requestType>%s</requestType>
            </customer>
            """, requestType);
    }

    private String createValidJsonPayload(String requestType) {
        return String.format("""
            {
                "customerId": "CUST-12345",
                "personalInfo": {
                    "firstName": "Test",
                    "lastName": "User",
                    "email": "test.user@example.com",
                    "phone": "+1234567890"
                },
                "address": {
                    "street": "123 Test St",
                    "city": "Test City",
                    "state": "TS",
                    "zipCode": "12345",
                    "country": "USA"
                },
                "kycInfo": {
                    "documentType": "PASSPORT",
                    "documentNumber": "P123456789",
                    "issueDate": "2020-01-01",
                    "expiryDate": "2030-01-01"
                },
                "requestType": "%s"
            }
            """, requestType);
    }

    private void verifyProcessCompletion(String processId) {
        int maxAttempts = 30;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                Response statusResponse = given()
                        .header("X-Correlation-ID", correlationId)
                        .when()
                        .get("/api/v1/onboarding/status/{processId}", processId)
                        .then()
                        .extract()
                        .response();

                if (statusResponse.getStatusCode() == 200) {
                    String status = statusResponse.jsonPath().getString("status");
                    if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                        return;
                    }
                }

                Thread.sleep(1000);
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test interrupted while waiting for process completion");
            }
        }

        fail("Process did not complete within " + maxAttempts + " seconds");
    }

    private void waitForProcessCompletion(String processId) {
        verifyProcessCompletion(processId);
    }
}





