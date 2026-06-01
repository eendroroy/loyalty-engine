# Loyalty Management System

> **Note:** This is an experimental, work-in-progress project. The entire codebase is AI-generated — produced by GitHub Copilot agents from high-level conceptual requirements, with no manual coding involved. 

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue.svg)](https://www.typescriptlang.org/)

A comprehensive loyalty management platform with rule-based reward automation, voucher management, and multi-source data ingestion capabilities. Features an intuitive admin interface with visual rule builder, real-time validation, and comprehensive monitoring.

## 🚀 Key Features

- **🎯 Enhanced Visual Rule Builder** — Dual-mode editing (text + visual) with live validation and advanced grouping
- **🎟️ Dynamic Voucher Management** — Complete lifecycle with unique secret code generation and auto-awarding
- **📊 Multi-Source Data Ingestion** — File uploads + webhook endpoints with schema management and automatic metadata tracking
- **⚡ Real-Time Processing** — Event-driven rule evaluation and reward fulfillment
- **📈 Comprehensive Monitoring** — Live status tracking and processing history
- **🎨 Modern UI** — React 18 + TypeScript + MUI v6 with responsive design and accessibility

**Complete Project Recreation**: All documentation files collectively provide comprehensive instructions, patterns, and specifications needed to rebuild this entire system from scratch.

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 25 • Spring Boot 4 • Spring Data JPA • PostgreSQL |
| **Frontend** | React 18 • TypeScript • Vite • MUI v6 • Axios |
| **Build** | Maven • frontend-maven-plugin |
| **API Docs** | SpringDoc OpenAPI 2 |
| **Code Gen** | Lombok • MapStruct |

## 📋 Quick Start

### Prerequisites
- mise (optional, recommended for auto-installing pinned tool versions)
- Java 25+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.9+

### Setup & Run
```bash
# Clone the repository
git clone git@github.com:eendroroy/loyalty.git
cd loyalty

# Optional: install pinned runtime tools from .mise.toml
mise install

# Configure database
cp src/main/resources/application.example.yaml src/main/resources/application.yaml
# Edit application.yaml with your database credentials

# Build and run
mvn clean package -DskipTests
mvn spring-boot:run
```

Application will be available at:
- **Admin UI**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## 📖 Documentation

- **[documentation/BRD.md](documentation/BRD.md)** — Business Requirements Document for stakeholders and business users
- **[documentation/REQUIREMENTS.md](documentation/REQUIREMENTS.md)** — Complete technical specifications and system architecture
- **[.github/copilot-instructions.md](.github/copilot-instructions.md)** — Development guidelines and implementation patterns
- **[documentation/diagrams/](documentation/diagrams/)** — Technical diagrams including system architecture, database schema, and deployment flows

**Project Recreation**: These documentation files contain ALL information needed to recreate the entire project from scratch, including detailed business requirements, technical specifications, implementation patterns, and architectural decisions.

## 🎯 Rule Language

Create powerful loyalty rules with natural syntax:

```sql
-- Simple point reward
WHEN transaction.amount > 50 THEN Point(100)

-- Complex conditions with logic
WHEN transaction.category = "grocery" AND transaction.date >= 2025-01-01 THEN Point(30)

-- Voucher rewards with grouping
WHEN (transaction.amount > 100 OR customer.tier = "gold") THEN Voucher(SUMMER25)
```

**Supported Features:**
- **Operators**: `>`, `<`, `>=`, `<=`, `=`, `!=`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`
- **Logic**: `AND`, `OR`, parentheses for grouping
- **Data Types**: Numbers, dates (YYYY-MM-DD), strings, booleans
- **Rewards**: Points and Voucher codes

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   File Upload   │    │   Webhook API    │    │  Manual Entry   │
│     (CSV)       │    │    (JSON)        │    │    (Forms)      │
└─────────┬───────┘    └────────┬─────────┘    └─────────┬───────┘
          │                     │                        │
          └─────────────────────┼────────────────────────┘
                                │
                    ┌───────────▼────────────┐
                    │   Data Ingestion       │
                    │   Processing Engine    │
                    └───────────┬────────────┘
                                │
                    ┌───────────▼────────────┐
                    │   Rule Evaluation      │
                    │   Engine (AST)         │
                    └───────────┬────────────┘
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
┌─────────▼───────┐   ┌─────────▼────────┐   ┌────────▼───────┐
│ Point Rewards   │   │ Voucher Awards   │   │ Event Logging  │
│ (Accumulation)  │   │ (Secret Codes)   │   │ (Audit Trail)  │
└─────────────────┘   └──────────────────┘   └────────────────┘
```

## 📁 Project Structure

```
loyalty/
├── src/main/java/io/github/eendroroy/loyalty/
│   ├── config/          # Spring configuration
│   ├── controller/      # REST endpoints
│   ├── entity/          # JPA entities
│   ├── rule/            # Rule language engine
│   ├── service/         # Business logic
│   └── ...
├── frontend/src/
│   ├── api/             # API integrations
│   ├── components/      # React components
│   ├── pages/           # Route components
│   └── types/           # TypeScript definitions
├── documentation/       # Project documentation
│   ├── BRD.md           # Business requirements
│   ├── REQUIREMENTS.md  # Technical specifications
│   └── diagrams/        # Technical diagrams
└── README.md            # This file
```

## 🔧 Development

### Build Commands
```bash
mvn clean package -DskipTests          # Full build (backend + frontend)
mvn spring-boot:run                    # Run locally
mvn compile -DskipTests -Dfrontend.build.skip=true  # Backend only
mvn test                               # Run tests
```

### API Endpoints
- `GET /api/admin/rules` — List rules
- `POST /api/admin/rules/validate-expression` — Validate rule syntax
- `GET /api/admin/vouchers` — List vouchers  
- `POST /api/admin/vouchers/{id}/award` — Award voucher instance
- `GET /api/admin/data-sources` — List data sources
- `POST /web-hook/{dataSourceName}` — Webhook ingestion endpoint

## 🐛 Issues & Contributing

1. Check existing issues before creating new ones
2. Follow established code patterns (see [copilot-instructions.md](.github/copilot-instructions.md))
3. Update documentation when making changes
4. Ensure tests pass before submitting PRs

## 📄 License

This project is licensed under the GNU Affero General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Recent Updates

### ✅ **v0.5.0 — Enhanced Data Source File Management** (Latest)

**Archive Directory per File Source**:
- Each `DataSourceFile` can have an optional `archiveDirectory` path
- After zero-error ingestion the source file is atomically moved there with a timestamp suffix (e.g. `feed_20260601T143022.csv`)
- Move failure is logged as a warning — ingestion result is unaffected

**Per-Field Date Format for DATE Columns**:
- Each `DataSourceField` with `dataType: DATE` can specify a `dateFormat` using standard Java `DateTimeFormatter` patterns (e.g. `dd/MM/yyyy`, `yyyyMMdd`)
- Omitting the format or leaving it blank defaults to ISO-8601 (`yyyy-MM-dd`)
- The format is shown in the field mapping DataGrid and is editable in edit mode, read-only in view mode

**Frontend readOnly Mode Fixes (DataSourceForm)**:
- File and field dialog inputs are properly disabled in view mode
- Eye (view) icon shown in view mode instead of pencil (edit) icon
- "Add Field" button hidden in view mode; "Close" button shown instead of Cancel+Save
- Date Format column visible in field mapping grid for DATE fields

### ✅ **v0.4.0 — Enhanced Visual Rule Builder & Production Ready Platform**

**Enhanced Visual Rule Builder**:
- Dual-mode interface with seamless text/visual switching
- Structured WHEN/THEN sections with clear visual hierarchy  
- Three-column condition builder: Property → Operator → Value layout
- Advanced grouping with expandable cards and parentheses support
- Live voucher integration with availability tracking
- Context-aware UI with field type-based operator filtering
- Real-time expression preview with copy functionality
- Mobile responsive design with full accessibility support

**Complete Rule Language Engine**:
- Natural `WHEN ... THEN ...` syntax with full parser and evaluator
- Live expression validation with syntax guide in admin UI
- Type-aware evaluation supporting strings, numbers, dates, booleans
- Complex logic with AND/OR operators and parentheses grouping
- Automatic reward fulfillment for points and vouchers
- Event-driven evaluation on data ingestion

**Dynamic Voucher Management System**:
- Complete voucher lifecycle (create, edit, archive, purge)
- Unique 7-character alphanumeric secret code generation
- Capacity management with instance tracking
- Auto-awarding through rule triggers with conflict prevention
- Copy-to-clipboard functionality for secret codes
- Archive/active separation with comprehensive status tracking

**Enhanced Dashboard & UI System**:
- Dual-mode design system (light/dark) with glassmorphism effects
- Modern dashboard with 4 stat cards, trend indicators, and activity charts
- Recharts integration with crimson gradient fills matching design aesthetic
- Enhanced sidebar with gradient logo, version badge, and section labels
- AppBar with glassmorphism blur, notification badges, and gradient avatars
- Comprehensive MUI v6 theme system with custom component overrides

### ✅ **v0.1.0 — Enhanced Data Source Management**

**Frontend Enhancements**:
- Multi-step wizard interface (Details → Ingestion → Review)
- Real-time validation with immediate feedback
- Tabular field management using DataGrid components
- Auto-generated webhook paths and sample request bodies
- Enhanced error handling with comprehensive type safety

**Backend Improvements**:
- Schema-first architecture with auto-synced webhook properties
- Simplified webhook model (one per data source)
- Enhanced field management with atomic transactions
- Archive/purge lifecycle with conflict detection
- Comprehensive event system for reactive processing

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25 · Spring Boot 4 · Spring Data JPA · Spring Kafka |
| Database | PostgreSQL — schema via Hibernate `ddl-auto: update` |
| Messaging | Apache Kafka (dependency present; producers/consumers future) |
| ORM | Hibernate / Spring Data JPA + raw `JdbcTemplate` for destination tables |
| Mapping | MapStruct 1.6.3 (`componentModel = "spring"`) |
| Code Gen | Lombok |
| Rule Engine | Custom recursive descent parser with AST evaluation |
| API Docs | SpringDoc OpenAPI 2 (`/swagger-ui.html`, `/v3/api-docs`) |
| Frontend | React 18 · TypeScript · Vite · MUI v6 · MUI X DataGrid · Axios · Day.js |
| Build | Maven Wrapper (`./mvnw`) · frontend-maven-plugin |

Root package: `io.github.eendroroy.loyalty`

---

## Key Features

### 🎯 **Rule Language Engine**
- **Natural Syntax**: `WHEN transaction.amount > 50 THEN Point(100)`
- **Complex Logic**: AND/OR operators with parentheses grouping
- **Type Safety**: Automatic type coercion with runtime validation
- **Live Validation**: Real-time syntax checking in admin UI
- **Smart Editor**: Field insertion with `source.field` auto-formatting

### 🎫 **Voucher Management**
- **Complete Lifecycle**: Create, edit, archive, purge with capacity tracking
- **Unique Codes**: 7-character alphanumeric secret codes (A-Z, 0-9)
- **Auto-Awarding**: Rule-triggered voucher instance generation
- **Capacity Control**: Instance limits with overflow prevention
- **Copy Integration**: One-click secret code copying in admin UI

### 📊 **Data Source Management**
- **Multi-Step Wizard**: Guided configuration with validation at each step
- **Schema-First**: Reusable field definitions across files and webhooks
- **Auto-Generated Webhooks**: Paths, properties, and sample requests
- **Real-Time Validation**: Immediate feedback with comprehensive error handling
- **Archive Directory**: Optional per-file path — processed files moved with timestamp suffix after zero-error ingestion
- **Per-Field Date Format**: Configurable Java `DateTimeFormatter` pattern for DATE fields (e.g. `dd/MM/yyyy`); defaults to ISO-8601

### 📈 **Monitoring & Analytics**
- **Live Status**: File watchers, webhook endpoints, processing history
- **Rule Execution**: Trigger events, reward fulfillment, audit trails
- **Performance Tracking**: Processing times, success/failure rates
- **Archive Management**: Soft-delete with purge options

---

## Rule Language

### Syntax Examples

```sql
-- Simple point rewards
WHEN transaction.amount > 50 THEN Point(100)

-- Category-based with date conditions
WHEN transaction.category = "grocery" AND transaction.date >= 2025-01-01 THEN Point(30)

-- Complex logic with vouchers
WHEN (transaction.amount > 100 OR customer.tier = "gold") AND transaction.active = true 
     THEN Voucher(SUMMER25)

-- String matching
WHEN customer.email ENDS_WITH "@company.com" THEN Point(50)

-- Boolean conditions
WHEN transaction.verified = true AND transaction.amount > 20 THEN Point(25)
```

### Supported Features

**Operators**: `>`, `<`, `>=`, `<=`, `=`, `!=`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`
**Logic**: `AND`, `OR`, parentheses for grouping
**Types**: Numbers, dates (YYYY-MM-DD), strings, booleans
**Rewards**: `Point(amount)`, `Voucher(code)` with auto-instance generation

See [RULE_LANGUAGE.md](./RULE_LANGUAGE.md) for complete documentation.

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
| `dateFormat` | Java `DateTimeFormatter` pattern (e.g. `dd/MM/yyyy`, `yyyyMMdd`); only for `DATE` fields. Omit or leave blank to use ISO-8601 (`yyyy-MM-dd`) |
| `description` | Optional hint shown in rule-expression editor autocomplete |

**File ingestion file-level options** (on `DataSourceFile`):

| Property | Purpose |
|---|---|
| `filePath` | Absolute path to the watched file or directory |
| `fieldSeparator` | CSV column delimiter (default: `,`) |
| `quoteCharacter` | CSV quote character (default: `"`) |
| `lineSeparator` | Row delimiter (default: auto-detect) |
| `skipFirstNLines` | Number of preamble lines to skip before the header row |
| `archiveDirectory` | Optional absolute path. After zero-error ingestion the source file is moved here and renamed with a timestamp suffix (e.g. `feed_20260601T143022.csv`). Move failures are logged as warnings but do not fail the ingestion. |

Because fields live on individual files, two files inside the same data source may map completely
different columns to different aliases, enabling heterogeneous file formats in one logical source.

### CSV Ingestion

`DataPullServiceImpl` is fully implemented for CSV files:
- **Distributed deduplication** via a unique constraint on
  `(data_source_file_id, file_path, last_modified_millis, file_size_bytes)` in `FileProcessingLog` —
  only one application instance processes each file version; competing instances silently skip.
- Loads each `DataSourceFile` with its **own** field definitions — columns are resolved by
  `columnNumber` (1-based) or by `fieldName` header match (case-insensitive) **for that specific file**.
- Values type-coerced per `dataType`; DATE fields use `dateFormat` when specified, ISO-8601 otherwise.
- After zero-error ingestion, file is atomically moved to `archiveDirectory` (if configured) with a timestamp suffix.
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
      "description": "Format A — 3-column daily export (dd/MM/yyyy dates)",
      "archiveDirectory": "/data/archive",
      "fields": [
        { "fieldName": "Date",   "fieldAlias": "transaction.date",   "columnNumber": 1, "dataType": "DATE",    "dateFormat": "dd/MM/yyyy" },
        { "fieldName": "Amount", "fieldAlias": "transaction.amount", "columnNumber": 2, "dataType": "DECIMAL" },
        { "fieldName": "Tier",   "fieldAlias": "customer.tier",      "columnNumber": 3, "dataType": "STRING" }
      ]
    },
    {
      "filePath": "/data/transactions_b.csv",
      "description": "Format B — different column order (ISO-8601 dates)",
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
