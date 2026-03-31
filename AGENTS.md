# AGENTS.md — Loyalty Management System

Spring Boot 4 / Java 25 backend for loyalty program management. PostgreSQL + Kafka required.

---

## Essential Commands

```bash
mvn clean package -DskipTests   # build
mvn spring-boot:run             # run locally
mvn test                        # run tests
mvn compile -DskipTests -Dfrontend.build.skip=true  # compile only
```

`application.yaml` is gitignored. On first checkout:
```bash
cp src/main/resources/application.example.yaml src/main/resources/application.yaml
# fill in <HOST>, <DATABASE>, <USERNAME>, <PASSWORD>, <KAFKA_HOST>
```

API docs: `http://localhost:8080/swagger-ui.html` · spec: `http://localhost:8080/v3/api-docs`

---

## Architecture

**Six controllers** under `/api/admin/` plus one public ingestion endpoint:
- `DataSourceController` — CRUD for `DataSource` + nested `DataSourceSchemaField`, `DataSourceFile` (with per-file `DataSourceField`), and the single `DataSourceWebhook`; archive/purge lifecycle. `DELETE /{id}` returns **405 Method Not Allowed** — use `/archive` + `/purge` instead.
- `DataSourceTableController` — paginated, searchable query of a data source's destination table (`GET /data-sources/{id}/table/data`)
- `RuleController` — CRUD for `Rule` + nested `RuleAction`
- `MetadataController` — read-only field-alias registry for rule-expression autocomplete
- `MonitorController` — read-only live status of file watchers, webhooks, and processing history
- `WebhookIngestionController` — **public** `POST /web-hook?dataSourceName={name}`; receives external JSON payloads and ingests them

**Data flow:**
`DataSource` → `DataSourceSchemaField` (canonical destination-column definitions; auto-synced to webhook)
             → `DataSourceFile` (file paths, each watched by `FileWatcherService`)
                    └─ `DataSourceField` (typed aliases **per file**)
             → `DataSourceWebhook` (**exactly one** per source, @OneToOne)
                    └─ `DataSourceWebhookProperty` (auto-synced from schema fields;
                                                     JSON key → fieldAlias → destination column;
                                                     `contentType` defaults to `APPLICATION_JSON`)
             → `destinationTable` (PostgreSQL table managed by `DataSourceTableService` via JDBC)
             → `Rule.ruleExpression` (`WHEN transaction.amount > 20 THEN 30 POINT`)

`DataSource.getAllFields()` is a `@Transient` helper that flattens fields across all files.
`DataSource.archived` (boolean, default `false`) is a soft-delete flag; hard deletion is disabled —
use `archiveById()` + `purgeArchivedById()` instead.

**OSIV is disabled** (`spring.jpa.open-in-view: false`). Always use the `WithFields`/`WithActions`
JOIN FETCH repository queries before leaving a transaction — plain `findById` will cause
`LazyInitializationException` for collections.

---

## Mandatory Patterns

### Service layer
Every service has an interface in `service/` and an `@Service @RequiredArgsConstructor` impl in
`service/impl/`. Controllers inject the interface only.

Services:
- `DataSourceService` — `DataSource` aggregate (save/update/archive/purge, publishes events;
  `deleteById` throws `UnsupportedOperationException` — use archive/purge instead)
- `DataSourceFileService` — `DataSourceFile` CRUD; `updateWithFields()` atomically replaces
  scalar fields and field definitions in one transaction; publishes `DataSourceFileSavedEvent` /
  `DataSourceFileDeletedEvent`
- `DataSourceSchemaFieldService` — `DataSourceSchemaField` CRUD (canonical destination-column defs)
- `DataSourceWebhookService` — `DataSourceWebhook` CRUD (**one per source**, @OneToOne)
- `DataSourceWebhookPropertyService` — `DataSourceWebhookProperty` CRUD; properties are
  auto-synced from schema fields via `DataSourceServiceImpl.syncWebhookPropertiesToSchemaFields()`
  — direct CRUD is not exposed through controller endpoints
