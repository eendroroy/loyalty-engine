# Requirements Document — Loyalty Management System

> Version: 0.1.0  
> Date: 2026-03-31  
> Author: eendroroy  
> Status: **IMPLEMENTED** ✅

---

## 1. Purpose

This document defines the functional and non-functional requirements for the
**Loyalty Management System** backend. The system provides a comprehensive REST API 
consumed by an enhanced React admin panel to configure ingestion sources, define 
dynamic reward rules, and manage reward distribution.

**Recent Implementation**: Enhanced data source management with multi-step forms, 
schema-first architecture, and simplified webhook configuration.

---

## 2. Scope

| In Scope | Out of Scope |
|---|---|
| ✅ **Enhanced Admin REST API** (data sources, rules, metadata, monitor) | End-user-facing customer API |
| ✅ **Multi-step Data Source Configuration** (schema, files, webhooks) | Payment gateway integration |
| ✅ **Schema-First Architecture** with auto-synced webhook properties | Customer account management |
| ✅ **Advanced File Management** with CSV parsing options | Reporting / analytics dashboards |
| ✅ **Simplified Webhook Model** with auto-generated paths | Authentication / authorisation (future) |
| ✅ **Real-time Validation** and sample request generation | Kafka consumer logic (future) |
| ✅ **Archive Management** with purge capabilities | Data archival / GDPR deletion |
| ✅ **TypeScript Integration** with comprehensive error handling | SCHEDULED cron-based polling (future) |

---

## 3. Stakeholders

| Role | Responsibility |
|---|---|
| Loyalty Admin | Configures data sources and authors rules via the admin panel |
| Developer | Builds and maintains the backend and React frontend |
| DevOps | Manages PostgreSQL, Kafka, and deployment infrastructure |

---

## 4. Functional Requirements

### 4.1 Data Source Management ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| DS-01 | The system shall provide a **multi-step wizard interface** for creating data sources with name, description, and destination table configuration. | ✅ **IMPLEMENTED** |
| DS-02 | The system shall support **schema-first architecture** where destination table columns are defined as reusable schema fields. | ✅ **IMPLEMENTED** |
| DS-03 | The system shall provide **real-time validation** with immediate feedback during form completion. | ✅ **IMPLEMENTED** |
| DS-04 | The system shall support **atomic transactions** for creating/updating data sources with all nested entities. | ✅ **IMPLEMENTED** |
| DS-05 | The system shall provide **archive management** with soft-delete capabilities and purge options for data/tables. | ✅ **IMPLEMENTED** |
| DS-06 | The system shall generate **comprehensive review summaries** before saving configurations. | ✅ **IMPLEMENTED** |
| DS-07 | The system shall support **tabular field management** with DataGrid interfaces for complex configurations. | ✅ **IMPLEMENTED** |
| DS-08 | Operations referencing non-existent data source IDs must return `404 Not Found`. | ✅ **IMPLEMENTED** |

### 4.2 Enhanced Webhook Management ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| DSW-01 | Each data source shall have **exactly one webhook configuration** with enable/disable toggle. | ✅ **IMPLEMENTED** |
| DSW-02 | The system shall **auto-generate webhook paths** in format `POST /web-hook?dataSourceName={name}`. | ✅ **IMPLEMENTED** |
| DSW-03 | Webhook properties shall **auto-sync from schema fields**, eliminating duplicate definitions. | ✅ **IMPLEMENTED** |
| DSW-04 | The system shall provide **description support** for documenting webhook purpose and usage. | ✅ **IMPLEMENTED** |
| DSW-05 | The system shall **auto-generate sample request bodies** based on schema field definitions. | ✅ **IMPLEMENTED** |
| DSW-06 | Webhook configuration shall include **visual status indicators** and real-time path updates. | ✅ **IMPLEMENTED** |
| DSW-07 | The system shall support **JSON payload ingestion** with automatic type coercion based on schema fields. | ✅ **IMPLEMENTED** |

### 4.3 Advanced File Management ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| DSF-01 | The system shall support **comprehensive CSV parsing configuration** including separators, quotes, and line handling. | ✅ **IMPLEMENTED** |
| DSF-02 | File field mappings shall **reference schema fields** with dropdown selection and validation. | ✅ **IMPLEMENTED** |
| DSF-03 | The system shall provide **tabular field mapping interfaces** with column number and name-based matching. | ✅ **IMPLEMENTED** |
| DSF-04 | File configurations shall support **real-time validation** with immediate feedback on mapping errors. | ✅ **IMPLEMENTED** |
| DSF-05 | The system shall maintain **file path uniqueness** across the entire system. | ✅ **IMPLEMENTED** |
| DSF-06 | File deletion shall trigger **automatic cleanup** of associated field mappings and processing logs. | ✅ **IMPLEMENTED** |

