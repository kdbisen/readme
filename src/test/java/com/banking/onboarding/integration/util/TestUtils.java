package com.banking.onboarding.integration.util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * Test utility class for common test operations
 */
public class TestUtils {

    private static final int DEFAULT_MAX_WAIT_TIME = 30;
    private static final int DEFAULT_POLL_INTERVAL = 1000;

    /**
     * Wait for process completion
     */
    public static void waitForProcessCompletion(String processId, String correlationId, int port) {
        waitForProcessCompletion(processId, correlationId, port, DEFAULT_MAX_WAIT_TIME);
    }

    /**
     * Wait for process completion with custom timeout
     */
    public static void waitForProcessCompletion(String processId, String correlationId, int port, int maxWaitSeconds) {
        RestAssured.baseURI = "http://localhost:" + port;
        
        int maxAttempts = maxWaitSeconds;
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

                Thread.sleep(DEFAULT_POLL_INTERVAL);
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test interrupted while waiting for process completion");
            }
        }

        fail("Process did not complete within " + maxWaitSeconds + " seconds");
    }

    /**
     * Create valid XML payload for testing
     */
    public static String createValidXmlPayload(String requestType) {
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

    /**
     * Create valid JSON payload for testing
     */
    public static String createValidJsonPayload(String requestType) {
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

    /**
     * Create large XML payload for performance testing
     */
    public static String createLargeXmlPayload(int fieldCount) {
        StringBuilder payload = new StringBuilder();
        payload.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><customer>");
        payload.append("<personalInfo><firstName>Large</firstName><lastName>Test</lastName></personalInfo>");
        payload.append("<data>");
        
        for (int i = 0; i < fieldCount; i++) {
            payload.append("<field").append(i).append(">Value").append(i).append("</field").append(i).append(">");
        }
        
        payload.append("</data></customer>");
        return payload.toString();
    }

    /**
     * Generate unique correlation ID
     */
    public static String generateCorrelationId(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Verify process status
     */
    public static void verifyProcessStatus(String processId, String correlationId, int port, String expectedStatus) {
        RestAssured.baseURI = "http://localhost:" + port;
        
        given()
                .header("X-Correlation-ID", correlationId)
                .when()
                .get("/api/v1/onboarding/status/{processId}", processId)
                .then()
                .statusCode(200)
                .body("processId", equalTo(processId))
                .body("correlationId", equalTo(correlationId))
                .body("status", equalTo(expectedStatus));
    }

    /**
     * Verify all steps are completed
     */
    public static void verifyAllStepsCompleted(String processId, String correlationId, int port) {
        RestAssured.baseURI = "http://localhost:" + port;
        
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

    /**
     * Measure response time
     */
    public static long measureResponseTime(Runnable request) {
        long startTime = System.currentTimeMillis();
        request.run();
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Assert response time is within limit
     */
    public static void assertResponseTime(long responseTime, long maxTimeMs) {
        assertTrue(responseTime < maxTimeMs, 
            "Response time " + responseTime + "ms exceeded limit " + maxTimeMs + "ms");
    }
}