- `DataSourceFieldService` — `DataSourceField` CRUD (per-file typed field definitions)
- `DataSourceTableService` — JDBC-backed (`JdbcTemplate`) destination table management: DDL
  (`CREATE TABLE IF NOT EXISTS`), DML (`INSERT`, `DELETE`), paginated query with ILIKE search;
  **not JPA** — no mapper needed; SQL safety via allow-list + double-quoting + bind parameters;
  overloads accepting explicit `List<DataSourceField>` used by webhook ingestion
- `FileProcessingLogService` — `FileProcessingLog` lifecycle; both methods run in
  `Propagation.REQUIRES_NEW` to isolate constraint-violation rollbacks from the outer pull call
- `WebhookIngestionService` — coerces a JSON payload against the data source's schema fields,
  inserts into destination table, publishes `DataPulledEvent`
- `RuleService` / `RuleActionService` — rule aggregate
- `MetadataService` — read-only field-alias registry
- `MonitorService` — file-watcher status, webhook list, paginated processing log
- `DataPullService` — CSV ingestion with distributed deduplication; publishes `DataPulledEvent`

### Lombok on entities vs DTOs
- **Entities**: `@Getter @Setter` only. Never `@Data` — it breaks Hibernate lazy loading.
- **DTOs (request)**: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- **DTOs (response)**: `@Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(NON_NULL)`

### MapStruct mappers
All mapping goes through `mapper/` interfaces using `MapStructConfig` (`componentModel = "spring"`).
Every mapper must:
- Ignore `id`, `createdAt`, `updatedAt`, and relationship fields (`dataSource`, `rule`) in `toEntity`
- Apply `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` on `updateEntity` for PATCH semantics

Mappers: `DataSourceMapper`, `DataSourceFileMapper`, `DataSourceWebhookMapper`,
`DataSourceFieldMapper`, `RuleMapper`, `RuleActionMapper`

### Transactional rules
- Read methods: `@Transactional(readOnly = true)`
- Write methods: `@Transactional`

### Field replacement — JPQL bulk DELETE
Fields belong to individual `DataSourceFile` records. When replacing a file's fields, call
`DataSourceFieldRepository.deleteByDataSourceFileId(fileId)` (a `@Modifying` JPQL query with
`clearAutomatically = true`) **before** inserting new fields. Hibernate queues INSERTs before
DELETEs, causing unique-constraint violations on `field_alias` if orphan removal is used instead.
Same pattern applies to files via `DataSourceFileRepository.deleteByDataSourceId(id)`.

**Before deleting files**, always call `FileProcessingLogRepository.deleteByDataSourceFileIdIn(fileIds)`
first to avoid FK constraint violations on `file_processing_log`. The service collects file IDs
(`fileRepository.findByDataSourceId(id)`) before bulk-deleting fields and files.

### Spring events — never call schedulers directly
After save/delete, service impls publish events. Listeners use:
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

| Event | Publisher | Listener |
|---|---|---|
| `DataSourceSavedEvent` | `DataSourceServiceImpl` | — |
| `DataSourceFileSavedEvent(fileId, filePath)` | `DataSourceFileServiceImpl` | `FileWatcherService` |
| `DataSourceFileDeletedEvent(fileId)` | `DataSourceFileServiceImpl` | `FileWatcherService` |
| `RuleSavedEvent` | `RuleServiceImpl` | `RuleScheduler` |
| `RuleDeletedEvent` | `RuleServiceImpl` | `RuleScheduler` |
| `DataPulledEvent(List<IngestedRecord>)` | `DataPullServiceImpl` / `WebhookIngestionServiceImpl` | future `RuleEvaluationService` |

Do **not** call `RuleScheduler` or `FileWatcherService` directly from controllers or services.

### Trigger type rules
`TriggerType` is a backend enum on `DataSourceFile` (not on `DataSource`):