### 4.4 Data Source Webhook Management

The implemented model simplifies webhook management to a **single auto-managed webhook per data source**.

| ID | Requirement |
|----|-------------|
| DSW-01 | Each data source has **exactly one** `DataSourceWebhook` record (created automatically on source creation; `@OneToOne`). |
| DSW-02 | The webhook endpoint is auto-generated as `POST /web-hook?dataSourceName={name}` — no manual path configuration. |
| DSW-03 | Webhook has an `enabled` flag (default `false`); the ingestion controller rejects payloads with `503` when `enabled = false`. |
| DSW-04 | Webhook properties are **auto-synced from schema fields** via `DataSourceServiceImpl.syncWebhookPropertiesToSchemaFields()` on every schema change — no separate property management endpoints. |
| DSW-05 | The webhook record stores a `description` field for documentation purposes. |
| DSW-06 | `secretKey` / HMAC signature verification is not yet implemented (future work). |

### 4.5 Data Source Field Management

Field definitions belong to individual `DataSourceFile` records (not to `DataSource`). This
allows each file under a data source to declare its own schema independently.

| ID | Requirement |
|----|-------------|
| DF-01 | Each `DataSourceFile` may have zero or more typed field definitions. Fields are owned by the file, not the parent data source. |
| DF-02 | A field definition must include: `fieldName`, `fieldAlias`, `dataType`. `columnNumber` is optional (CSV FILE sources only; if omitted the ingestion engine matches by `fieldName` against the CSV header). |
| DF-03 | `fieldAlias` must be **globally unique** across all data sources and files, and must match the pattern `^[a-zA-Z][a-zA-Z0-9._]*$`. |
| DF-04 | Valid `dataType` values are: `STRING`, `INTEGER`, `DECIMAL`, `DATE`, `BOOLEAN`. |
| DF-05 | Individual fields may be added, updated, or deleted via nested endpoints (`/data-sources/{id}/files/{fileId}/fields/{fieldId}`). |
| DF-06 | Updating a field must support partial updates — null request values must leave stored values unchanged. |
| DF-07 | The system must prevent insertion of a duplicate `fieldAlias` even when replacing fields in bulk (JPQL bulk DELETE before INSERT). |

### 4.6 Rule Management

| ID | Requirement |
|----|-------------|
| R-01 | The system shall allow creating a rule with: name, description, ruleExpression, status, priority, optional activeFrom/activeTo, and optional cron frequency. |
| R-02 | A rule's `status` lifecycle must follow: `DRAFT → ACTIVE → INACTIVE`. Default status on creation is `DRAFT`. |
| R-03 | Rules must be evaluated in descending priority order (higher number = higher priority). |
| R-04 | The system must store the `lastRunAt` timestamp updated after each evaluation cycle. |
| R-05 | Deleting a rule must cascade to all its reward actions. |
| R-06 | The system must return all rules with their embedded actions in the list response. |

### 4.7 Rule Action Management

| ID | Requirement |
|----|-------------|
| RA-01 | Each rule may have one or more reward actions. |
| RA-02 | A reward action must include a `rewardType` (`POINT` or `VOUCHER`) and a `rewardAmount` (string). |
| RA-03 | Reward actions may be added, updated, or deleted independently via nested endpoints. |

### 4.8 Rule Expression Language

| ID | Requirement |
|----|-------------|
| RE-01 | The rule DSL syntax is: `WHEN <condition> [AND\|OR <condition>]... THEN <amount> <RewardType>`. |
| RE-02 | Supported comparison operators: `>=`, `<=`, `>`, `<`, `=`, `!=`. |
| RE-03 | The built-in variable `DATE` resolves to the current date in `yyyy-MM-dd` format. |
| RE-04 | Field references in expressions must use `fieldAlias` values registered on `DataSourceField`. |
| RE-05 | Rule evaluation is stubbed (`TODO`) pending `RuleEvaluationService` implementation. |

### 4.9 Data Ingestion — CSV FILE_WATCHER

