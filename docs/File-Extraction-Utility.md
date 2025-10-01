# File Extraction Utility Documentation

## Overview

The `FileExtractionUtils` class provides comprehensive functionality for extracting data from JSON and XML files. This utility is designed to work seamlessly with the banking onboarding service for processing customer data files.

## Features

- **JSON File Processing**: Extract complete data or specific fields from JSON files
- **XML File Processing**: Extract complete data or specific elements from XML files
- **Field/Element Extraction**: Navigate nested structures using dot-separated paths
- **File Format Conversion**: Convert between JSON and XML formats
- **File Validation**: Check file existence, readability, and size
- **Error Handling**: Comprehensive error handling with detailed logging

## Core Components

### 1. FileExtractionUtils

The main utility class with the following key methods:

#### JSON Methods
- `extractFromJsonFile(String filePath)` - Extract complete JSON data
- `extractFieldFromJsonFile(String filePath, String fieldPath)` - Extract specific field
- `extractRawJsonContent(String filePath)` - Get raw JSON content as string

#### XML Methods
- `extractFromXmlFile(String filePath)` - Extract complete XML data
- `extractElementFromXmlFile(String filePath, String elementPath)` - Extract specific element
- `extractRawXmlContent(String filePath)` - Get raw XML content as string

#### Utility Methods
- `isFileReadable(String filePath)` - Check if file exists and is readable
- `getFileSize(String filePath)` - Get file size in bytes
- `convertJsonFileToXml(String filePath)` - Convert JSON to XML
- `convertXmlFileToJson(String filePath)` - Convert XML to JSON

### 2. CustomerDataFileService

A service layer that provides business logic for processing customer data files:

- `processCustomerDataFromJson(String filePath)` - Process customer data from JSON
- `processCustomerDataFromXml(String filePath)` - Process customer data from XML
- `getCustomerName(String filePath, boolean isXmlFile)` - Extract customer name
- `getCustomerEmail(String filePath, boolean isXmlFile)` - Extract customer email
- `getFileInfo(String filePath)` - Get file information

### 3. FileExtractionController

REST API endpoints for file extraction operations:

- `GET /api/v1/file-extraction/json/{filePath}` - Extract from JSON file
- `GET /api/v1/file-extraction/xml/{filePath}` - Extract from XML file
- `GET /api/v1/file-extraction/json/{filePath}/field/{fieldPath}` - Extract field from JSON
- `GET /api/v1/file-extraction/xml/{filePath}/element/{elementPath}` - Extract element from XML
- `GET /api/v1/file-extraction/json/{filePath}/convert-to-xml` - Convert JSON to XML
- `GET /api/v1/file-extraction/xml/{filePath}/convert-to-json` - Convert XML to JSON
- `GET /api/v1/file-extraction/info/{filePath}` - Get file information
- `GET /api/v1/file-extraction/customer-name/{filePath}` - Get customer name

## Usage Examples

### 1. Extract Complete Data from JSON File

```java
@Autowired
private FileExtractionUtils fileExtractionUtils;

// Extract all data from JSON file
Optional<Map<String, Object>> data = fileExtractionUtils.extractFromJsonFile("customer.json");
if (data.isPresent()) {
    Map<String, Object> customerData = data.get();
    // Process customer data
}
```

### 2. Extract Specific Field from JSON File

```java
// Extract customer's first name
Optional<Object> firstName = fileExtractionUtils.extractFieldFromJsonFile(
    "customer.json", 
    "customer.personalInfo.firstName"
);
```

### 3. Extract Complete Data from XML File

```java
// Extract all data from XML file
Optional<Map<String, Object>> data = fileExtractionUtils.extractFromXmlFile("customer.xml");
if (data.isPresent()) {
    Map<String, Object> customerData = data.get();
    // Process customer data
}
```

### 4. Extract Specific Element from XML File

```java
// Extract customer's email
Optional<Object> email = fileExtractionUtils.extractElementFromXmlFile(
    "customer.xml", 
    "personalInfo.email"
);
```

### 5. Convert Between Formats

```java
// Convert JSON to XML
Optional<String> xmlContent = fileExtractionUtils.convertJsonFileToXml("customer.json");

// Convert XML to JSON
Optional<String> jsonContent = fileExtractionUtils.convertXmlFileToJson("customer.xml");
```

### 6. File Validation

```java
// Check if file is readable
boolean readable = fileExtractionUtils.isFileReadable("customer.json");

// Get file size
Optional<Long> size = fileExtractionUtils.getFileSize("customer.json");
```

## API Usage Examples

### Extract from JSON File

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/json/sample-customer.json"
```

### Extract Specific Field from JSON

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/json/sample-customer.json/field/customer.personalInfo.firstName"
```

### Extract from XML File

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/xml/sample-customer.xml"
```

### Extract Specific Element from XML

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/xml/sample-customer.xml/element/personalInfo.email"
```

### Convert JSON to XML

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/json/sample-customer.json/convert-to-xml"
```

### Convert XML to JSON

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/xml/sample-customer.xml/convert-to-json"
```

### Get File Information

```bash
curl -X GET "http://localhost:8080/api/v1/file-extraction/info/sample-customer.json"
```

### Get Customer Name

```bash
# From JSON file
curl -X GET "http://localhost:8080/api/v1/file-extraction/customer-name/sample-customer.json?isXmlFile=false"

# From XML file
curl -X GET "http://localhost:8080/api/v1/file-extraction/customer-name/sample-customer.xml?isXmlFile=true"
```

## Sample Files

The project includes sample files for testing:

- `sample-customer.json` - Sample customer data in JSON format
- `sample-customer.xml` - Sample customer data in XML format

## Error Handling

The utility provides comprehensive error handling:

- **File Not Found**: Returns empty Optional when file doesn't exist
- **Invalid Format**: Handles malformed JSON/XML gracefully
- **Permission Issues**: Checks file readability before processing
- **Logging**: Detailed logging for debugging and monitoring

## Dependencies

Required dependencies in `pom.xml`:

```xml
<!-- Jackson for JSON processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Jackson for XML processing -->
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
</dependency>

<!-- Jackson JSR310 for Java 8 date/time support -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

## Testing

The utility includes comprehensive unit tests in `FileExtractionUtilsTest.java` that cover:

- JSON file extraction
- XML file extraction
- Field/element extraction
- File format conversion
- File validation
- Error scenarios

## Integration with Banking Onboarding Service

The file extraction utility integrates seamlessly with the banking onboarding service:

1. **Customer Data Processing**: Extract customer information from uploaded files
2. **KYC Document Processing**: Process KYC documents in various formats
3. **Data Validation**: Validate extracted data before processing
4. **Format Conversion**: Convert between different data formats as needed
5. **Audit Trail**: Log all file operations for compliance

## Best Practices

1. **Always validate files** before processing
2. **Use specific field extraction** when you only need certain data
3. **Handle errors gracefully** with proper logging
4. **Check file permissions** before attempting to read
5. **Use appropriate data types** for extracted values
6. **Implement proper security** for file path validation in production

## Security Considerations

- Validate file paths to prevent directory traversal attacks
- Implement file size limits to prevent memory issues
- Use appropriate file permissions
- Sanitize extracted data before processing
- Implement rate limiting for API endpoints
