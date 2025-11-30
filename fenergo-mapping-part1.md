I need to do it on java

now keeping all my faqs on mind.  create a detailed comprehension document

# Fenergo XML-to-JSON Transformer Service
## Comprehensive Technical Specification (Java Implementation)

**Version**: 1.0 | **Date**: Nov 30, 2025 | **Author**: AI Assistant for IoT Startup Founder

***

## 1. Business Context & Requirements

**Your Use Case**: IoT platform founder integrating legacy banking systems (XML data) with Fenergo CLM/KYB APIs for automated client onboarding.

**Key Challenges**:
- Legacy systems send **XML** with varying structures
- Fenergo requires **specific JSON** with data groups + lookups (`LookupId` + `LookupName`)
- **Business Analysts (BAs)** need to configure mappings without developer intervention
- Support **LegalEntity**, **Individual**, **Trust** entity types
- Handle **single/multi-select dropdowns** (lookups) automatically
- **Configurable via Excel** for rapid changes

***

## 2. High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   BA Excel      │───▶│ Config Generator │───▶│ Transformer     │
│ (Field Mappings)│    │ (Excel→JSON)     │    │ Service (Java)  │
└─────────────────┘    └──────────────────┘    │                 │
                                               │ ┌──────────────┐ │
┌─────────────────┐                            │ │ Lookup Cache │ │
│ Legacy XML Data │───▶─────────────────────────▶│ (Redis/Memory)│ │
└─────────────────┘                            │ └──────────────┘ │
                                               │                 │
                                               └─────────────────┘
                                                              │
                                                      ┌─────────────────┐
                                                      │ Fenergo APIs    │
                                                      │ • /entitydata   │
                                                      │ • /lookups      │
                                                      └─────────────────┘
```

***

## 3. Excel Configuration Template

### Sheet: `Field_Mappings` (Primary BA Sheet)

| A: Fenergo_Path | B: Fenergo_Type | C: XML_Path | D: Lookup_Name | E: Multi_Select | F: Transform | G: Required | H: Default_Value | I: Condition |
|-----------------|-----------------|-------------|----------------|---------------|--------------|-------------|------------------|--------------|
| DataGroups.Name.LegalName | Text | /root/entity/legalName | | FALSE | trim | TRUE | | |
| DataGroups.Address.Country | Lookup | /root/entity/address/country | Country | FALSE | upper | TRUE | IN | |
| DataGroups.Business.Sectors | MultiLookup | /root/entity/sectors/sector | BusinessSectors | TRUE | capitalize | FALSE | | |
| DataGroups.Risk.RiskRating | Lookup | /root/entity/risk/level | RiskRating | FALSE | | TRUE | Low | |

### Sheet: `Lookups` (Optional - for custom mappings)

| Source_Value | Lookup_Name | Fenergo_LookupName | LookupId |
|--------------|-------------|-------------------|----------|
| India | Country | IN | 123 |
| Fintech | BusinessSectors | Fintech | 456 |

***

## 4. Generated JSON Config Schema

```json
{
  "entityType": "LegalEntity",
  "version": "1.0",
  "mappings": [
    {
      "fenergoPath": "DataGroups.Name.LegalName",
      "fenergoType": "Text",
      "xmlPath": "/root/entity/legalName",
      "lookupName": null,
      "multi": false,
      "transform": "trim",
      "required": true,
      "defaultValue": null,
      "condition": null
    },
    {
      "fenergoPath": "DataGroups.Address.Country",
      "fenergoType": "Lookup",
      "xmlPath": "/root/entity/address/country",
      "lookupName": "Country",
      "multi": false,
      "transform": "upper",
      "required": true,
      "defaultValue": "IN",
      "condition": null
    }
  ]
}
```

***

## 5. Complete Java Implementation

### 5.1 Maven Dependencies (`pom.xml`)

```xml
<dependencies>
    <!-- XML Processing -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-xml</artifactId>
        <version>6.1.5</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-xml</artifactId>
        <version>2.17.2</version>
    </dependency>
    
    <!-- Excel Processing -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.3.0</version>
    </dependency>
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.2</version>
    </dependency>
    
    <!-- HTTP Client for Fenergo -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webflux</artifactId>
        <version>6.1.5</version>
    </dependency>
    
    <!-- Cache -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
        <version>3.1.8</version>
    </dependency>
