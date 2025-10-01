Nice — here’s a deeper, practical dive into the full flow (Entity → Logic Engine → Journey Command), with exact request/response shapes, decision logic, pitfalls, tests, and recommended patterns you can use in real systems.

# 1) Quick recap of the three APIs (roles)

* **Entity API** (`/entity`) — create/manage the *entity* (client / related party / etc.). You get an `entityId` that journeys attach to.
* **Journey Logic Engine** (`/engine/evaluate-journey-schema`) — rule engine: you give it key/value attributes (usually taken from the entity) and it returns matching **journeySchemaId(s)** (and version, name, journeyType). Use when you want the system to pick the correct schema.
* **Journey Command API** (`/journey-instance`, `/launch-journey`, `/…/launch`) — creates a journey instance (optionally), launches it, pauses/unpauses/aborts, etc.

---

# 2) When to use which approach (decision tree)

1. **You want the platform to pick the right schema automatically for the entity →**
   Use **Logic Engine** first to evaluate attributes → pick `journeySchemaId` → **create** instance with that schema (or directly pass schemaId to create), then **launch**.

2. **You already know the journeyType and are OK with internal selection of schema →**
   Use **Direct Launch** (`POST /api/journey-instance/launch-journey`) with `entityId + journeyType` — the platform will pick the published schema and start the journey in one call.

3. **You need manual control / setup before starting** (e.g., stage/task pre-population, attach files, set timers, assign people) →
   **Create** instance (`POST /api/journey-instance`) with `journeySchemaId` and required data, store `journeyInstanceId`, do your pre-work, then **launch** (`POST /api/journey-instance/{id}/launch`).

---

# 3) Exact payloads, headers and examples

All requests should include:

* `Authorization: Bearer <token>`
* `X-TENANT-ID: <tenant-uuid>`
* (recommended) `X-CORRELATION-ID: <uuid>` for tracing

### A — Create Entity

```
POST /entity
Headers: X-TENANT-ID, Authorization
Body:
{
  "data": {
    "type": "Company",
    "targetEntity": "Client",
    "properties": {
      "name": { "type": "Single", "value": "Acme Ltd" },
      "jurisdiction": { "type": "Single", "value": "US" }
    },
    "policyJurisdictions": ["US"]
  }
}
```

Response includes `entityId` (UUID). Save it.

### B — Evaluate Journey Schema (Logic Engine)

```
POST /journeylogicengine/api/engine/evaluate-journey-schema?journeyTypeFilter=Client Onboarding
Headers: X-TENANT-ID, Authorization
Body:
{
  "data": {
    "entityType": "Company",
    "jurisdiction": "US",
    "someOtherAttr": "value"
  }
}
```

Response:

```json
{
  "data": [
    {
      "journeySchemaId": "118313c6-...",
      "journeySchemaVersion": 2,
      "name": "US Corporate Onboarding",
      "identifier": "US_CORP_ONB",
      "journeyType": "Client Onboarding",
      "channelType": "Internal"
    }
  ]
}
```

If multiple schemas returned, pick based on your rules (see selection guidance below).

### C1 — Create Journey Instance (optional)

```
POST /api/journey-instance
Headers: X-TENANT-ID, Authorization
Body:
{
  "data": {
    "journeySchemaId": "118313c6-...",
    "entityId": "58e4b43a-...",
    "journeySchemaVersionNumber": 2,        // optional: lock version
    "jurisdictions": ["US"],
    "accessLayers": { ... }                 // optional
  }
}
```

Response: returns `journeyInstanceId`.

### C2 — Launch (two-step)

```
POST /api/journey-instance/{journeyInstanceId}/launch
Headers: X-TENANT-ID, Authorization, X-CORRELATION-ID
Body: {}   // often empty — or include comment/initiator if supported by your installation
```

Response: `202 Accepted` (launch workflow initialized). Use Journey Query API to check status.

### D — Direct Launch (one-step)

```
POST /api/journey-instance/launch-journey
Headers: X-TENANT-ID, Authorization, X-CORRELATION-ID
Body:
{
  "data": {
    "entityId": "58e4b43a-...",
    "journeyType": "Client Onboarding",    // optional but helpful
    "jurisdictions": ["US"],
    "accessLayers": { ... }                // optional
  }
}
```

Response: `202 Accepted` with `JourneyLaunchedDtoServiceResponse`.

---

# 4) Schema selection rules & best practices (how to pick when multiple matches)

If the Logic Engine returns >1 schema:

1. **Filter by requested `journeyType`** (if supplied).
2. Prefer **rules that explicitly match jurisdiction** (if your business requires jurisdiction-specific flows).
3. Prefer **highest `journeySchemaVersion`** (unless you intentionally want an older version).
4. Respect **channelType** (if you filtered for internal vs external).
5. If you need determinism, implement explicit tie-breaker logic in your client (e.g., prefer `identifier` matching a configured list).

**Tip:** If you call Logic Engine and will immediately create instance, include the chosen `journeySchemaVersionNumber` when creating instance to lock the version (avoids race if an admin publishes a new schema right after your eval).

---

# 5) Launch controls, permissions and governance

* Launch endpoints require specific permissions (e.g., `JourneyCreate`, `JourneyPauseInstance`, etc.). If you get `403`, check user token scopes/roles.
* Admins can create **Journey Launch Controls** (rules that may block or alter launching behaviour). If direct `launch-journey` doesn’t behave as expected, check if launch controls exist in your tenant.
* **X-CORRELATION-ID** is recommended for observability and troubleshooting.

---

