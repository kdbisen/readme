now consider you are fully new person in this fenergo world, thn ask all mapping level questions and create documents of it as FAQS .


ALL QUESTIONS

# FENERGO MAPPING FAQS - COMPLETE NEWBIE GUIDE
## 100+ Mapping-Level Questions Every Developer MUST Ask

**"I'm brand new to Fenergo. What do I need to know about EVERY mapping detail?"**

***

## 🎯 **1. BASIC STRUCTURE QUESTIONS**

### **Q1: What does a MINIMAL LegalEntity creation JSON look like? Exactly.**
**A:**
```json
{
  "TenantId": "TENANT123",
  "EntityType": {"LookupId":1,"LookupName":"LegalEntity"},
  "DataGroups": {
    "Name": {"LegalName": "Test Corp Pvt Ltd"}
  }
}
```

### **Q2: Is TenantId a string or number? Case sensitive?**
**A:** **String**, case-sensitive: `"TENANT123"` ≠ `"tenant123"`

### **Q3: Where do I get valid LookupId for EntityType=LegalEntity?**
**A:** `GET /referencedata/lookups/EntityType` → Find `"LookupName":"LegalEntity"` → use its `LookupId`

***

## 📋 **2. DATA GROUPS - EVERY SINGLE ONE**

### **Q4: List ALL possible DataGroups for LegalEntity. No exceptions.**
**A:**
```
Name, Addresses, Contacts, Identifications, Incorporation, Business, Risk, 
Regulatory, Ownership, RelatedParties, Documents, CustomFields
```

### **Q5: Are Addresses/Identifications arrays or single objects?**
**A:** **ARRAYS** - even for 1 item:
```json
"Addresses": [{"AddressType":..., "Country":..., "AddressLine1":...}]
```

### **Q6: What is EXACTLY inside Name group? All fields.**
**A:**
```json
"Name": {
  "LegalName": "ABC Pvt Ltd",      // Text
  "ShortName": "ABC",             // Text  
  "TradingName": "ABC Trading",   // Text
  "PreviousNames": ["OldName1"]   // Array<Text>
}
```

### **Q7: What fields are REQUIRED in Address group?**
**A:** `AddressLine1`, `City`, `Country`, `AddressType` **minimum**

***

## 🔍 **3. LOOKUP MAPPING - EVERY DETAIL**

### **Q8: For Country="IN", what is the EXACT JSON structure?**
**A:**
```json
"Country": {
  "LookupId": 123,
  "LookupName": "IN"
}
```

### **Q9: Do I need BOTH LookupId AND LookupName? What happens if missing one?**
**A:** **BOTH REQUIRED**. Missing either → `LOOKUP_INVALID` error.

### **Q10: How do I know what LookupName to use? "India" or "IN"?**
**A:** **LookupName = short code** ("IN"), **DisplayName = full** ("India")

### **Q11: Show me the API call to get Country lookup.**
**A:** `GET /referencedata/lookups/Country?tenantId=TENANT123`

**Response:**
```json
[
  {"LookupId":123,"LookupName":"IN","DisplayName":"India","Active":true},
  {"LookupId":124,"LookupName":"US","DisplayName":"United States","Active":true}
]
```

### **Q12: What if my source data has "India" but Fenergo wants "IN"?**
**A:** **Transform in config**: `Transform: upper` → "INDIA" → map to "IN"

***

## 📄 **4. FIELD TYPE MAPPING - ALL FORMATS**

### **Q13: Show Text, Lookup, MultiLookup, ALL side-by-side.**
**A:**
```json
{
  "LegalName": "ABC Pvt Ltd",                                    // Text
  "Country": {"LookupId":123,"LookupName":"IN"},                 // Lookup
  "Sectors": {"LookupId":456,"Values":[{"LookupId":456,"LookupName":"Fintech"}]}, // MultiLookup
  "RiskScore": 7,                                               // Number
  "IsPEP": true,                                                // Boolean
  "IssueDate": "2025-01-15"                                     // Date
}
```

### **Q14: Currency field - EXACT structure?**
**A:**
```json
"AnnualTurnover": {
  "Amount": 10000000,
  "Currency": {"LookupId":1,"LookupName":"INR"}
}
```

### **Q15: How to add 2nd address? Exact JSON.**
**A:**
```json
"Addresses": [
  {"AddressType":{"LookupId":1,"LookupName":"Registered"},"Country":{"LookupId":123,"LookupName":"IN"},"AddressLine1":"123 St"},
  {"AddressType":{"LookupId":2,"LookupName":"Business"},"Country":{"LookupId":123,"LookupName":"IN"},"AddressLine1":"456 St"}
]
```

***

## 🔗 **5. ARRAY MAPPING QUESTIONS**

### **Q16: XML has `<address><country>IN</country></address><address><country>US</country></address>` - how to map?**
**A:**
```
Excel Row: DataGroups.Addresses[].Country | Lookup | /root/address/country | Country | TRUE | upper | TRUE | |
```
**Note:** `Multi_Select=TRUE`, `Array=TRUE`

### **Q17: My XML has fixed positions. How to map Addresses, Addresses[1]?**
**A:**
```
DataGroups.Addresses[0].Country | Lookup | /root/addresses/country[1] | Country | FALSE | | FALSE | |
DataGroups.Addresses[1].Country | Lookup | /root/addresses/country[2] | Country | FALSE | | FALSE | |
```