| ID | Requirement |
|----|-------------|
| DI-01 | `DataSourceFile` entries must be registered with `FileWatcherService` on application start. |
| DI-02 | On `ENTRY_CREATE` or `ENTRY_MODIFY` NIO events for a watched directory, `DataPullService.pull()` must be invoked for the affected file. |
| DI-03 | Before parsing, the system must insert an `IN_PROGRESS` row in `file_processing_log` with a unique constraint on `(data_source_file_id, file_path, last_modified_millis, file_size_bytes)`. Competing instances that lose the insert race must skip silently. |
| DI-04 | The ingestion engine must load the **file-specific** field definitions for each `DataSourceFile`. CSV fields with `columnNumber` must be resolved by 1-based index; fields without `columnNumber` must be matched case-insensitively by `fieldName` against the header row of that specific file. |
| DI-05 | Field values must be type-coerced to `Long`, `BigDecimal`, `LocalDate`, `Boolean`, or `String` according to `dataType`. |
| DI-06 | On success, the log row must be updated to `COMPLETED` and a `DataPulledEvent` published. On failure, the log row must be deleted (so the file is retried on the next event). |
| DI-07 | `DataPullService` must return a `DataPullResult(rowsIngested, rowsSkipped, errors)` summary. |
| DI-08 | File-watcher registration must be hot-reloaded when a `DataSourceFile` is saved or deleted (via `@TransactionalEventListener`). |

### 4.9 Data Ingestion — Webhook (HTTP Push)

| ID | Requirement |
|----|-------------|
| DI-09 | External services push JSON payloads to `POST /web-hook?dataSourceName={name}`. The system must coerce each value according to the data source's `DataSourceSchemaField` definitions (JSON key = schema field name → typed destination-column value) and insert a row into the destination table. |
| DI-10 | After successful ingestion, a `DataPulledEvent` must be published for downstream consumers (e.g. `RuleEvaluationService`). |
| DI-11 | The webhook ingestion endpoint must return `200 OK` with a `DataPullResult` body, `404` if the webhook ID is not found, or `400` for a null/empty payload. |
| DI-12 | The date-format `format` field on `DataSourceWebhookProperty` must be applied when parsing DATE values; blank/null defaults to ISO-8601. |

### 4.9 Metadata API

| ID | Requirement |
|----|-------------|
| M-01 | `GET /api/admin/metadata` must return all field aliases grouped by data source with their data types and descriptions. |
| M-02 | The metadata response is consumed by the rule-expression editor in the React admin panel for autocomplete. |

### 4.10 Monitoring API

| ID | Requirement |
|----|-------------|
| MON-01 | `GET /api/admin/monitors/file-watchers` must return all registered `DataSourceFile` entries with live watching status, most recent `FileProcessingLog` summary, and cumulative completed/failed counts. |
| MON-02 | `GET /api/admin/monitors/webhooks` must return all configured `DataSourceWebhook` endpoints with their parent source details. |
| MON-03 | `GET /api/admin/monitors/logs` must return a paginated list of `FileProcessingLog` records sorted newest-first. |

---

## 5. Non-Functional Requirements

### 5.1 Performance

| ID | Requirement |
|----|-------------|
| NF-P1 | All list endpoints must use `LEFT JOIN FETCH` JPQL to avoid N+1 query issues. |
| NF-P2 | OSIV (Open Session in View) must be disabled (`spring.jpa.open-in-view: false`). |
| NF-P3 | HikariCP connection pool: max 10 connections, min idle 5. |

### 5.2 Consistency and Integrity

| ID | Requirement |
|----|-------------|
| NF-C1 | All write operations must be executed within a `@Transactional` service method. |
| NF-C2 | Event listeners (`DataSourceFileSavedEvent` etc.) must fire only after the DB transaction commits (`AFTER_COMMIT`). |
| NF-C3 | `fieldAlias` global uniqueness must be enforced at both the DB level (unique constraint) and application level (pattern validation). |
| NF-C4 | `DataSourceWebhook.endpoint` must be globally unique (DB unique constraint). |
| NF-C5 | Schema changes are reflected via `ddl-auto: update` — Hibernate applies DDL automatically from entity definitions. |

### 5.3 API Design

| ID | Requirement |
|----|-------------|
| NF-A1 | All endpoints are prefixed `/api/admin/`. |
| NF-A2 | `POST` returns `201 Created` with the created resource. |
| NF-A3 | `PUT` returns `200 OK` with the updated resource (PATCH semantics for null fields). |
| NF-A4 | `DELETE` returns `204 No Content`. |
| NF-A5 | Missing resources return `404 Not Found`. |
| NF-A6 | All date/time fields use ISO-8601 format (no epoch timestamps). |
| NF-A7 | Response DTOs use `@JsonInclude(NON_NULL)` — null fields are omitted. |
| NF-A8 | OpenAPI 3.1 spec is auto-generated by SpringDoc and available at `/v3/api-docs`. |
| NF-A9 | Swagger UI is available at `/swagger-ui.html` with try-it-out enabled. |

