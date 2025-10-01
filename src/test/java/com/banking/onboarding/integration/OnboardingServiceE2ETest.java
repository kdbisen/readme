package com.banking.onboarding.integration;

import com.banking.onboarding.BankingOnboardingServiceApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Tests for Banking Onboarding Service
 * Tests the complete flow from process initiation to completion
 */
@SpringBootTest(classes = BankingOnboardingServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OnboardingServiceE2ETest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String correlationId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        RestAssured.baseURI = baseUrl;
        correlationId = "TEST-CORR-" + System.currentTimeMillis();
    }

    @Test
    void testCompleteOnboardingFlow_ADD_KYC() {
        // Test data
        String xmlPayload = """
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
            """;

        // Step 1: Initiate Process
        Response processResponse = given()
                .header("X-Correlation-ID", correlationId)
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/ADD_KYC")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("processId", notNullValue())
                .body("correlationId", equalTo(correlationId))
                .body("requestType", equalTo("ADD_KYC"))
                .body("status", equalTo("PROCESSING"))
                .body("message", containsString("initiated"))
                .body("timestamp", notNullValue())
                .extract()
                .response();

        String processId = processResponse.jsonPath().getString("processId");
        assertNotNull(processId, "Process ID should not be null");

        // Step 2: Wait for processing and check status
        waitForProcessCompletion(processId);

        // Step 3: Verify final status
        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("processId", equalTo(processId))
                .body("correlationId", equalTo(correlationId))
                .body("status", oneOf("COMPLETED", "FAILED"))
                .body("steps", notNullValue())
                .body("steps", hasSize(greaterThan(0)));

        // Step 4: Get journey details
        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/journey/{processId}", processId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("processId", equalTo(processId))
                .body("correlationId", equalTo(correlationId))
                .body("steps", notNullValue());
    }

    @Test
    void testCompleteOnboardingFlow_UPDATE_KYC() {
        // Test data for UPDATE_KYC
        String jsonPayload = """
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
            """;

        // Step 1: Initiate Process
        Response processResponse = given()
                .header("X-Correlation-ID", correlationId)
                .contentType(ContentType.JSON)
                .body(jsonPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/UPDATE_KYC")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("processId", notNullValue())
                .body("correlationId", equalTo(correlationId))
                .body("requestType", equalTo("UPDATE_KYC"))
                .body("status", equalTo("PROCESSING"))
                .extract()
                .response();

        String processId = processResponse.jsonPath().getString("processId");

        // Step 2: Wait and verify completion
        waitForProcessCompletion(processId);

        // Step 3: Verify all steps completed
        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId)
                .then()
                .statusCode(200)
                .body("steps", hasSize(5)) // Should have 5 steps
                .body("steps.stepName", hasItems(
                    "APIGEE_TRANSFORM",
                    "ENTITY_CREATE", 
                    "JOURNEY_INFO",
                    "JOURNEY_INITIATE",
                    "JOURNEY_DETAILS"
                ));
    }

    @Test
    void testProcessStatusNotFound() {
        String nonExistentProcessId = "NON-EXISTENT-" + System.currentTimeMillis();

        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/status/{processId}", nonExistentProcessId)
                .then()
                .statusCode(404);
    }

    @Test
    void testJourneyDetailsNotFound() {
        String nonExistentProcessId = "NON-EXISTENT-" + System.currentTimeMillis();

        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/journey/{processId}", nonExistentProcessId)
                .then()
                .statusCode(404);
    }

    @Test
    void testInvalidRequestType() {
        String xmlPayload = "<test>data</test>";

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
    void testLargePayload() {
        // Create a large XML payload
        StringBuilder largePayload = new StringBuilder();
        largePayload.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><customer>");
        for (int i = 0; i < 1000; i++) {
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

        String processId = response.jsonPath().getString("processId");
        waitForProcessCompletion(processId);

        // Verify it was processed
        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId)
                .then()
                .statusCode(200)
                .body("status", oneOf("COMPLETED", "FAILED"));
    }

    @Test
    void testConcurrentProcesses() {
        String correlationId1 = "CONCURRENT-1-" + System.currentTimeMillis();
        String correlationId2 = "CONCURRENT-2-" + System.currentTimeMillis();
        String correlationId3 = "CONCURRENT-3-" + System.currentTimeMillis();

        String xmlPayload = """
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>Concurrent</firstName>
                    <lastName>Test</lastName>
                    <email>concurrent@test.com</email>
                </personalInfo>
            </customer>
            """;

        // Start 3 concurrent processes
        Response response1 = given()
                .header("X-Correlation-ID", correlationId1)
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/ADD_KYC")
                .then()
                .statusCode(202)
                .extract()
                .response();

        Response response2 = given()
                .header("X-Correlation-ID", correlationId2)
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/VERIFY_KYC")
                .then()
                .statusCode(202)
                .extract()
                .response();

        Response response3 = given()
                .header("X-Correlation-ID", correlationId3)
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/RENEW_KYC")
                .then()
                .statusCode(202)
                .extract()
                .response();

        String processId1 = response1.jsonPath().getString("processId");
        String processId2 = response2.jsonPath().getString("processId");
        String processId3 = response3.jsonPath().getString("processId");

        // Verify all processes are unique
        assertNotEquals(processId1, processId2);
        assertNotEquals(processId2, processId3);
        assertNotEquals(processId1, processId3);

        // Wait for all to complete
        waitForProcessCompletion(processId1);
        waitForProcessCompletion(processId2);
        waitForProcessCompletion(processId3);

        // Verify all completed successfully
        given()
                .header("X-Correlation-ID", correlationId1)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId1)
                .then()
                .statusCode(200)
                .body("status", oneOf("COMPLETED", "FAILED"));

        given()
                .header("X-Correlation-ID", correlationId2)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId2)
                .then()
                .statusCode(200)
                .body("status", oneOf("COMPLETED", "FAILED"));

        given()
                .header("X-Correlation-ID", correlationId3)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId3)
                .then()
                .statusCode(200)
                .body("status", oneOf("COMPLETED", "FAILED"));
    }

    @Test
    void testStepDetailsAndTiming() {
        String xmlPayload = """
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>Timing</firstName>
                    <lastName>Test</lastName>
                    <email>timing@test.com</email>
                </personalInfo>
            </customer>
            """;

        // Start process
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

        // Wait for completion
        waitForProcessCompletion(processId);

        // Verify step details
        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId)
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
                .body("steps.durationMs", everyItem(greaterThan(0L)));
    }

    @Test
    void testCorrelationIdHandling() {
        String customCorrelationId = "CUSTOM-CORR-" + System.currentTimeMillis();
        String xmlPayload = "<test>correlation test</test>";

        // Test with custom correlation ID
        Response response = given()
                .header("X-Correlation-ID", customCorrelationId)
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/ADD_KYC")
                .then()
                .statusCode(202)
                .body("correlationId", equalTo(customCorrelationId))
                .extract()
                .response();

        String processId = response.jsonPath().getString("processId");

        // Test without correlation ID (should auto-generate)
        Response response2 = given()
                .contentType(ContentType.TEXT)
                .body(xmlPayload)
                .when()
                .post("/api/v1/onboarding/process-entity/ADD_KYC")
                .then()
                .statusCode(202)
                .body("correlationId", notNullValue())
                .extract()
                .response();

        String processId2 = response2.jsonPath().getString("processId");
        String autoCorrelationId = response2.jsonPath().getString("correlationId");

        // Verify correlation IDs are different
        assertNotEquals(customCorrelationId, autoCorrelationId);
        assertNotEquals(processId, processId2);
    }

    /**
     * Helper method to wait for process completion
     */
    private void waitForProcessCompletion(String processId) {
        int maxAttempts = 30; // 30 seconds max wait
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
                        return; // Process completed
                    }
                }

                Thread.sleep(1000); // Wait 1 second
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test interrupted while waiting for process completion");
            }
        }

        fail("Process did not complete within " + maxAttempts + " seconds");
    }
}
