package com.banking.onboarding.constants;

/**
 * Central constants class for Banking Onboarding Service
 * Contains all hardcoded strings used throughout the application
 */
public final class OnboardingConstants {

    private OnboardingConstants() {
        // Utility class - prevent instantiation
    }

    // ===========================================
    // REQUEST TYPES
    // ===========================================
    public static final class RequestTypes {
        public static final String ADD_KYC = "ADD_KYC";
        public static final String UPDATE_KYC = "UPDATE_KYC";
        public static final String DELETE_KYC = "DELETE_KYC";
        public static final String VERIFY_KYC = "VERIFY_KYC";
        
        private RequestTypes() {}
    }

    // ===========================================
    // STEP NAMES
    // ===========================================
    public static final class StepNames {
        public static final String XML_TO_JSON_TRANSFORMATION = "XML_TO_JSON_TRANSFORMATION";
        public static final String FENERGO_ENTITY_CREATION = "FENERGO_ENTITY_CREATION";
        public static final String FENERGO_JOURNEY_SCHEMA_EVALUATION = "FENERGO_JOURNEY_SCHEMA_EVALUATION";
        public static final String FENERGO_JOURNEY_LAUNCH = "FENERGO_JOURNEY_LAUNCH";
        
        private StepNames() {}
    }

    // ===========================================
    // PROCESS STATUS
    // ===========================================
    public static final class ProcessStatus {
        public static final String PENDING = "PENDING";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String CANCELLED = "CANCELLED";
        
        private ProcessStatus() {}
    }

    // ===========================================
    // STEP STATUS
    // ===========================================
    public static final class StepStatus {
        public static final String PENDING = "PENDING";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
        public static final String SKIPPED = "SKIPPED";
        
        private StepStatus() {}
    }

    // ===========================================
    // ERROR TYPES
    // ===========================================
    public static final class ErrorTypes {
        public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String PROCESS_ERROR = "PROCESS_ERROR";
        public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
        public static final String STEP_EXECUTION_ERROR = "STEP_EXECUTION_ERROR";
        public static final String BINDING_ERROR = "BINDING_ERROR";
        public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
        public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
        public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
        public static final String METHOD_NOT_SUPPORTED = "METHOD_NOT_SUPPORTED";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String ILLEGAL_ARGUMENT = "ILLEGAL_ARGUMENT";
        public static final String RUNTIME_ERROR = "RUNTIME_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        public static final String OPERATION_FAILURE = "OPERATION_FAILURE";
        public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
        public static final String DATABASE_ERROR = "DATABASE_ERROR";
        public static final String CIRCUIT_BREAKER_ERROR = "CIRCUIT_BREAKER_ERROR";
        public static final String RETRY_FAILURE = "RETRY_FAILURE";
        
        private ErrorTypes() {}
    }

    // ===========================================
    // ERROR CODES
    // ===========================================
    public static final class ErrorCodes {
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String PROCESS_ERROR = "PROCESS_ERROR";
        public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
        public static final String STEP_EXECUTION_ERROR = "STEP_EXECUTION_ERROR";
        public static final String BINDING_ERROR = "BINDING_ERROR";
        public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
        public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
        public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
        public static final String METHOD_NOT_SUPPORTED = "METHOD_NOT_SUPPORTED";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String ILLEGAL_ARGUMENT = "ILLEGAL_ARGUMENT";
        public static final String RUNTIME_ERROR = "RUNTIME_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String UNKNOWN = "UNKNOWN";
        
        private ErrorCodes() {}
    }

    // ===========================================
    // SEVERITY LEVELS
    // ===========================================
    public static final class SeverityLevels {
        public static final String LOW = "LOW";
        public static final String MEDIUM = "MEDIUM";
        public static final String HIGH = "HIGH";
        public static final String CRITICAL = "CRITICAL";
        
        private SeverityLevels() {}
    }

    // ===========================================
    // HTTP HEADERS
    // ===========================================
    public static final class HttpHeaders {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_TENANT_ID = "X-TENANT-ID";
        public static final String X_CORRELATION_ID = "X-Correlation-ID";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String USER_AGENT = "User-Agent";
        public static final String BEARER_PREFIX = "Bearer ";
        
        private HttpHeaders() {}
    }

    // ===========================================
    // CONTENT TYPES
    // ===========================================
    public static final class ContentTypes {
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_XML = "application/xml";
        public static final String TEXT_XML = "text/xml";
        public static final String TEXT_PLAIN = "text/plain";
        
        private ContentTypes() {}
    }

    // ===========================================
    // ENTITY TYPES
    // ===========================================
    public static final class EntityTypes {
        public static final String COMPANY = "Company";
        public static final String CLIENT = "Client";
        public static final String INDIVIDUAL = "Individual";
        public static final String CORPORATE = "Corporate";
        
        private EntityTypes() {}
    }

    // ===========================================
    // JOURNEY TYPES
    // ===========================================
    public static final class JourneyTypes {
        public static final String CLIENT_ONBOARDING = "Client Onboarding";
        public static final String KYC_VERIFICATION = "KYC Verification";
        public static final String RISK_ASSESSMENT = "Risk Assessment";
        
