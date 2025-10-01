Perfect 👍 let’s stitch this together.

Based on the three specs you gave me:

* **Entity API** (`entity.json`)
* **Journey Logic Engine API** (`Journey Logic Engine.json`)
* **Journey Command API** (`swagger.json`)

Here’s the **end-to-end flow**:

---

## 🔄 End-to-End Flow (Entity → Logic Engine → Journey Command)

### **Step 1: Create Entity**

Every journey needs to be tied to an entity (e.g., client, company, individual).

📌 **Request**

```
POST /api/entity
```

```json
{
  "data": {
    "type": "Company",              // or Individual
    "targetEntity": "Client",       // or Related Party
    "properties": {
      "name": "Acme Ltd",
      "jurisdiction": "US"
    },
    "policyJurisdictions": ["US"],
    "accessLayers": {}
  }
}
```

📌 **Response**

```json
{
  "data": {
    "id": "58e4b43a-a082-49a6-bf79-0cf8df2313c6",
    "type": "Company",
    "targetEntity": "Client"
  }
}
```

➡️ Save the **`entityId`**.

---

### **Step 2: Evaluate Journey Schema (Logic Engine)**

Now use entity attributes (type, jurisdiction, etc.) to let the **Logic Engine** decide which **journey schema** applies.

📌 **Request**

```
POST /journeylogicengine/api/engine/evaluate-journey-schema?journeyTypeFilter=Client Onboarding
```

```json
{
  "data": {
    "entityType": "Company",
    "jurisdiction": "US"
  }
}
```

📌 **Response**

```json
{
  "data": [
    {
      "journeySchemaId": "118313c6-91e6-4b05-a537-5a1ab906418f",
      "journeySchemaVersion": 1,
      "name": "US Corporate Onboarding",
      "identifier": "US_CORP_ONB",
      "journeyType": "Client Onboarding",
      "channelType": "Internal"
    }
  ]
}
```

➡️ Save the **`journeySchemaId`**.

---

### **Step 3a: (Option 1) Create & Launch Journey Instance**

If you want control (create first, launch later):

**Create Instance**

```
POST /api/journey-instance
```

```json
{
  "data": {
    "journeySchemaId": "118313c6-91e6-4b05-a537-5a1ab906418f",
    "entityId": "58e4b43a-a082-49a6-bf79-0cf8df2313c6"
  }
}
```

➡️ Returns `journeyInstanceId`.

**Launch Instance**

```
POST /api/journey-instance/{journeyInstanceId}/launch
```

```json
{}
```

---

### **Step 3b: (Option 2) Direct Launch Journey**

If you don’t care about manual creation, you can **skip Step 2 + 3a** and go straight to:

```
POST /api/journey-instance/launch-journey
```

```json
{
  "data": {
    "entityId": "58e4b43a-a082-49a6-bf79-0cf8df2313c6",
    "journeyType": "Client Onboarding",
    "jurisdictions": ["US"]
  }
}
```

➡️ The system will pick the **right schema** for that entity + journey type, using internal rules.

---

## ⚡ When to Use Which?

* **With Logic Engine (Step 2)** → When you want the platform to dynamically select the correct schema based on entity data + conditions.
* **Direct Launch** → When you already know the entity and journey type (and don’t need schema evaluation).
* **Create + Launch** → When you need more control (e.g., storing instance in Created state before launching).

---

👉 Do you want me to build a **sequence diagram** (visual flow) for these three approaches, so it’s crystal clear which APIs talk to which?