### 5.4 Observability

| ID | Requirement |
|----|-------------|
| NF-O1 | Spring Boot Actuator endpoints: `health`, `info`, `metrics`, `loggers`, `scheduledtasks`, `beans`, `env`. |
| NF-O2 | Structured rolling log files via Logback; max 10 MB per file, 30-day history, 300 MB total cap. |
| NF-O3 | Log levels must be adjustable at runtime via `/actuator/loggers`. |

### 5.5 Security (Future)

| ID | Requirement |
|----|-------------|
| NF-S1 | Admin endpoints must be secured with authentication/authorisation before production deployment. |
| NF-S2 | Actuator `env` and `beans` endpoints must be restricted in production. |
| NF-S3 | Database credentials must not be committed to source control (`application.yaml` is gitignored). |
| NF-S4 | `DataSourceWebhook.secretKey` must never be returned in any API response. |

### 5.6 Developer Experience

| ID | Requirement |
|----|-------------|
| NF-DX1 | CORS must allow requests from `http://localhost:3000` and `http://localhost:5173` for local development. |
| NF-DX2 | All public service interface methods must have JavaDoc. |
| NF-DX3 | All request DTOs must have `@Schema` annotations with `description` and `example` values. |
| NF-DX4 | Controllers must have `@Tag`, `@Operation`, `@ApiResponse`, and `@Parameter` annotations. |

---

## 6. Data Model

```
data_source (id PK, name UNIQUE, description, destination_table UNIQUE, archived, created_at, updated_at)
    |
    ├─── data_source_schema_field (id PK, data_source_id FK, name, data_type, description,
    |         created_at, updated_at)
    |    [Canonical destination-column definitions; DDL source for the destination table]
    |
    ├─── data_source_file (id PK, data_source_id FK, file_path UNIQUE, description,
    |         field_separator, quote_character, line_separator, skip_first_n_lines,
    |         created_at, updated_at)
    |         |
    |         ├─── data_source_field (id PK, data_source_file_id FK, field_name, field_alias,
    |         |         column_number, data_type, description)
    |         |    [UNIQUE: (data_source_file_id, field_alias)]
    |         |    [Each file has its own independent field schema]
    |         |
    |         └─── file_processing_log (id PK, data_source_file_id FK, file_path, file_name,
    |                  file_size_bytes, last_modified_millis, status, rows_ingested, rows_skipped,
    |                  errors, instance_id, started_at, completed_at)
    |              [UNIQUE: (data_source_file_id, file_path, last_modified_millis, file_size_bytes)]
    |
    └─── data_source_webhook (id PK, data_source_id FK UNIQUE, description, enabled,
              created_at, updated_at)
              |    [Exactly ONE webhook per data source (@OneToOne); path auto-generated from source name]
              |
              └─── data_source_webhook_property (id PK, webhook_id FK, name, field_alias,
                       data_type, description)
                   [Auto-synced from data_source_schema_field on every schema change]

rule (id PK, name, description, rule_expression TEXT, status, priority, active_from, active_to,
      frequency, last_run_at, created_at, updated_at)
    |
    └─── rule_action (id PK, rule_id FK, reward_type, reward_amount, description)
```

---

## 7. Technology Constraints

| Constraint | Value |
|---|---|
| Minimum Java version | 25 |
| Spring Boot version | 4.0.5 |
| Database | PostgreSQL (any recent version) |
| Message broker | Apache Kafka |
| API documentation | SpringDoc OpenAPI 2.8.5 |
| Build tool | Maven (wrapper bundled) |

---

## 8. Out-of-Scope / Future Work

- **`SCHEDULED` trigger type + `DataSourceScheduler`** — cron-based file polling; `pullFrequency`
  field on the entity
- **`RuleEvaluationService`** — parsing and evaluating the DSL expression language; subscribes to
  `DataPulledEvent`
- **HMAC signature verification** — `X-Hub-Signature-256` validation on webhook ingestion requests
- **Kafka integration** — producing reward events to Kafka topics; consuming ingestion acknowledgements
- **Authentication & authorisation** — JWT or OAuth2 on admin endpoints
- **Customer-facing API** — querying a customer's accumulated rewards
- **Audit trail** — who changed what and when (beyond `createdAt`/`updatedAt`)
- **Multi-tenancy** — isolating loyalty programs per client

