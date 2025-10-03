package com.banking.onboarding.enums;

/**
 * Enums for Banking Onboarding Service
 * Provides type-safe constants for better code maintainability
 */
public class OnboardingEnums {

    // ===========================================
    // REQUEST TYPE ENUM
    // ===========================================
    public enum RequestType {
        ADD_KYC("ADD_KYC", "Add KYC Information"),
        UPDATE_KYC("UPDATE_KYC", "Update KYC Information"),
        DELETE_KYC("DELETE_KYC", "Delete KYC Information"),
        VERIFY_KYC("VERIFY_KYC", "Verify KYC Information");

        private final String code;
        private final String description;

        RequestType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static RequestType fromCode(String code) {
            for (RequestType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown request type: " + code);
        }
    }

    // ===========================================
    // STEP NAME ENUM
    // ===========================================
    public enum StepName {
        XML_TO_JSON_TRANSFORMATION("XML_TO_JSON_TRANSFORMATION", "XML to JSON Transformation", 1),
        FENERGO_ENTITY_CREATION("FENERGO_ENTITY_CREATION", "Fenergo Entity Creation", 2),
        FENERGO_JOURNEY_SCHEMA_EVALUATION("FENERGO_JOURNEY_SCHEMA_EVALUATION", "Fenergo Journey Schema Evaluation", 3),
        FENERGO_JOURNEY_LAUNCH("FENERGO_JOURNEY_LAUNCH", "Fenergo Journey Launch", 4);

        private final String code;
        private final String description;
        private final int priority;

        StepName(String code, String description, int priority) {
            this.code = code;
            this.description = description;
            this.priority = priority;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }

        public static StepName fromCode(String code) {
            for (StepName step : values()) {
                if (step.code.equals(code)) {
                    return step;
                }
            }
            throw new IllegalArgumentException("Unknown step name: " + code);
        }
    }

    // ===========================================
    // PROCESS STATUS ENUM
    // ===========================================
    public enum ProcessStatus {
        PENDING("PENDING", "Process is pending"),
        IN_PROGRESS("IN_PROGRESS", "Process is in progress"),
        COMPLETED("COMPLETED", "Process completed successfully"),
        FAILED("FAILED", "Process failed"),
        CANCELLED("CANCELLED", "Process was cancelled");

        private final String code;
        private final String description;

        ProcessStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ProcessStatus fromCode(String code) {
            for (ProcessStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown process status: " + code);
        }
    }

    // ===========================================
    // STEP STATUS ENUM
    // ===========================================
    public enum StepStatus {
        PENDING("PENDING", "Step is pending"),
        IN_PROGRESS("IN_PROGRESS", "Step is in progress"),
        COMPLETED("COMPLETED", "Step completed successfully"),
        FAILED("FAILED", "Step failed"),
        SKIPPED("SKIPPED", "Step was skipped");

        private final String code;
        private final String description;

        StepStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static StepStatus fromCode(String code) {
            for (StepStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown step status: " + code);
        }
    }

    // ===========================================
    // ERROR TYPE ENUM
    // ===========================================
    public enum ErrorType {
        BUSINESS_ERROR("BUSINESS_ERROR", "Business logic error"),
        VALIDATION_ERROR("VALIDATION_ERROR", "Validation error"),
        PROCESS_ERROR("PROCESS_ERROR", "Process execution error"),
        EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "External API error"),
        STEP_EXECUTION_ERROR("STEP_EXECUTION_ERROR", "Step execution error"),
        BINDING_ERROR("BINDING_ERROR", "Request binding error"),
        MISSING_PARAMETER("MISSING_PARAMETER", "Missing required parameter"),
        TYPE_MISMATCH("TYPE_MISMATCH", "Parameter type mismatch"),
        MALFORMED_REQUEST("MALFORMED_REQUEST", "Malformed request"),
        METHOD_NOT_SUPPORTED("METHOD_NOT_SUPPORTED", "HTTP method not supported"),
        NOT_FOUND("NOT_FOUND", "Resource not found"),
        ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT", "Illegal argument"),
        RUNTIME_ERROR("RUNTIME_ERROR", "Runtime error"),
        INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", "Unknown error"),
        SYSTEM_ERROR("SYSTEM_ERROR", "System error"),
        OPERATION_FAILURE("OPERATION_FAILURE", "Operation failure"),
        RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded"),
        DATABASE_ERROR("DATABASE_ERROR", "Database error"),
        CIRCUIT_BREAKER_ERROR("CIRCUIT_BREAKER_ERROR", "Circuit breaker error"),
        RETRY_FAILURE("RETRY_FAILURE", "Retry failure");

        private final String code;
        private final String description;

        ErrorType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ErrorType fromCode(String code) {
            for (ErrorType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown error type: " + code);
        }
    }

    // ===========================================
    // SEVERITY LEVEL ENUM
    // ===========================================
    public enum SeverityLevel {
        LOW("LOW", "Low severity"),
        MEDIUM("MEDIUM", "Medium severity"),
        HIGH("HIGH", "High severity"),
        CRITICAL("CRITICAL", "Critical severity");

        private final String code;
        private final String description;

        SeverityLevel(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static SeverityLevel fromCode(String code) {
            for (SeverityLevel level : values()) {
                if (level.code.equals(code)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Unknown severity level: " + code);
        }
    }

    // ===========================================
    // ENTITY TYPE ENUM
    // ===========================================
    public enum EntityType {
        COMPANY("Company", "Corporate entity"),
        CLIENT("Client", "Client entity"),
        INDIVIDUAL("Individual", "Individual person"),
        CORPORATE("Corporate", "Corporate entity");

        private final String code;
        private final String description;

        EntityType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static EntityType fromCode(String code) {
            for (EntityType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown entity type: " + code);
        }
    }

    // ===========================================
    // JOURNEY TYPE ENUM
    // ===========================================
    public enum JourneyType {
        CLIENT_ONBOARDING("Client Onboarding", "Client onboarding journey"),
        KYC_VERIFICATION("KYC Verification", "KYC verification journey"),
        RISK_ASSESSMENT("Risk Assessment", "Risk assessment journey");

        private final String code;
        private final String description;

        JourneyType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static JourneyType fromCode(String code) {
            for (JourneyType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown journey type: " + code);
        }
    }

    // ===========================================
    // JURISDICTION ENUM
    // ===========================================
    public enum Jurisdiction {
        US("US", "United States"),
        UK("UK", "United Kingdom"),
        EU("EU", "European Union"),
        APAC("APAC", "Asia Pacific");

        private final String code;
        private final String description;

        Jurisdiction(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Jurisdiction fromCode(String code) {
            for (Jurisdiction jurisdiction : values()) {
                if (jurisdiction.code.equals(code)) {
                    return jurisdiction;
                }
            }
            throw new IllegalArgumentException("Unknown jurisdiction: " + code);
        }
    }

    // ===========================================
    // PROPERTY TYPE ENUM
    // ===========================================
    public enum PropertyType {
        SINGLE("Single", "Single value property"),
        MULTIPLE("Multiple", "Multiple value property"),
        ARRAY("Array", "Array property");

        private final String code;
        private final String description;

        PropertyType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static PropertyType fromCode(String code) {
            for (PropertyType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown property type: " + code);
        }
    }
}