### **Q18: Multiple GSTIN/PAN - exact mapping?**
**A:**
```
DataGroups.Identifications[0].IdType | Lookup | /root/ids/type[1] | IdTypes | FALSE | | FALSE | GSTIN |
DataGroups.Identifications[0].IdNumber | Text | /root/ids/number[1] | | FALSE | | FALSE | |
DataGroups.Identifications[1].IdType | Lookup | /root/ids/type[2] | IdTypes | FALSE | | FALSE | PAN |
DataGroups.Identifications[1].IdNumber | Text | /root/ids/number[2] | | FALSE | | FALSE | |
```

***

## ⚙️ **6. EXCEL CONFIG - EVERY COLUMN EXPLAINED**

### **Q19: EXACT Excel column order and validation rules?**
**A:**

| A | B | C | D | E | F | G | H | I |
|---|---|---|---|---|---|---|---|---|
| **Fenergo_Path** | **Fenergo_Type** | **XML_Path** | **Lookup_Name** | **Multi** | **Transform** | **Required** | **Default** | **ArrayIndex** |
| `DataGroups.Name.LegalName` | `Text` | `/root/name` | `` | `FALSE` | `trim` | `TRUE` | `` | `` |

**Validation:**
- Col B: `Text|Lookup|MultiLookup|Date|Number|Boolean|Currency`
- Col E: `TRUE|FALSE`
- Col G: `TRUE|FALSE`

### **Q20: What are ALL valid Transform functions? Examples.**
**A:**
```
trim → " ABC " → "ABC"
upper → "india" → "IN"  
lower → "INDIA" → "in"
capitalize → "fintech" → "Fintech"
date(YYYY-MM-DD) → "15/01/2025" → "2025-01-15"
number → "1,000" → 1000
split(,) → "A,B,C" → ["A","B","C"]
currency:INR → "1000000 INR" → {"Amount":1000000,"Currency":{"LookupId":1,"LookupName":"INR"}}
```

***

## 🚨 **7. ERROR MAPPING QUESTIONS**

### **Q21: I get "LOOKUP_INVALID for Country". Exact fix?**
**A:**
1. Check: `GET /lookups/Country` → verify "IN" exists
2. Verify: sending `{"LookupId":123,"LookupName":"IN"}` (both fields)
3. Cache issue? Clear Redis + retry

### **Q22: "FIELD_REQUIRED DataGroups.Name.LegalName" - but I sent it?**
**A:** Sent empty string `""`. Use `Default_Value` or validate `trim` result.

### **Q23: "ARRAY_INDEX_OUT_OF_BOUNDS Addresses[2]"?**
**A:** XML has 2 addresses, you mapped `Addresses[2]`. Use `Addresses[].Country` for dynamic.

***

## 🔗 **8. RELATIONSHIP MAPPING**

### **Q24: How to link Director (Individual) to LegalEntity?**
**A:**
```json
"RelatedParties": [{
  "RelationshipType": {"LookupId":10,"LookupName":"Director"},
  "RelatedEntity": {
    "EntityId": "IND-456789",
    "EntityType": {"LookupId":2,"LookupName":"Individual"}
  }
}]
```

### **Q25: UBO with ownership %?**
**A:**
```json
{
  "RelationshipType": {"LookupId":11,"LookupName":"UBO"},
  "RelatedEntity": {"EntityId":"IND-123","EntityType":{"LookupId":2,"LookupName":"Individual"}},
  "OwnershipPercentage": 25.5
}
```

***

## ⚡ **9. PERFORMANCE MAPPING**

### **Q26: Should I cache ALL lookups on startup? How?**
**A:**
```java
@PostConstruct
public void initCache() {
  Map<String,List<LookupEntry>> allLookups = fenergoClient.get()
    .uri("/referencedata/lookups")
    .retrieve()
    .bodyToMono(new TypeReference<>() {})
    .block();
  cache.putAll(allLookups);
}
```

### **Q27: Bulk transform 100 entities - best pattern?**
**A:**
```java
CompletableFuture.allOf(
  entities.stream()
    .map(xml -> transformer.transform(xml))
    .toArray(CompletableFuture[]::new)
).thenApply(v -> results.stream().map(CompletableFuture::join).collect(toList()))
```

***

## ✅ **10. VALIDATION CHECKLIST**

```
□ TenantId correct format?
□ EntityType has LookupId+LookupName?
□ ALL Lookup fields have LookupId+LookupName?
□ Addresses/Identifications are ARRAYS?
□ Currency has nested Currency lookup?
□ MultiLookup has "Values" array?
□ Dates in YYYY-MM-DD?
□ Numbers are integers (no decimals unless specified)?
□ Required fields have values OR defaults?
```

**Copy this FAQ. Answer YES to all checklist items before every POST.** [3][4][5]

Citations:
[1] Fenergo: Financial Compliance Software | CLM and KYC https://www.fenergo.com
[2] Discover Fenergo https://www.fenergo.com/company/discover-fenergo
[3] Journey Policy and Entity Data Overview https://docs.fenergonebula.com/developer-hub/journey-policy-and-entity-data/overview
[4] Getting Started with APIs - Videos | Fenergo Document Portal https://docs.fenergonebula.com/api-docs/getting-started-with-apis
[5] API Catalogue | Fenergo Document Portal https://docs.fenergonebula.com/developer-hub/api-overview/api-catalogue