| `TriggerType` | Source | Behaviour |
|---|---|---|
| `FILE_WATCHER` | FILE | `FileWatcherService` triggers a pull on `ENTRY_CREATE`/`ENTRY_MODIFY` NIO events |
| `INSTANT` | WEBHOOK | External service pushes data; no polling (ingestion controller not yet implemented) |

> **`SCHEDULED` trigger type and `DataSourceScheduler` are not yet implemented.**
> `DataSource` does not yet have a `type`, `triggerType`, or `pullFrequency` field on the entity.

### DataPullService — CSV ingestion
`DataPullServiceImpl` is fully implemented for CSV files:
- Loads `DataSourceFile` with its parent `DataSource` and `DataSourceField` list in one query
- Inserts an `IN_PROGRESS` `FileProcessingLog` row (unique constraint on
  `(data_source_file_id, file_path, last_modified_millis, file_size_bytes)`) for distributed
  deduplication — competing instances receive a constraint violation and skip silently
- Parses CSV: fields with `columnNumber` resolved by 1-based index; others matched by `fieldName`
  against header row (case-insensitive)
- Type-coerces values to `Long`, `BigDecimal`, `LocalDate`, `Boolean`, or `String`
- If `DataSource.destinationTable` is set, calls `DataSourceTableService.createDestinationTableIfNotExists()`
  then `DataSourceTableService.insertRecords()` to persist parsed rows into the destination table
- Updates log to `COMPLETED` or `FAILED`; `FAILED` rows are deleted so the file retries next event
- Publishes `DataPulledEvent(List<IngestedRecord>)` after a successful parse

Returns `DataPullResult(rowsIngested, rowsSkipped, errors)`.

### Scheduler internals
`RuleScheduler` maintains a `ConcurrentHashMap<Long, ScheduledFuture<?>>`. To reschedule: call
`cancel(id)` then register a new `CronTrigger`. The shared `ThreadPoolTaskScheduler` bean
(pool size 10, prefix `loyalty-scheduler-`) is defined in `config/SchedulerConfig.java`.

---

## API Conventions

- `POST` → `201 Created`; `DELETE` → `204 No Content`; missing resource → `404`
- `PUT` uses `updateEntity` mapper: only non-null request fields overwrite entity state
- Each `DataSourceFileRequest` carries its own `fields` list; there is no top-level `fields` array
  on `DataSourceRequest`. Supplying `files` in a `PUT /data-sources/{id}` body **replaces all**
  existing files (and their fields); omitting `files` (null) leaves them untouched. Same for `webhooks`.
- Nested resources under data sources: `/files/{fileId}`, `/webhook` (singular — one webhook per source)
- Field resources are nested under files: `/files/{fileId}/fields/{fieldId}`
- Nested resources under rules: `/actions/{actionId}`
- `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` required on every controller and handler
- `@Schema(description, example)` required on every request DTO field

Additional data source endpoints:
- `GET /data-sources/with-tables` — lists sources that have a non-blank `destinationTable`
- `GET /data-sources/archived` — lists archived (soft-deleted) sources
- `POST /data-sources/{id}/archive` → `204` — soft-deletes a source (`archived = true`);
  publishes `DataSourceFileDeletedEvent` for every file so `FileWatcherService` deregisters them
- `DELETE /data-sources/{id}/purge` → `204` or `409` — hard-deletes an archived source;
  returns `409 Conflict` if `destinationTable` is still referenced by an active source
- `DELETE /data-sources/{id}/destination-data` → `204` — purges all rows from the destination table
  (keeps the table schema); irreversible
- `DELETE /data-sources/{id}/destination-table` → `204` — drops the destination table entirely and
  clears `destinationTable` on the source; irreversible
- `GET /data-sources/{id}/table/data` → `200` — paginated/searchable destination-table query;
  params: `search`, `page` (0-based), `size` (default 20, max 200), `sortBy`, `sortDir (asc|desc)`

---

## Schema & Config