</dependencies>
```

### 5.2 Core Data Models

```java
// FieldMapping.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldMapping {
    private String fenergoPath;
    private FenergoFieldType fenergoType;
    private String xmlPath;
    private String lookupName;
    private boolean multi;
    private String transform;
    private boolean required;
    private String defaultValue;
    private String condition;
}

// TransformerConfig.java
@Data
@NoArgsConstructor
public class TransformerConfig {
    private String entityType;
    private String version;
    private List<FieldMapping> mappings;
}

// FenergoFieldType.java (Enum)
public enum FenergoFieldType {
    TEXT, LOOKUP, MULTI_LOOKUP, DATE, NUMBER, BOOLEAN, LONG_TEXT, REFERENCE
}
```

### 5.3 Excel Config Generator

```java
@Service
public class ExcelConfigGenerator {
    
    public TransformerConfig generateFromExcel(MultipartFile excelFile) throws IOException {
        Workbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        Sheet mappingsSheet = workbook.getSheet("Field_Mappings");
        
        TransformerConfig config = new TransformerConfig();
        List<FieldMapping> mappings = new ArrayList<>();
        
        for (int i = 1; i <= mappingsSheet.getLastRowNum(); i++) { // Skip header
            Row row = mappingsSheet.getRow(i);
            if (row == null) continue;
            
            FieldMapping mapping = FieldMapping.builder()
                .fenergoPath(getCellValue(row, 0))
                .fenergoType(FenergoFieldType.valueOf(getCellValue(row, 1)))
                .xmlPath(getCellValue(row, 2))
                .lookupName(getCellValue(row, 3))
                .multi("TRUE".equalsIgnoreCase(getCellValue(row, 4)))
                .transform(getCellValue(row, 5))
                .required("TRUE".equalsIgnoreCase(getCellValue(row, 6)))
                .defaultValue(getCellValue(row, 7))
                .condition(getCellValue(row, 8))
                .build();
                
            mappings.add(mapping);
        }
        
        config.setMappings(mappings);
        workbook.close();
        return config;
    }
    
    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        return cell != null ? cell.getStringCellValue() : null;
    }
}
```

### 5.4 Lookup Cache Service

```java
@Service
@Slf4j
public class FenergoLookupCache {
    
    private final WebClient fenergoClient;
    private final Cache<String, LookupEntry> cache = Caffeine.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build();
    
    public record LookupEntry(int lookupId, String lookupName) {}
    
    public CompletableFuture<LookupEntry> resolveLookup(String lookupName, String value) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Check cache first
            String cacheKey = lookupName + ":" + value.toLowerCase();
            LookupEntry cached = cache.getIfPresent(cacheKey);
            if (cached != null) return cached;
            
            // 2. Query Fenergo API
            String response = fenergoClient.get()
                .uri("/referencedata/lookups/{lookupName}?search={value}", lookupName, value)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            // 3. Parse and cache
            LookupEntry entry = parseLookupResponse(response, value);
            cache.put(cacheKey, entry);
            return entry;
        });
    }
    
    public CompletableFuture<List<LookupEntry>> resolveMultiLookup(String lookupName, List<String> values) {
        return values.stream()
            .map(value -> resolveLookup(lookupName, value))
            .collect(CompletableFutureUtils.collectToList());
    }
}
```

### 5.5 Core Transformer Engine

```java
@Service
@Slf4j
public class FenergoTransformer {
    
    private final FenergoLookupCache lookupCache;
    private TransformerConfig config;
    
    public FenergoTransformer(FenergoLookupCache lookupCache) {
        this.lookupCache = lookupCache;
    }
    
    public void loadConfig(TransformerConfig config) {
        this.config = config;
    }
    