# 6) Error handling & important HTTP codes

* **202 Accepted** — request accepted, processing asynchronous (common for create/launch). Check Journey Query API for final state.
* **400 Bad Request** — missing/invalid payload (inspect validation error model in response).
* **401 Unauthorized** — missing/invalid token.
* **403 Forbidden** — missing permissions.
* **404 Not Found** — resource id missing (e.g., entity or instance not found).
* **409 Conflict** — version conflict (e.g., when saving drafts or creating with concurrent edits).
* **500 Internal Server Error** — backend error.

**When you receive 202:** your call triggered backend processing — it does not mean the whole business workflow completed. Poll or subscribe to journey status.

---

# 7) Concurrency & atomicity considerations

* **Race between evaluate → create → launch**: If you evaluate schema and then wait, an admin may publish a new schema. To avoid mismatches:

    * Include `journeySchemaVersionNumber` in create to lock the intended version.
    * Or create-and-launch quickly in a transaction (if your installation supports that).
* **Idempotency**: APIs don’t show a built-in idempotency header — consider generating your own `X-CORRELATION-ID` and checking server behavior for duplicate launches. If repeated launches are a risk, implement server-side checks (e.g., “is there already an in-progress journey for this entity+type?”) before launching.
* **Multiple launches for same entity**: define business rules. Some journey types (Offboarding/Re-onboarding) might allow multiple concurrent journeys; others (Onboarding) may need a single active instance.

---

# 8) Drafts & Verify step (useful pattern)

* If you need to collect data before starting the journey:

    1. Create `entity` (or update existing).
    2. Create **Entity Draft** (`POST /api/entity/{entityId}/draft`) with `journeyId` if you want the draft tied to a particular journey.
    3. Update/populate draft, run `verify` (`PUT .../draft/{id}/verify`) to run validations.
    4. When draft is ready, **launch** journey (direct or create+launch).
       This pattern helps if business requires human review before starting automated tasks.

---

# 9) Tests you should run in non-prod

1. **Happy path**: create entity → evaluate → create instance → launch → confirm instance state via Query API.
2. **Direct launch**: create entity → direct `launch-journey` → confirm journey started.
3. **No schema match**: evaluate with attributes that match no schema — validate behavior and your fallback.
4. **Launch controls**: set an admin-level launch control that blocks launch and confirm the API returns expected error/messages.
5. **Permissions**: call launch with token missing `JourneyCreate` and confirm `403`.
6. **Version conflict**: simulate schema version change between evaluate and create; ensure you can lock a version on create.

---

# 10) Practical cURL examples

**A. Evaluate (Logic Engine)**

```bash
curl -X POST "https://<host>/journeylogicengine/api/engine/evaluate-journey-schema?journeyTypeFilter=Client%20Onboarding" \
 -H "Authorization: Bearer <token>" \
 -H "X-TENANT-ID: <tenant-uuid>" \
 -H "Content-Type: application/json" \
 -d '{
  "data": {
    "entityType": "Company",
    "jurisdiction": "US"
  }
}'
```

**B. Direct Launch**

```bash
curl -X POST "https://<host>/api/journey-instance/launch-journey" \
 -H "Authorization: Bearer <token>" \
 -H "X-TENANT-ID: <tenant-uuid>" \
 -H "X-CORRELATION-ID: <uuid>" \
 -H "Content-Type: application/json" \
 -d '{
  "data": {
    "entityId": "58e4b43a-...",
    "journeyType": "Client Onboarding",
    "jurisdictions": ["US"]
  }
}'
```

**C. Create instance then launch**

```bash
# create
curl -X POST "https://<host>/api/journey-instance" \
 -H "Authorization: Bearer <token>" \
 -H "X-TENANT-ID: <tenant-uuid>" \
 -H "Content-Type: application/json" \
 -d '{
  "data": {
    "journeySchemaId": "118313c6-...",
    "entityId": "58e4b43a-...",
    "journeySchemaVersionNumber": 2
  }
}'

# launch (after you get journeyInstanceId)
curl -X POST "https://<host>/api/journey-instance/<journeyInstanceId>/launch" \
 -H "Authorization: Bearer <token>" \
 -H "X-TENANT-ID: <tenant-uuid>" \
 -H "X-CORRELATION-ID: <uuid>" \
 -H "Content-Type: application/json" \
 -d '{}'
```

---

# 11) Troubleshooting quick-check

* `403` → missing permission (JourneyCreate / EntityDataEdit).
* `400` → schema mismatch in body (validate against DTOs in swagger).
* `202` but no activity → verify backend listeners, message queue health (journey launch is likely async).
* No matching schema from Logic Engine → check input keys (Logic Engine expects specific keys configured in launch controls/conditions) and check cache/logic rules. You can call `cache/flush` in Logic Engine only as a maintenance step (requires JourneyBuilderDelete role).

---

# 12) Summary & recommended pattern

* Use **Logic Engine** when you need the platform to choose the correct schema based on entity attributes and business rules.
* Use **Direct Launch** when you have entityId and journeyType and want a single-call flow.
* Use **Create+Launch** when you need to set up instance state before starting (pre-population, approvals, attachments).
* Always include tenant, auth, and correlation-id headers. Lock schema version when you need determinism.

---

If you want, I can now:

* produce a **ready-to-run Postman collection** for both the two-step and direct-launch flows, or
* generate a **sequence diagram** (SVG/ASCII) showing all interactions and error branches, or
* pick one real example from your environment values (tenant id, example entity properties) and produce exact payloads ready to paste.

Which of those would help you most next?