- Hibernate `ddl-auto: update` — no migration tool; structural changes go directly in entities
- `fieldAlias` must be globally unique and match `^[a-zA-Z][a-zA-Z0-9._]*$`
- `DataSourceWebhook.endpoint` must be globally unique
- `DataSource.destinationTable` must match `^[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)?$`
  (supports schema-qualified names, e.g. `public.transaction_events`); validated in
  `DataSourceTableServiceImpl.validTableName()` before any SQL is issued
- `DataSource.archived` (boolean, default `false`) — soft-delete; `deleteById` throws
  `UnsupportedOperationException`; use `archiveById()` + `purgeArchivedById()` instead
- `DataSourceField.description` (TEXT, optional) — displayed as a tooltip/hint in the rule-expression
  autocomplete UI; surfaced in `TableDataResponse.ColumnMeta`
- `DataSourceFile` CSV parsing config (all optional; backend defaults apply when null/blank):
  - `fieldSeparator` (VARCHAR 10) — column delimiter, default `,`
  - `quoteCharacter` (VARCHAR 10) — quote char, default `"`
  - `lineSeparator` (VARCHAR 10) — informational; parser auto-detects `\n`/`\r\n`/`\r`
  - `skipFirstNLines` (INTEGER) — lines to discard before the CSV header row, default 0
- Enums stored as `EnumType.STRING`; never raw strings for type columns
- Max line length: 120 characters; `var` for obvious local types; no star imports

---

## Not Yet Implemented

- `SCHEDULED` trigger type + `DataSourceScheduler` (cron-based file polling)
- `RuleEvaluationService` — parses and evaluates `WHEN … THEN …` expressions; should subscribe to
  `DataPulledEvent` (`evaluate()` is a TODO stub in `RuleScheduler`)
- HMAC signature verification on `WebhookIngestionController` (`secretKey` is stored but not checked)
- Kafka producers/consumers (dependency present, no code yet)
- Authentication/authorization on admin endpoints

Key files:
`entity/`, `service/DataSourceService.java`, `service/impl/DataSourceServiceImpl.java`,
`service/DataSourceFileService.java`, `service/impl/DataSourceFileServiceImpl.java`,
`service/DataSourceTableService.java`, `service/impl/DataSourceTableServiceImpl.java`,
`service/DataPullService.java`, `service/impl/DataPullServiceImpl.java`,
`service/FileProcessingLogService.java`, `service/impl/FileProcessingLogServiceImpl.java`,
`service/MonitorService.java`, `service/impl/MonitorServiceImpl.java`,
`repository/DataSourceFieldRepository.java`, `repository/DataSourceFileRepository.java`,
`repository/FileProcessingLogRepository.java`,
`scheduler/RuleScheduler.java`, `watcher/FileWatcherService.java`,
`model/IngestedRecord.java`, `model/DataPullResult.java`, `event/DataPulledEvent.java`,
`dto/response/TableDataResponse.java`

---

## Enhanced Data Source Management (v0.1.0) ✅

### Recent Implementation

The admin interface has been significantly enhanced with comprehensive data source management capabilities:

#### Multi-Step Form Wizard
- **Step 1: Details** — Source name, description, destination table, and schema fields
- **Step 2: Ingestion** — File paths with CSV options and webhook configuration  
- **Step 3: Review** — Comprehensive validation and summary before saving

#### Key Features Implemented
- **Schema-First Architecture**: Define destination columns as reusable schema fields
- **Auto-Synced Webhooks**: Properties automatically mirror schema fields
- **Real-Time Validation**: Immediate feedback with comprehensive error handling
- **Tabular Field Management**: DataGrid interfaces for complex field mappings
- **Sample Request Generation**: Auto-generated JSON samples based on schema
- **Archive Management**: Soft-delete with purge options for data/table cleanup

#### Technical Improvements
- **Simplified Webhook Model**: One webhook per data source with auto-generated paths
- **Type Safety**: Full TypeScript integration with proper error handling
- **Enhanced UX**: Visual status indicators, real-time updates, guided workflows
- **Clean Architecture**: Separated UI state from entity management