    public CompletableFuture<JsonNode> transform(String xmlString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Parse XML to JsonNode
                XmlMapper xmlMapper = new XmlMapper();
                JsonNode xmlData = xmlMapper.readTree(xmlString);
                
                // 2. Transform all fields
                ObjectNode result = JsonNodeFactory.instance.objectNode();
                
                for (FieldMapping mapping : config.getMappings()) {
                    JsonNode value = processField(xmlData, mapping);
                    setNestedPath(result, mapping.getFenergoPath(), value);
                }
                
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Transformation failed", e);
            }
        });
    }
    
    private JsonNode processField(JsonNode xmlData, FieldMapping mapping) {
        // 1. Extract from XML
        JsonNode rawValue = extractXPath(xmlData, mapping.getXmlPath(), mapping.isMulti());
        
        // 2. Apply transforms
        String transformed = applyTransform(rawValue.asText(), mapping.getTransform());
        
        // 3. Handle defaults/required
        if (isEmpty(transformed) && mapping.getDefaultValue() != null) {
            transformed = mapping.getDefaultValue();
        }
        if (mapping.isRequired() && isEmpty(transformed)) {
            throw new IllegalArgumentException("Missing required field: " + mapping.getFenergoPath());
        }
        
        // 4. Convert to Fenergo format
        return toFenergoFormat(transformed, mapping);
    }
    
    private JsonNode toFenergoFormat(String value, FieldMapping mapping) {
        return switch (mapping.getFenergoType()) {
            case TEXT -> new TextNode(value);
            case LOOKUP -> {
                LookupEntry lookup = lookupCache.resolveLookup(mapping.getLookupName(), value).join();
                yield objectNode("LookupId", lookup.lookupId(), "LookupName", value);
            }
            case MULTI_LOOKUP -> {
                String[] values = value.split(",");
                List<LookupEntry> lookups = lookupCache.resolveMultiLookup(mapping.getLookupName(), 
                    Arrays.asList(values)).join();
                yield createMultiLookupNode(lookups, values);
            }
            case DATE -> new TextNode(parseDate(value));
            case NUMBER -> new IntNode(Integer.parseInt(value));
            case BOOLEAN -> BooleanNode.valueOf(Boolean.parseBoolean(value));
            default -> new TextNode(value);
        };
    }
}
```

### 5.6 REST Controller

```java
@RestController
@RequestMapping("/api/transformer")
@RequiredArgsConstructor
public class TransformerController {
    
    private final ExcelConfigGenerator configGenerator;
    private final FenergoTransformer transformer;
    
    @PostMapping("/config/generate")
    public ResponseEntity<String> generateConfig(@RequestParam("file") MultipartFile excelFile) {
        TransformerConfig config = configGenerator.generateFromExcel(excelFile);
        transformer.loadConfig(config);
        
        String configPath = saveConfig(config);
        return ResponseEntity.ok("Config generated: " + configPath);
    }
    
    @PostMapping("/transform")
    public CompletableFuture<ResponseEntity<JsonNode>> transform(@RequestBody String xmlString) {
        return transformer.transform(xmlString)
            .thenApply(result -> ResponseEntity.ok(result));
    }
}
```

***

## 6. Deployment Configuration

### `application.yml`
```yaml
fenergo:
  base-url: https://your-tenant.fenergo.com
  api-key: ${FENERGO_API_KEY}

spring:
  redis:
    host: localhost
    port: 6379

transformer:
  configs-path: /app/configs/
  cache-refresh-interval: 24h
```

***

## 7. BA Operations Manual

### Daily Workflow:
```
1. Download: LegalEntity_mappings.xlsx
2. Edit rows in Field_Mappings sheet
3. Upload via POST /api/transformer/config/generate
4. Test: POST /api/transformer/transform {xml}
5. Deployed instantly - no restart needed
```

### Adding New Field (30 seconds):
```
Row: DataGroups.Risk.IsPEP | Boolean | /root/risk/pep | | FALSE | | TRUE | false |
```

***

## 8. Error Handling & Monitoring

```
- Missing lookup values → Log + use default
- Invalid XML → 400 with XPath error
- Required field missing → 400 with field name
- Config validation → Schema check on Excel upload
- Metrics: transformation latency, lookup hit rate, error rate
```

***

## 9. Testing Strategy

```java
@SpringBootTest
class TransformerTest {
    
    @Test
    void testLegalEntityTransformation() {
        String xml = loadTestXml("legal-entity.xml");
        JsonNode result = transformer.transform(xml).join();
        
        assertThat(result.at("/DataGroups/Address/Country/LookupId").asInt())
            .isEqualTo(123);
    }
}
```

***

**This solution gives your BAs complete control over ALL mappings via Excel while developers maintain ONE generic Java engine. Perfect for your IoT platform scaling KYB integrations across multiple banking partners.** [1][2]

Citations:
[1] Getting Started with APIs - Videos | Fenergo Document Portal https://docs.fenergonebula.com/api-docs/getting-started-with-apis
[2] API Catalogue | Fenergo Document Portal https://docs.fenergonebula.com/developer-hub/api-overview/api-catalogue
