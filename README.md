# Loyalty Management System

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

> **Repository:** `git@github.com:eendroroy/loyalty.git`

A comprehensive backend system for managing loyalty programs with **multi-source data ingestion**,
**dynamic rule evaluation**, and **flexible reward distribution**. Features an intuitive admin
interface with enhanced data source management, webhook configuration, and real-time monitoring.

---

## Table of Contents

- [Overview](#overview)
- [Recent Updates](#recent-updates)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Package Structure](#package-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Build and Run](#build-and-run)
- [Admin Interface](#admin-interface)
- [Swagger UI](#swagger-ui)
- [API Reference](#api-reference)
- [Rule Expression Syntax](#rule-expression-syntax)
- [Data Source Field Aliases](#data-source-field-aliases)
- [File Watcher and Ingestion](#file-watcher-and-ingestion)
- [Monitoring](#monitoring)
- [Actuator Endpoints](#actuator-endpoints)
- [License](#license)

---

## Overview

The system provides an admin API (backing a React admin panel) for:

1. **Enhanced Data Source Management** — Comprehensive multi-step form for configuring data sources
   with schema fields, file paths, and webhook endpoints. Features real-time validation, auto-generated
   webhook paths, and sample request body generation.
   
2. **Intelligent Webhook Configuration** — Simplified webhook model with auto-synced properties from
   schema fields, eliminating redundant field definitions while providing rich configuration options.
   
3. **Dynamic Rule Engine** — Rules written in custom `WHEN ... THEN ...` expression language with
   lifecycle management (`DRAFT / ACTIVE / INACTIVE`) and cron-based evaluation.
   
4. **Flexible Reward Distribution** — Multi-type reward actions (`POINT` or `VOUCHER`) with
   comprehensive tracking and monitoring.

---

## Recent Updates

### 🎉 Enhanced Data Source Management (v0.1.0)

- **Multi-Step Form Wizard**: Intuitive 3-step process (Details → Ingestion → Review)
- **Schema-First Architecture**: Define destination table columns with auto-sync to webhooks
- **Advanced File Management**: CSV parsing options, field mappings, and tabular interfaces
- **Webhook Enhancements**: 
  - Auto-generated webhook paths (`POST /web-hook?dataSourceName={name}`)
  - Real-time sample request body generation
  - Enable/disable toggle with description support
- **Archive Management**: Soft-delete with purge options for data and table cleanup
- **Type Safety**: Full TypeScript integration with comprehensive error handling
- **Real-Time Validation**: Form validation with immediate feedback

### 🔧 Technical Improvements

- **Simplified Webhook Model**: Properties auto-sync from schema fields (no duplicate definitions)
- **Enhanced Type Safety**: Strict TypeScript types with proper error handling
- **Clean Architecture**: Separated UI state management from entity operations
- **Improved UX**: Visual status indicators, real-time field validation, comprehensive field management

---

## Tech Stack

| Layer           | Technology                                                      |
|-----------------|-----------------------------------------------------------------|
| Language        | Java 25                                                         |
| Framework       | Spring Boot 4.0.5 (Web MVC, Data JPA, Kafka, Validation)        |
| Database        | PostgreSQL (schema managed via Hibernate `ddl-auto: update`)    |
| Messaging       | Apache Kafka                                                    |
| ORM             | Hibernate / Spring Data JPA                                     |
| Code generation | Lombok, MapStruct 1.6.3                                         |
| API Docs        | SpringDoc OpenAPI 2.8.5 (Swagger UI at `/swagger-ui.html`)      |
| Build           | Maven Wrapper (`./mvnw`)                                        |
| Auditing        | Spring Data JPA (`@CreatedDate`, `@LastModifiedDate`)           |

---

## Architecture

### Data Sources

A `DataSource` is a logical ingestion source with enhanced management capabilities. It owns:

| Child entity | Relationship | Purpose |
|---|---|---|
| `DataSourceSchemaField` | one-to-many | Canonical destination table column definitions |
| `DataSourceFile` | one-to-many | Watched file paths with individual field mappings |
| `DataSourceWebhook` | one-to-one | Push ingestion endpoint with auto-synced properties |

#### Enhanced Features

**Schema-First Design**: Schema fields define the destination table structure and serve as the
canonical field definitions. File field mappings and webhook properties automatically reference
these schema fields, eliminating duplication and ensuring consistency.

**Simplified Webhook Model**: Each data source has exactly one webhook configuration. Webhook
properties are automatically synchronized with the schema fields, reducing configuration complexity
while maintaining flexibility.

**Multi-Step Management**: The admin interface provides a guided workflow for configuring:
1. Basic source details and schema fields
2. File paths and webhook configuration  
3. Review and validation before saving

Each `DataSourceFile` owns its own typed field/column mappings (`DataSourceField`), allowing
different files under the same data source to have **different column layouts** while mapping
to the same destination schema. `DataSource.getAllFields()` aggregates all file fields for
metadata and destination table operations.

**File ingestion trigger types** (on `DataSourceFile`):

| `TriggerType` | Behaviour |
|---|---|
| `FILE_WATCHER` | `FileWatcherService` triggers an immediate pull on `ENTRY_CREATE`/`ENTRY_MODIFY` NIO events |
| `INSTANT` | External service pushes data via HTTP to the webhook endpoint; no polling |

**Field schema** (defined per `DataSourceFile`, not per `DataSource`):

| Property | Purpose |
|---|---|
| `fieldName` | Column header or raw field name in the source |
| `fieldAlias` | Globally-unique identifier used in rule expressions, e.g. `transaction.amount` |
| `columnNumber` | 1-based column index (CSV FILE sources only; optional — falls back to header name match) |
| `dataType` | `STRING`, `INTEGER`, `DECIMAL`, `DATE`, `BOOLEAN` |

Because fields live on individual files, two files inside the same data source may map completely
different columns to different aliases, enabling heterogeneous file formats in one logical source.

### CSV Ingestion

`DataPullServiceImpl` is fully implemented for CSV files:
- **Distributed deduplication** via a unique constraint on
  `(data_source_file_id, file_path, last_modified_millis, file_size_bytes)` in `FileProcessingLog` —
  only one application instance processes each file version; competing instances silently skip.
- Loads each `DataSourceFile` with its **own** field definitions — columns are resolved by
  `columnNumber` (1-based) or by `fieldName` header match (case-insensitive) **for that specific file**.
- Values type-coerced per `dataType`; ingestion summary returned as `DataPullResult`.
- On success, publishes `DataPulledEvent` for downstream rule evaluation.

### Webhook Ingestion

`WebhookIngestionServiceImpl` handles inbound HTTP push events:
- External services `POST` a JSON payload to `POST /web-hook?dataSourceName={name}`.
- The webhook's `DataSourceWebhookProperty` definitions describe the expected payload schema
  (JSON key → typed alias → destination-table column).
- Each property value is type-coerced per `dataType`; `format` is applied for DATE fields.
- If the data source has a `destinationTable`, the row is inserted and a `DataPulledEvent` is published.
- Response: `DataPullResult` with `rowsIngested`, `rowsSkipped`, `errors`.

**Webhook property schema** (per `DataSourceWebhook`):

| Property | Purpose |
|---|---|
| `name` | JSON key in the incoming payload (e.g. `"transactionAmount"`) |
| `fieldAlias` | Globally-unique alias used in rule expressions, e.g. `transaction.amount` |
| `dataType` | `STRING`, `INTEGER`, `DECIMAL`, `DATE`, `BOOLEAN` |
| `format` | Date-format pattern (e.g. `yyyy-MM-dd`); blank = ISO-8601 |
| `description` | Hint shown in rule-expression editor autocomplete |

**`contentType`** on `DataSourceWebhook` controls how the request body is parsed:

| `contentType` | Behaviour |
|---|---|
| `APPLICATION_JSON` | Default; request body parsed as JSON object |

### Rule Engine

Rules use a `DRAFT → ACTIVE → INACTIVE` lifecycle, a numeric priority, an optional active date
window, and one or more `RuleAction` reward records. `RuleScheduler` fires active rules on their
cron `frequency`; rule expression evaluation is stubbed pending `RuleEvaluationService`.

### Event Bus

Services publish domain events after DB transaction commits.
Schedulers and the file watcher listen via `@TransactionalEventListener(AFTER_COMMIT)`.

| Event | Listener |
|---|---|
| `DataSourceFileSavedEvent` | `FileWatcherService` — register/re-register directory watch |
| `DataSourceFileDeletedEvent` | `FileWatcherService` — deregister directory watch |
| `RuleSavedEvent` | `RuleScheduler` — (re)schedule or cancel cron task |
| `RuleDeletedEvent` | `RuleScheduler` — cancel cron task |
| `DataPulledEvent` | future `RuleEvaluationService` |

---

## Package Structure

```
src/main/java/io/github/eendroroy/loyalty/
|- config/          CorsConfig, JacksonConfig, OpenApiConfig, SchedulerConfig, SpaController
|- controller/      DataSourceController, MetadataController, MonitorController, RuleController,
|                   WebhookIngestionController
|- dto/request/     @Schema-annotated inbound DTOs (incl. DataSourceWebhookPropertyRequest)
|- dto/response/    @JsonInclude NON_NULL outbound DTOs (incl. DataSourceWebhookPropertyResponse)
|- entity/          JPA entities; enums: FieldDataType, RewardType, RuleStatus,
|                   TriggerType, WebhookContentType
|- event/           Spring event records
|- mapper/          MapStruct interfaces (incl. DataSourceWebhookPropertyMapper)
|- model/           DataPullResult, IngestedRecord (non-JPA value objects)
|- repository/      JpaRepository + custom JPQL (incl. DataSourceWebhookPropertyRepository)
|- scheduler/       RuleScheduler
|- service/         Interfaces (JavaDoc); incl. WebhookIngestionService,
|                   DataSourceWebhookPropertyService
|- service/impl/    @Service implementations
|- watcher/         FileWatcherService (NIO WatchService)

src/main/resources/
|- application.yaml
|- application.example.yaml
|- logback-spring.xml
```

---

## Prerequisites

- **Java 25+**
- **PostgreSQL** running and accessible
- **Apache Kafka** broker running

---

## Configuration

`application.yaml` is **gitignored**. Copy the example and fill in credentials:

```bash
cp src/main/resources/application.example.yaml src/main/resources/application.yaml
```

| Placeholder | Description |
|---|---|
| `<HOST>` | PostgreSQL host |
| `<DATABASE>` | Database name |
| `<USERNAME>` | Database user |
| `<PASSWORD>` | Database password |
| `<KAFKA_HOST>` | Kafka broker host |

---

## Admin Interface

### Data Source Management

The enhanced admin interface provides comprehensive data source management through a multi-step wizard:

#### 📋 Step 1: Source Details
- **Name**: Unique system-wide identifier
- **Description**: Optional documentation
- **Destination Table**: Target PostgreSQL table name
- **Schema Fields**: Define destination columns with name, data type, and descriptions

#### 📁 Step 2: Ingestion Configuration

**File Sources**:
- **Directory Paths**: Absolute paths to watched files/directories
- **CSV Parsing**: Custom separators, quote characters, line handling
- **Field Mappings**: Map source columns to schema fields with column numbers
- **Real-time Validation**: Immediate feedback on configuration

**Webhook Sources**:
- **Auto-generated Paths**: `/weeb-hook/{dataSourceName}`
- **Enable/Disable Toggle**: Control webhook activation
- **Description Support**: Document webhook purpose and usage
- **Sample Request Body**: Auto-generated JSON based on schema fields
- **Property Auto-sync**: Webhook properties automatically mirror schema fields

#### ✅ Step 3: Review & Save
- **Configuration Summary**: Review all settings before saving
- **Validation Status**: Comprehensive pre-save validation
- **Atomic Operations**: All changes saved in single transaction

### Key Features

- **Tabular Field Management**: DataGrid interfaces for managing complex field mappings
- **Real-time Path Generation**: Webhook paths update automatically with source name
- **Archive Management**: Soft-delete sources with options to purge data or drop tables
- **Type Safety**: Full TypeScript integration with comprehensive error handling
- **Visual Status Indicators**: Clear indication of configuration state and validation status

---

## Build and Run

```bash
mvn clean package -DskipTests   # build
mvn spring-boot:run             # run
mvn test                        # test
```

---

## Swagger UI

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.1 JSON spec |
| `http://localhost:8080/v3/api-docs.yaml` | OpenAPI 3.1 YAML spec |

Tags: **Data Sources**, **Rules**, **Metadata**, **Monitor**

---

## API Reference

All endpoints: `/api/admin/…`

### Data Sources

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/data-sources` | List all (includes files, webhook, fields) |
| `POST` | `/data-sources` | Create + optional inline files, webhook, fields |
| `GET` | `/data-sources/{id}` | Get by ID |
| `PUT` | `/data-sources/{id}` | Update + optional replacement of files/webhook/fields |
| `DELETE` | `/data-sources/{id}` | Delete (cascades to all children) |

### Data Source Files

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/data-sources/{id}/files` | List watched file paths (each includes its field schema) |
| `POST` | `/data-sources/{id}/files` | Add a file path (with optional inline `fields` array) |
| `PUT` | `/data-sources/{id}/files/{fileId}` | Update a file path; non-null `fields` replaces schema |
| `DELETE` | `/data-sources/{id}/files/{fileId}` | Remove a file path and all its field definitions |

### Data Source Fields _(nested under files)_

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/data-sources/{id}/files/{fileId}/fields` | List field definitions for a specific file |
| `POST` | `/data-sources/{id}/files/{fileId}/fields` | Add a field to a specific file |
| `PUT` | `/data-sources/{id}/files/{fileId}/fields/{fieldId}` | Update a field |
| `DELETE` | `/data-sources/{id}/files/{fileId}/fields/{fieldId}` | Remove a field |

### Data Source Webhooks

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/data-sources/{id}/webhooks` | List webhook configs (incl. properties) |
| `POST` | `/data-sources/{id}/webhooks` | Add a webhook config (with optional inline `properties`) |
| `PUT` | `/data-sources/{id}/webhooks/{webhookId}` | Update; non-null `properties` replaces schema |
| `DELETE` | `/data-sources/{id}/webhooks/{webhookId}` | Remove a webhook config and its properties |

### Webhook Properties _(nested under webhooks)_

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/data-sources/{id}/webhooks/{webhookId}/properties` | List property definitions |
| `POST` | `/data-sources/{id}/webhooks/{webhookId}/properties` | Add a property definition |
| `PUT` | `/data-sources/{id}/webhooks/{webhookId}/properties/{propertyId}` | Update a property |
| `DELETE` | `/data-sources/{id}/webhooks/{webhookId}/properties/{propertyId}` | Remove a property |

### Webhook Ingestion _(public endpoint, NOT under `/api/admin/`)_

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/hooks/data/{webhookId}` | Push a JSON payload; coerced and written to destination table |

### Rules

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/rules` | List all (includes actions) |
| `POST` | `/rules` | Create |
| `GET` | `/rules/{id}` | Get by ID |
| `PUT` | `/rules/{id}` | Update |
| `DELETE` | `/rules/{id}` | Delete (cascades to actions) |
| `GET` | `/rules/{id}/actions` | List actions |
| `POST` | `/rules/{id}/actions` | Add an action |
| `PUT` | `/rules/{id}/actions/{actionId}` | Update an action |
| `DELETE` | `/rules/{id}/actions/{actionId}` | Remove an action |

### Metadata

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/metadata` | Field-alias registry for rule autocomplete |

### Monitor

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/monitors/file-watchers` | Live file-watcher status per file path |
| `GET` | `/monitors/webhooks` | All configured webhook endpoints |
| `GET` | `/monitors/logs` | Paginated file processing history (newest-first) |

#### Example: Create a data source with files (each with their own field schema) and webhooks

```json
POST /api/admin/data-sources
{
  "name": "Transaction Feed",
  "description": "Daily CSV exports (different formats) plus real-time push events",
  "destinationTable": "transaction_events",
  "files": [
    {
      "filePath": "/data/transactions_a.csv",
      "description": "Format A — 3-column daily export",
      "fields": [
        { "fieldName": "Date",   "fieldAlias": "transaction.date",   "columnNumber": 1, "dataType": "DATE" },
        { "fieldName": "Amount", "fieldAlias": "transaction.amount", "columnNumber": 2, "dataType": "DECIMAL" },
        { "fieldName": "Tier",   "fieldAlias": "customer.tier",      "columnNumber": 3, "dataType": "STRING" }
      ]
    },
    {
      "filePath": "/data/transactions_b.csv",
      "description": "Format B — different column order",
      "fields": [
        { "fieldName": "Tier",   "fieldAlias": "customer.tier_b",    "columnNumber": 1, "dataType": "STRING" },
        { "fieldName": "Amount", "fieldAlias": "transaction.amount_b","columnNumber": 2, "dataType": "DECIMAL" }
      ]
    }
  ],
  "webhooks": [
    { "endpoint": "/webhooks/payments", "description": "Payment gateway push events" }
  ]
}
```


---

## Rule Expression Syntax

```
WHEN <condition> [AND|OR <condition>]... THEN <amount> <RewardType>
```

**Operators:** `>=`, `<=`, `>`, `<`, `=`, `!=`  
**Built-in:** `DATE` (current date `yyyy-MM-dd`)  
**Field refs:** use `fieldAlias` values (e.g. `transaction.amount`)  
**RewardType:** `POINT` | `VOUCHER`

```
WHEN transaction.amount > 50 THEN 100 POINT
WHEN DATE >= 2025-12-01 AND DATE <= 2025-12-31 AND transaction.amount > 20 THEN 50 POINT
WHEN customer.tier = GOLD AND transaction.amount > 100 THEN VOUCHER
```

> Rule expression **evaluation is not yet implemented** — `RuleScheduler` calls a TODO stub.

---

## Data Source Field Aliases

`fieldAlias` is globally unique. Pattern: `^[a-zA-Z][a-zA-Z0-9._]*$`

Field definitions live on individual `DataSourceFile` records, not on the parent `DataSource`.
This means two files under the same source can have completely different schemas:

```
DataSource: "Transaction Feed"
  DataSourceFile: /data/transactions_a.csv   ← Format A (4 columns)
    |- col=1  alias="transaction.date"    type=DATE
    |- col=2  alias="transaction.id"      type=STRING
    |- col=3  alias="transaction.amount"  type=DECIMAL
    |- col=4  alias="customer.tier"       type=STRING

  DataSourceFile: /data/transactions_b.csv   ← Format B (different order, 2 columns)
    |- col=1  alias="customer.tier_b"     type=STRING
    |- col=2  alias="transaction.amount_b" type=DECIMAL
```

---

## File Watcher and Ingestion

- **FileWatcherService** — NIO `WatchService` on every registered `DataSourceFile` directory.
  Registers on `ApplicationReadyEvent`; hot-reloads on `DataSourceFileSavedEvent` /
  `DataSourceFileDeletedEvent` (fired after transaction commit).
- **DataPullServiceImpl** — CSV parser with distributed deduplication via `FileProcessingLog`.
  Returns `DataPullResult(rowsIngested, rowsSkipped, errors)` and publishes `DataPulledEvent`.

---

## Monitoring

`GET /api/admin/monitors/file-watchers` — live status per `DataSourceFile`:
`fileId`, `dataSourceId`, `sourceName`, `filePath`, `watching` (boolean),
`lastLog` (most recent `FileProcessingLog` summary), `totalCompleted`, `totalFailed`.

`GET /api/admin/monitors/webhooks` — all configured webhook endpoints.

`GET /api/admin/monitors/logs?page=0&size=50` — paginated `FileProcessingLog` history.

---

## Actuator Endpoints

`/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/loggers`,
`/actuator/scheduledtasks`, `/actuator/beans`, `/actuator/env`

> Restrict to `health,info` in production and secure with Spring Security.

---

## License

Copyright (C) 2025-2026 [eendroroy](https://github.com/eendroroy)  
**GNU Affero General Public License v3.0** — see [LICENSE](LICENSE).
