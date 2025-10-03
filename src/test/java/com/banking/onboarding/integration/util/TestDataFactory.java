package com.banking.onboarding.integration.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test data factory for creating test data
 */
public class TestDataFactory {

    /**
     * Create customer XML data
     */
    public static String createCustomerXml(String firstName, String lastName, String email, String requestType) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>%s</firstName>
                    <lastName>%s</lastName>
                    <email>%s</email>
                    <phone>+1234567890</phone>
                    <dateOfBirth>1990-01-01</dateOfBirth>
                </personalInfo>
                <address>
                    <street>123 Main Street</street>
                    <city>New York</city>
                    <state>NY</state>
                    <zipCode>10001</zipCode>
                    <country>USA</country>
                </address>
                <kycInfo>
                    <documentType>PASSPORT</documentType>
                    <documentNumber>P%s</documentNumber>
                    <issueDate>2020-01-01</issueDate>
                    <expiryDate>2030-01-01</expiryDate>
                    <issuingCountry>USA</issuingCountry>
                </kycInfo>
                <requestType>%s</requestType>
                <metadata>
                    <source>TEST</source>
                    <timestamp>%d</timestamp>
                </metadata>
            </customer>
            """, firstName, lastName, email, UUID.randomUUID().toString().substring(0, 8), 
                 requestType, System.currentTimeMillis());
    }

    /**
     * Create customer JSON data
     */
    public static String createCustomerJson(String firstName, String lastName, String email, String requestType) {
        return String.format("""
            {
                "customerId": "CUST-%s",
                "personalInfo": {
                    "firstName": "%s",
                    "lastName": "%s",
                    "email": "%s",
                    "phone": "+1234567890",
                    "dateOfBirth": "1990-01-01"
                },
                "address": {
                    "street": "123 Main Street",
                    "city": "New York",
                    "state": "NY",
                    "zipCode": "10001",
                    "country": "USA"
                },
                "kycInfo": {
                    "documentType": "PASSPORT",
                    "documentNumber": "P%s",
                    "issueDate": "2020-01-01",
                    "expiryDate": "2030-01-01",
                    "issuingCountry": "USA"
                },
                "requestType": "%s",
                "metadata": {
                    "source": "TEST",
                    "timestamp": %d
                }
            }
            """, UUID.randomUUID().toString().substring(0, 8), firstName, lastName, email,
                 UUID.randomUUID().toString().substring(0, 8), requestType, System.currentTimeMillis());
    }

    /**
     * Create complex customer data with multiple addresses
     */
    public static String createComplexCustomerJson(String requestType) {
        return String.format("""
            {
                "customerId": "CUST-%s",
                "personalInfo": {
                    "firstName": "Complex",
                    "lastName": "Customer",
                    "email": "complex.customer@example.com",
                    "phone": "+1234567890",
                    "dateOfBirth": "1985-05-15",
                    "nationality": "US",
                    "maritalStatus": "SINGLE"
                },
                "addresses": [
                    {
                        "type": "HOME",
                        "street": "123 Home Street",
                        "city": "New York",
                        "state": "NY",
                        "zipCode": "10001",
                        "country": "USA"
                    },
                    {
                        "type": "WORK",
                        "street": "456 Business Avenue",
                        "city": "New York",
                        "state": "NY",
                        "zipCode": "10002",
                        "country": "USA"
                    }
                ],
                "kycInfo": {
                    "documents": [
                        {
                            "type": "PASSPORT",
                            "number": "P%s",
                            "issueDate": "2020-01-01",
                            "expiryDate": "2030-01-01",
                            "issuingCountry": "USA"
                        },
                        {
                            "type": "DRIVER_LICENSE",
                            "number": "DL%s",
                            "issueDate": "2021-06-15",
                            "expiryDate": "2026-06-15",
                            "issuingState": "NY"
                        }
                    ]
                },
                "financialInfo": {
                    "annualIncome": 75000,
                    "employmentStatus": "EMPLOYED",
                    "employer": "Test Company Inc"
                },
                "requestType": "%s",
                "metadata": {
                    "source": "TEST",
                    "timestamp": %d,
                    "testType": "COMPLEX"
                }
            }
            """, UUID.randomUUID().toString().substring(0, 8),
                 UUID.randomUUID().toString().substring(0, 8),
                 UUID.randomUUID().toString().substring(0, 8),
                 requestType, System.currentTimeMillis());
    }

    /**
     * Create customer data with special characters
     */
    public static String createCustomerWithSpecialChars(String requestType) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <customer>
                <personalInfo>
                    <firstName>José</firstName>
                    <lastName>García-López</lastName>
                    <email>josé.garcía@example.com</email>
                    <phone>+34 91 123 45 67</phone>
                    <dateOfBirth>1985-05-15</dateOfBirth>
                </personalInfo>
                <address>
                    <street>Calle Mayor 123, 2º A</street>
                    <city>Madrid</city>
                    <state>Madrid</state>
                    <zipCode>28001</zipCode>
                    <country>España</country>
                </address>
                <kycInfo>
                    <documentType>PASSPORT</documentType>
                    <documentNumber>P%s</documentNumber>
                    <issueDate>2020-01-01</issueDate>
                    <expiryDate>2030-01-01</expiryDate>
                    <issuingCountry>España</issuingCountry>
                </kycInfo>
                <requestType>%s</requestType>
                <metadata>
                    <source>TEST</source>
                    <timestamp>%d</timestamp>
                    <specialChars>true</specialChars>
                </metadata>
            </customer>
            """, UUID.randomUUID().toString().substring(0, 8), 
                 requestType, System.currentTimeMillis());
    }

    /**
     * Create minimal customer data
     */
    public static String createMinimalCustomer(String requestType) {
        return String.format("""
            {
                "firstName": "Minimal",
                "lastName": "Test",
                "email": "minimal@test.com",
                "requestType": "%s"
            }
            """, requestType);
    }

    /**
     * Create invalid customer data
     */
    public static String createInvalidCustomerData() {
        return """
            {
                "invalidField": "invalidValue",
                "missingRequiredFields": true
            }
            """;
    }

    /**
     * Create test headers
     */
    public static Map<String, String> createTestHeaders(String correlationId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Correlation-ID", correlationId);
        headers.put("X-Test-Source", "RESTASSURED");
        headers.put("X-Test-Timestamp", String.valueOf(System.currentTimeMillis()));
        return headers;
    }

    /**
     * Create performance test data
     */
    public static String createPerformanceTestData(int fieldCount) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"customerId\": \"PERF-TEST-").append(System.currentTimeMillis()).append("\",");
        json.append("\"personalInfo\": {");
        json.append("\"firstName\": \"Performance\",");
        json.append("\"lastName\": \"Test\",");
        json.append("\"email\": \"performance@test.com\"");
        json.append("},");
        json.append("\"data\": {");
        
        for (int i = 0; i < fieldCount; i++) {
            json.append("\"field").append(i).append("\": \"Value").append(i).append("\"");
            if (i < fieldCount - 1) {
                json.append(",");
            }
        }
        
        json.append("}");
        json.append("}");
        return json.toString();
    }

    /**
     * Generate test correlation ID
     */
    public static String generateTestCorrelationId() {
        return "TEST-CORR-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Generate test process ID
     */
    public static String generateTestProcessId() {
        return "TEST-PROC-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
}