        private JourneyTypes() {}
    }

    // ===========================================
    // JURISDICTIONS
    // ===========================================
    public static final class Jurisdictions {
        public static final String US = "US";
        public static final String UK = "UK";
        public static final String EU = "EU";
        public static final String APAC = "APAC";
        
        private Jurisdictions() {}
    }

    // ===========================================
    // ACCESS LAYERS
    // ===========================================
    public static final class AccessLayers {
        public static final String INTERNAL = "internal";
        public static final String EXTERNAL = "external";
        
        private AccessLayers() {}
    }

    // ===========================================
    // PROPERTY TYPES
    // ===========================================
    public static final class PropertyTypes {
        public static final String SINGLE = "Single";
        public static final String MULTIPLE = "Multiple";
        public static final String ARRAY = "Array";
        
        private PropertyTypes() {}
    }

    // ===========================================
    // PROCESS ID PREFIXES
    // ===========================================
    public static final class ProcessIdPrefixes {
        public static final String PROCESS = "PROC-";
        public static final String PROCESS_REJECTED = "PROC-REJECTED-";
        public static final String TEST = "TEST-";
        
        private ProcessIdPrefixes() {}
    }

    // ===========================================
    // AUDIT PREFIXES
    // ===========================================
    public static final class AuditPrefixes {
        public static final String AUDIT = "AUDIT_";
        
        private AuditPrefixes() {}
    }

    // ===========================================
    // SEQUENCE NAMES
    // ===========================================
    public static final class SequenceNames {
        public static final String SEQUENCE = "SEQUENCE";
        
        private SequenceNames() {}
    }

    // ===========================================
    // MOCK TOKENS
    // ===========================================
    public static final class MockTokens {
        public static final String FENERGO_TOKEN = "mock-fenergo-token";
        public static final String APIGEE_TOKEN = "mock-apigee-token";
        
        private MockTokens() {}
    }

    // ===========================================
    // DEFAULT VALUES
    // ===========================================
    public static final class DefaultValues {
        public static final String DEFAULT_TENANT = "default-tenant";
        public static final String DEFAULT_ENTITY_NAME = "Acme Ltd";
        public static final String DEFAULT_JURISDICTION = "US";
        public static final String DEFAULT_ENTITY_TYPE = "Company";
        
        private DefaultValues() {}
    }

    // ===========================================
    // MESSAGES
    // ===========================================
    public static final class Messages {
        public static final String XML_DATA_REQUIRED = "XML data is required and cannot be empty";
        public static final String PROCESS_NOT_FOUND = "Process not found with ID: ";
        public static final String REQUEST_REJECTED = "Request rejected due to correlation ID strategy";
        public static final String REQUEST_REJECTED_PREFIX = "Request rejected: ";
        public static final String NO_JSON_DATA_AVAILABLE = "No JSON data available from previous step";
        public static final String NO_SCHEMA_INFO_AVAILABLE = "No schema information available from previous step";
        public static final String MISSING_ENTITY_ID_OR_SCHEMA = "Missing required entityId or journeySchemaId";
        public static final String ENTITY_CREATION_NO_ID = "Entity creation succeeded but no entityId returned";
        public static final String INVALID_FENERGO_RESPONSE = "Invalid response from Fenergo Entity API";
        public static final String ENTITY_CREATION_FAILED = "Entity creation failed: ";
        public static final String JOURNEY_LAUNCH_FAILED = "Journey launch failed: ";
        public static final String NO_JOURNEY_RESPONSE = "No response from Fenergo Journey Command API";
        
        private Messages() {}
    }

    // ===========================================
    // SUGGESTIONS
    // ===========================================
    public static final class Suggestions {
        public static final String CHECK_INPUT_DATA = "Please check your input data and ensure all required fields are provided with valid values.";
        public static final String CHECK_PROCESS_STATUS = "Please check the process status and retry if necessary.";
        public static final String EXTERNAL_SERVICE_UNAVAILABLE = "External service is temporarily unavailable. Please try again later.";
        public static final String STEP_EXECUTION_FAILED = "Step execution failed. Please check the process status and retry if necessary.";
        public static final String CHECK_REQUEST_FORMAT = "Please check your request format and ensure all fields are properly formatted.";
        public static final String PROVIDE_MISSING_PARAMETER = "Please provide the missing required parameter: ";
        public static final String PROVIDE_VALID_TYPE = "Please provide a valid value of type ";
        public static final String CHECK_REQUEST_BODY = "Please check your request body format (JSON/XML) and ensure it's properly formatted.";
        public static final String USE_SUPPORTED_METHODS = "Please use one of the supported HTTP methods: ";
        public static final String CHECK_API_DOCUMENTATION = "Please check the API documentation for available endpoints.";
        public static final String CHECK_INPUT_PARAMETERS = "Please check your input parameters and ensure they are valid.";
        public static final String CONTACT_SUPPORT = "An unexpected error occurred. Please contact support if this persists.";
        public static final String TRY_AGAIN = "Please check your request and try again.";
        
        private Suggestions() {}
    }
}


