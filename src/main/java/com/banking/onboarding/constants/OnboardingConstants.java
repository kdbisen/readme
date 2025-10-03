package com.banking.onboarding.constants;

/**
 * Comprehensive Constants for Banking Onboarding Service
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
        // Entity Onboarding Types
        public static final String ADD_KYC = "ADD_KYC";
        public static final String UPDATE_KYC = "UPDATE_KYC";
        public static final String DELETE_KYC = "DELETE_KYC";
        public static final String VERIFY_KYC = "VERIFY_KYC";
        
        // Document Verification Types
        public static final String DOCUMENT_VERIFICATION = "DOCUMENT_VERIFICATION";
        public static final String IDENTITY_VERIFICATION = "IDENTITY_VERIFICATION";
        public static final String ADDRESS_VERIFICATION = "ADDRESS_VERIFICATION";
        public static final String INCOME_VERIFICATION = "INCOME_VERIFICATION";
        
        private RequestTypes() {}
    }

    // ===========================================
    // STEP NAMES
    // ===========================================
    public static final class StepNames {
        // Entity Onboarding Steps
        public static final String XML_TO_JSON_TRANSFORMATION = "XML_TO_JSON_TRANSFORMATION";
        public static final String FENERGO_ENTITY_CREATION = "FENERGO_ENTITY_CREATION";
        public static final String FENERGO_JOURNEY_SCHEMA_EVALUATION = "FENERGO_JOURNEY_SCHEMA_EVALUATION";
        public static final String FENERGO_JOURNEY_LAUNCH = "FENERGO_JOURNEY_LAUNCH";
        
        // Document Verification Steps
        public static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
        public static final String DOCUMENT_VALIDATION = "DOCUMENT_VALIDATION";
        public static final String OCR_PROCESSING = "OCR_PROCESSING";
        public static final String COMPLIANCE_CHECK = "COMPLIANCE_CHECK";
        public static final String DOCUMENT_APPROVAL = "DOCUMENT_APPROVAL";
        
        private StepNames() {}
    }

    // ===========================================
    // STEP DESCRIPTIONS
    // ===========================================
    public static final class StepDescriptions {
        // Entity Onboarding Descriptions
        public static final String XML_TO_JSON_TRANSFORMATION = "Transform XML to JSON";
        public static final String FENERGO_ENTITY_CREATION = "Create Fenergo Entity";
        public static final String FENERGO_JOURNEY_SCHEMA_EVALUATION = "Evaluate Journey Schema";
        public static final String FENERGO_JOURNEY_LAUNCH = "Launch Journey";
        
        // Document Verification Descriptions
        public static final String DOCUMENT_UPLOAD = "Upload and validate document files";
        public static final String DOCUMENT_VALIDATION = "Validate document format and content";
        public static final String OCR_PROCESSING = "Extract text using OCR technology";
        public static final String COMPLIANCE_CHECK = "Check document compliance requirements";
        public static final String DOCUMENT_APPROVAL = "Approve document for processing";
        
        private StepDescriptions() {}
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
    // PROCESS ID PREFIXES
    // ===========================================
    public static final class ProcessIdPrefixes {
        public static final String PROCESS = "PROC-";
        public static final String PROCESS_REJECTED = "PROC-REJECTED-";
        public static final String TEST = "TEST-";
        public static final String CORRELATION = "CORR-";
        
        private ProcessIdPrefixes() {}
    }

    // ===========================================
    // EXECUTION ORDERS
    // ===========================================
    public static final class ExecutionOrders {
        public static final String PRIORITY = "PRIORITY";
        public static final String ORDER = "ORDER";
        
        private ExecutionOrders() {}
    }

    // ===========================================
    // STEP CATEGORIES
    // ===========================================
    public static final class StepCategories {
        public static final String GENERAL = "GENERAL";
        public static final String TRANSFORMATION = "TRANSFORMATION";
        public static final String FENERGO = "FENERGO";
        public static final String VALIDATION = "VALIDATION";
        
        private StepCategories() {}
    }

    // ===========================================
    // DEPENDENCY TYPES
    // ===========================================
    public static final class DependencyTypes {
        public static final String REQUIRED = "REQUIRED";
        public static final String OPTIONAL = "OPTIONAL";
        
        private DependencyTypes() {}
    }

    // ===========================================
    // DATA TYPES
    // ===========================================
    public static final class DataTypes {
        public static final String NULL = "NULL";
        public static final String JSON = "JSON";
        public static final String XML = "XML";
        public static final String JSON_ARRAY = "JSON_ARRAY";
        public static final String MAP = "MAP";
        public static final String STRING = "STRING";
        
        private DataTypes() {}
    }

    // ===========================================
    // ERROR TYPES
    // ===========================================
    public static final class ErrorTypes {
        public static final String STEP_EXECUTION_ERROR = "STEP_EXECUTION_ERROR";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String PROCESS_ERROR = "PROCESS_ERROR";
        public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
        public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
        public static final String BINDING_ERROR = "BINDING_ERROR";
        public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
        public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
        public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
        public static final String METHOD_NOT_SUPPORTED = "METHOD_NOT_SUPPORTED";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String ILLEGAL_ARGUMENT = "ILLEGAL_ARGUMENT";
        public static final String RUNTIME_ERROR = "RUNTIME_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        
        private ErrorTypes() {}
    }

    // ===========================================
    // API STATUS
    // ===========================================
    public static final class ApiStatus {
        public static final String CREATED = "CREATED";
        public static final String EVALUATED = "EVALUATED";
        public static final String LAUNCHED = "LAUNCHED";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        
        private ApiStatus() {}
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
        public static final String STEP_CANNOT_BE_EXECUTED = "Step cannot be executed";
        public static final String STEP_FAILED_FORMAT = "Step %s failed: %s";
        public static final String ENTITY_CREATED_SUCCESSFULLY = "Entity created successfully";
        
        private Messages() {}
    }

    // ===========================================
    // LOG MESSAGES
    // ===========================================
    public static final class LogMessages {
        public static final String STEP_NUMBERING_CACHE_CLEARED = "Step numbering cache cleared";
        public static final String STEP_NUMBERING_CACHE_REFRESHED = "Step numbering cache refreshed";
        public static final String DEV_ENVIRONMENT_INIT = "Initializing Development Environment Configuration";
        public static final String TEST_ENVIRONMENT_INIT = "Initializing Test Environment Configuration";
        public static final String STAGING_ENVIRONMENT_INIT = "Initializing Staging Environment Configuration";
        public static final String PROD_ENVIRONMENT_INIT = "Initializing Production Environment Configuration";
        
        private LogMessages() {}
    }

    // ===========================================
    // FORMAT STRINGS
    // ===========================================
    public static final class FormatStrings {
        public static final String STEP_FORMAT = "Step %d";
        public static final String STEP_FAILED_FORMAT = "Step %s failed: %s";
        public static final String PROCESS_ID_FORMAT = "%s%s";
        public static final String CORRELATION_ID_FORMAT = "%s%s";
        
        private FormatStrings() {}
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
        public static final String DEFAULT_STEP_DESCRIPTION = "Default step";
        
        private DefaultValues() {}
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

    // ===========================================
    // CONFIGURATION VALUES
    // ===========================================
    public static final class ConfigValues {
        public static final String STEP_DEFINITIONS_DEFAULT = "XML_TO_JSON_TRANSFORMATION,FENERGO_ENTITY_CREATION,FENERGO_JOURNEY_SCHEMA_EVALUATION,FENERGO_JOURNEY_LAUNCH";
        public static final String EXECUTION_ORDER_DEFAULT = "PRIORITY";
        public static final boolean RETRY_ENABLED_DEFAULT = true;
        public static final int MAX_RETRIES_DEFAULT = 3;
        public static final long RETRY_DELAY_MS_DEFAULT = 1000L;
        public static final double BACKOFF_MULTIPLIER_DEFAULT = 2.0;
        public static final int TIMEOUT_MS_DEFAULT = 30000;
        
        private ConfigValues() {}
    }

    // ===========================================
    // STEP PRIORITIES
    // ===========================================
    public static final class StepPriorities {
        // Entity Onboarding Priorities
        public static final int XML_TO_JSON_TRANSFORMATION = 1;
        public static final int FENERGO_ENTITY_CREATION = 2;
        public static final int FENERGO_JOURNEY_SCHEMA_EVALUATION = 3;
        public static final int FENERGO_JOURNEY_LAUNCH = 4;
        
        // Document Verification Priorities
        public static final int DOCUMENT_UPLOAD = 10;
        public static final int DOCUMENT_VALIDATION = 20;
        public static final int OCR_PROCESSING = 30;
        public static final int COMPLIANCE_CHECK = 40;
        public static final int DOCUMENT_APPROVAL = 50;
        
        private StepPriorities() {}
    }

    // ===========================================
    // HTTP HEADERS
    // ===========================================
    public static final class HttpHeaders {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String X_CORRELATION_ID = "X-Correlation-ID";
        public static final String X_TENANT_ID = "X-Tenant-ID";
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
        
        private ContentTypes() {}
    }

    // ===========================================
    // ENTITY TYPES
    // ===========================================
    public static final class EntityTypes {
        public static final String COMPANY = "Company";
        public static final String INDIVIDUAL = "Individual";
        public static final String PARTNERSHIP = "Partnership";
        public static final String CLIENT = "Client";
        
        private EntityTypes() {}
    }

    // ===========================================
    // PROPERTY TYPES
    // ===========================================
    public static final class PropertyTypes {
        public static final String STRING = "String";
        public static final String INTEGER = "Integer";
        public static final String BOOLEAN = "Boolean";
        public static final String DATE = "Date";
        public static final String SINGLE = "Single";
        public static final String MULTIPLE = "Multiple";
        
        private PropertyTypes() {}
    }

    // ===========================================
    // JOURNEY TYPES
    // ===========================================
    public static final class JourneyTypes {
        public static final String CLIENT_ONBOARDING = "Client Onboarding";
        public static final String KYC_VERIFICATION = "KYC Verification";
        public static final String COMPLIANCE_CHECK = "Compliance Check";
        
        private JourneyTypes() {}
    }

    // ===========================================
    // ACCESS LAYERS
    // ===========================================
    public static final class AccessLayers {
        public static final String PUBLIC = "Public";
        public static final String PRIVATE = "Private";
        public static final String INTERNAL = "Internal";
        public static final String EXTERNAL = "External";
        
        private AccessLayers() {}
    }

    // ===========================================
    // SEQUENCE NAMES
    // ===========================================
    public static final class SequenceNames {
        public static final String SEQUENCE = "SEQUENCE";
        public static final String STEP_EXECUTION = "STEP_EXECUTION";
        
        private SequenceNames() {}
    }

    // ===========================================
    // ERROR CODES
    // ===========================================
    public static final class ErrorCodes {
        public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String PROCESS_ERROR = "PROCESS_ERROR";
        public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
        public static final String BINDING_ERROR = "BINDING_ERROR";
        public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
        public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
        public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
        public static final String METHOD_NOT_SUPPORTED = "METHOD_NOT_SUPPORTED";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String ILLEGAL_ARGUMENT = "ILLEGAL_ARGUMENT";
        public static final String RUNTIME_ERROR = "RUNTIME_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String STEP_EXECUTION_ERROR = "STEP_EXECUTION_ERROR";
        
        private ErrorCodes() {}
    }

    // ===========================================
    // STEP DEPENDENCIES
    // ===========================================
    public static final class StepDependencies {
        // Entity Onboarding Dependencies
        public static final String[] XML_TO_JSON_TRANSFORMATION = {};
        public static final String[] FENERGO_ENTITY_CREATION = {"XML_TO_JSON_TRANSFORMATION"};
        public static final String[] FENERGO_JOURNEY_SCHEMA_EVALUATION = {"FENERGO_ENTITY_CREATION"};
        public static final String[] FENERGO_JOURNEY_LAUNCH = {"FENERGO_JOURNEY_SCHEMA_EVALUATION"};
        
        // Document Verification Dependencies
        public static final String[] DOCUMENT_UPLOAD = {};
        public static final String[] DOCUMENT_VALIDATION = {"DOCUMENT_UPLOAD"};
        public static final String[] OCR_PROCESSING = {"DOCUMENT_VALIDATION"};
        public static final String[] COMPLIANCE_CHECK = {"OCR_PROCESSING"};
        public static final String[] DOCUMENT_APPROVAL = {"COMPLIANCE_CHECK"};
        
        private StepDependencies() {}
    }
}