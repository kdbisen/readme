package com.banking.onboarding.enums;

/**
 * Enums for Banking Onboarding Service
 * Provides type safety for commonly used values
 */
public final class OnboardingEnums {

    private OnboardingEnums() {
        // Utility class - prevent instantiation
    }

    // ===========================================
    // REQUEST TYPES ENUM
    // ===========================================
    public enum RequestType {
        ADD_KYC("ADD_KYC"),
        UPDATE_KYC("UPDATE_KYC"),
        DELETE_KYC("DELETE_KYC"),
        VERIFY_KYC("VERIFY_KYC");

        private final String value;

        RequestType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static RequestType fromString(String value) {
            for (RequestType type : RequestType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown request type: " + value);
        }
    }

    // ===========================================
    // PROCESS STATUS ENUM
    // ===========================================
    public enum ProcessStatus {
        PENDING("PENDING"),
        IN_PROGRESS("IN_PROGRESS"),
        COMPLETED("COMPLETED"),
        FAILED("FAILED"),
        CANCELLED("CANCELLED");

        private final String value;

        ProcessStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ProcessStatus fromString(String value) {
            for (ProcessStatus status : ProcessStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown process status: " + value);
        }
    }

    // ===========================================
    // STEP STATUS ENUM
    // ===========================================
    public enum StepStatus {
        PENDING("PENDING"),
        IN_PROGRESS("IN_PROGRESS"),
        COMPLETED("COMPLETED"),
        FAILED("FAILED"),
        SKIPPED("SKIPPED");

        private final String value;

        StepStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static StepStatus fromString(String value) {
            for (StepStatus status : StepStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown step status: " + value);
        }
    }

    // ===========================================
    // EXECUTION ORDER ENUM
    // ===========================================
    public enum ExecutionOrder {
        PRIORITY("PRIORITY"),
        ORDER("ORDER");

        private final String value;

        ExecutionOrder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ExecutionOrder fromString(String value) {
            for (ExecutionOrder order : ExecutionOrder.values()) {
                if (order.value.equalsIgnoreCase(value)) {
                    return order;
                }
            }
            throw new IllegalArgumentException("Unknown execution order: " + value);
        }
    }

    // ===========================================
    // STEP CATEGORY ENUM
    // ===========================================
    public enum StepCategory {
        GENERAL("GENERAL"),
        TRANSFORMATION("TRANSFORMATION"),
        FENERGO("FENERGO"),
        VALIDATION("VALIDATION");

        private final String value;

        StepCategory(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static StepCategory fromString(String value) {
            for (StepCategory category : StepCategory.values()) {
                if (category.value.equals(value)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Unknown step category: " + value);
        }
    }

    // ===========================================
    // DEPENDENCY TYPE ENUM
    // ===========================================
    public enum DependencyType {
        REQUIRED("REQUIRED"),
        OPTIONAL("OPTIONAL");

        private final String value;

        DependencyType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DependencyType fromString(String value) {
            for (DependencyType type : DependencyType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown dependency type: " + value);
        }
    }

    // ===========================================
    // DATA TYPE ENUM
    // ===========================================
    public enum DataType {
        NULL("NULL"),
        JSON("JSON"),
        XML("XML"),
        JSON_ARRAY("JSON_ARRAY"),
        MAP("MAP"),
        STRING("STRING");

        private final String value;

        DataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DataType fromString(String value) {
            for (DataType type : DataType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown data type: " + value);
        }
    }

    // ===========================================
    // ERROR TYPE ENUM
    // ===========================================
    public enum ErrorType {
        STEP_EXECUTION_ERROR("STEP_EXECUTION_ERROR"),
        VALIDATION_ERROR("VALIDATION_ERROR"),
        PROCESS_ERROR("PROCESS_ERROR"),
        EXTERNAL_API_ERROR("EXTERNAL_API_ERROR");

        private final String value;

        ErrorType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ErrorType fromString(String value) {
            for (ErrorType type : ErrorType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown error type: " + value);
        }
    }

    // ===========================================
    // API STATUS ENUM
    // ===========================================
    public enum ApiStatus {
        CREATED("CREATED"),
        EVALUATED("EVALUATED"),
        LAUNCHED("LAUNCHED"),
        SUCCESS("SUCCESS"),
        FAILED("FAILED");

        private final String value;

        ApiStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ApiStatus fromString(String value) {
            for (ApiStatus status : ApiStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown API status: " + value);
        }
    }
}