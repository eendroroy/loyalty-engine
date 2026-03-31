# GitHub Copilot Instructions — Loyalty Management System

## Stack at a Glance

| Layer | Technology |
|---|---|
| Backend | Java 25 · Spring Boot 4 · Spring Data JPA · Spring Kafka |
| Database | PostgreSQL — schema via Hibernate `ddl-auto: update`, no migrations |
| Messaging | Apache Kafka (dependency present; producers/consumers not yet coded) |
| ORM | Hibernate / Spring Data JPA + raw `JdbcTemplate` for destination tables |
| Mapping | MapStruct 1.6.3 (`componentModel = "spring"`) |
| Code gen | Lombok |
| API docs | SpringDoc OpenAPI 2 (`/swagger-ui.html`, `/v3/api-docs`) |
| Frontend | React 18 · TypeScript · Vite · MUI v6 · MUI X DataGrid · Axios · Day.js |
| Build | Maven Wrapper (`./mvnw`) · frontend-maven-plugin |

Root package: `io.github.eendroroy.loyalty`

---

## Project Layout

```
src/main/java/.../loyalty/
  config/          # Spring beans: CorsConfig, JacksonConfig, OpenApiConfig, SchedulerConfig, SpaController
  controller/      # REST controllers (6 admin + WebhookIngestionController at /web-hook)
  dto/
    request/       # @Data @Builder @NoArgsConstructor @AllArgsConstructor
    response/      # @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(NON_NULL)
  entity/          # JPA entities (@Getter @Setter only — never @Data)
  enums/           # FieldDataType, FileProcessingStatus, RewardType, RuleStatus, TriggerType,
                   # WebhookContentType
  event/           # Spring application event records
  mapper/          # MapStruct interfaces + MapStructConfig
  model/           # Non-JPA value types: IngestedRecord (record), DataPullResult (record)
  repository/      # Spring Data JPA repositories
  scheduler/       # RuleScheduler
  service/         # Interfaces only
  service/impl/    # @Service @RequiredArgsConstructor implementations
  watcher/         # FileWatcherService

frontend/src/
  api/             # Axios wrappers: client.ts, dataSources.ts, rules.ts, metadata.ts, monitor.ts
  components/      # Shared UI: Layout, Sidebar, PageHeader, ConfirmDialog, FieldBrowser
  pages/           # Route-level components
  types/index.ts   # All TypeScript interfaces and enums (incl. WebhookContentType, WebhookProperty)
  theme.ts         # MUI dark theme (warm brick-red palette)
```

---

## Backend — Non-Negotiable Rules

### Lombok on entities vs DTOs
```java
// Entities — ONLY these two. Never @Data (breaks Hibernate lazy loading).
@Getter @Setter

// Request DTOs
@Data @Builder @NoArgsConstructor @AllArgsConstructor

// Response DTOs
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
```

### Service layer — always interface + impl
```java
// service/MyService.java  (interface only)
public interface MyService { ... }

// service/impl/MyServiceImpl.java
@Service
@RequiredArgsConstructor
public class MyServiceImpl implements MyService {
    private final SomeRepository repo;
    // ...
}
```
Controllers inject the **interface**, never the impl class.

### Transactional
```java
@Transactional(readOnly = true)   // all read methods
@Transactional                     // all write methods
```
`FileProcessingLogService` methods use `Propagation.REQUIRES_NEW` to isolate deduplication constraint violations from the outer transaction.

### OSIV is OFF — always JOIN FETCH before leaving a transaction
`spring.jpa.open-in-view: false`. Accessing a lazy collection outside a transaction throws `LazyInitializationException`. Use the JOIN FETCH repository queries:

```java
// Good — loads files + fields in one pass
fileRepository.findByIdWithDataSourceAndFields(id)

// Bad — files.getFields() will explode outside the transaction
fileRepository.findById(id)
```

Multi-pass fetching pattern (used in `DataSourceServiceImpl`):
```java
repository.findByIdWithFiles(id);                               // pass 1: files bag
fileRepository.findByDataSourceIdWithFields(id);                // pass 2: fields on each file
repository.findByIdWithWebhooks(id);                            // pass 3: webhook (@OneToOne)
webhookRepository.findAllWithPropertiesByDataSourceId(id);      // pass 4: properties on the webhook
repository.findByIdWithSchemaFields(id);                        // pass 5: schema fields bag
```

### MapStruct mappers
Every mapper must:
- Ignore `id`, `createdAt`, `updatedAt`, and relationship fields (`dataSource`, `rule`) in `toEntity`
- Use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` on `updateEntity` for PATCH semantics

```java
@Mapper(config = MapStructConfig.class)
public interface ExampleMapper {
    ExampleResponse toResponse(Example entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent",    ignore = true)  // relationship
    Example toEntity(ExampleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent",    ignore = true)
    void updateEntity(ExampleRequest request, @MappingTarget Example entity);
}
```

### Field / file replacement — bulk DELETE before INSERT
Hibernate batches INSERTs before DELETEs. Using orphan removal causes unique-constraint violations on `field_alias`. Always use `@Modifying` JPQL bulk-delete:

```java
// Before inserting new fields for a file:
fieldRepository.deleteByDataSourceFileId(fileId);       // @Modifying, clearAutomatically = true

// Before deleting files for a source:
logRepository.deleteByDataSourceFileIdIn(fileIds);      // remove FK-constrained logs first
fieldRepository.deleteByDataSourceFileId(fid);          // per file
fileRepository.deleteByDataSourceId(dataSourceId);      // bulk delete files
```

### Spring events — never call schedulers or watchers directly
```java
// After commit, not inside the transaction:
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onFileSaved(DataSourceFileSavedEvent event) { ... }
```

| Event | Triggers |
|---|---|
| `DataSourceFileSavedEvent(fileId, filePath)` | `FileWatcherService` registers path |
| `DataSourceFileDeletedEvent(fileId)` | `FileWatcherService` deregisters path |
| `RuleSavedEvent` / `RuleDeletedEvent` | `RuleScheduler` reschedules / cancels |
| `DataPulledEvent(List<IngestedRecord>)` | future `RuleEvaluationService` |

Do **not** call `FileWatcherService` or `RuleScheduler` directly from a service or controller.

### DataSourceTableService — JDBC only, not JPA
```java
// Standard overloads — use DataSource.getAllFields() internally
tableService.createDestinationTableIfNotExists(dataSource);
tableService.insertRecords(dataSource, records);
tableService.deleteAllRecords(dataSource);
tableService.dropDestinationTable(dataSource);

// Explicit-fields overloads — used by WebhookIngestionService with synthetic DataSourceField list
tableService.createDestinationTableIfNotExists(dataSource, syntheticFields);
tableService.insertRecords(dataSource, records, syntheticFields);
```
SQL safety: table names and column identifiers are validated against allow-list patterns and double-quoted. User values always go through `?` bind parameters.

### Webhook property / before deleting webhooks — bulk DELETE properties first
```java
// Before bulk-deleting webhooks for a data source (avoids FK violation):
webhookPropertyRepository.deleteByWebhookIdIn(webhookIds);    // @Modifying, clearAutomatically = true
webhookRepository.deleteByDataSourceId(dataSourceId);

// Before deleting a single webhook:
webhookPropertyRepository.deleteByWebhookId(webhookId);
webhookRepository.deleteById(webhookId);
```

### Archive / purge lifecycle for DataSource
```java
// Soft-delete (stops file watchers via events):
dataSourceService.archiveById(id);

// Hard-delete (only after archiving; 409 if destinationTable in use):
dataSourceService.purgeArchivedById(id);

// deleteById throws UnsupportedOperationException — do not use it
// DELETE /{id} endpoint returns 405 Method Not Allowed — use /archive + /purge instead
```

### API response conventions
- `POST` → `201 Created`
- `DELETE` → `204 No Content`
- Missing resource → `404 Not Found`
- Conflict (e.g. purge with active table reference) → `409 Conflict`
- Every controller method needs `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`
- Every request DTO field needs `@Schema(description, example)`

### Enums
Stored as `EnumType.STRING`. Use the enum type directly — never raw string literals for type columns.

### Style
- Max line length 120 characters
- `var` for obvious local types
- No star (`*`) imports

---

## Frontend — Patterns and Rules

### API layer
All HTTP calls go through `frontend/src/api/client.ts` (Axios, `baseURL: '/api/admin'`). Add new endpoints to the relevant file in `api/`:

```typescript
// api/dataSources.ts — example pattern
export const archiveDataSource = (id: number) =>
  client.post(`/data-sources/${id}/archive`);
```

### Types
All shared types live in `frontend/src/types/index.ts`. Do not define types inline in component files.

### Pages and routing
Pages are in `frontend/src/pages/`. Routes are registered in `frontend/src/App.tsx`. Sidebar links are in `frontend/src/components/Sidebar.tsx`. The appbar title map is in `frontend/src/components/Layout.tsx` (`TITLES`). Add all three when creating a new page.

### Theme
Dark warm-red theme defined in `frontend/src/theme.ts`. Use MUI `sx` prop and theme tokens — no raw hex colours in components. Key palette tokens:
- `primary.main` — brick red `#c0453a`
- `secondary.main` — terracotta `#d4845a`
- Backgrounds: `background.default`, `background.paper`

### Component patterns
```tsx
// Confirmation before destructive actions — always use ConfirmDialog
<ConfirmDialog
  open={deleteId != null}
  title="Delete …"
  message="This action is irreversible."
  onConfirm={handleDelete}
  onCancel={() => setDeleteId(null)}
/>

// Empty-state overlay inside DataGrid
slots={{ noRowsOverlay: () => (
  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', py: 3 }}>
    <Typography variant="body2" color="text.secondary">No items yet.</Typography>
  </Box>
) }}
```

### DataGrid row actions pattern
```tsx
renderCell: ({ row }) => (
  <Box sx={{ display: 'flex', gap: 0.5 }}>
    <Tooltip title="Edit">
      <IconButton size="small" onClick={() => navigate(`/path/${row.id}/edit`)}>
        <EditRoundedIcon fontSize="small" />
      </IconButton>
    </Tooltip>
  </Box>
)
```
Always use `Rounded` icon variants (e.g. `EditRoundedIcon`, `DeleteForeverRoundedIcon`).

---

## What Is NOT Yet Implemented

These features are planned but have no code — do not hallucinate their existence:
- `SCHEDULED` trigger type and `DataSourceScheduler`
- `RuleEvaluationService` (only a TODO stub in `RuleScheduler.evaluate()`)
- Kafka producers / consumers
- Authentication / authorization on any endpoint
- HMAC signature verification on `WebhookIngestionController` (`secretKey` stored, not yet verified)
- `DataSource.type`, `triggerType`, or `pullFrequency` fields do not exist on the entity

---

## Build Commands

```bash
mvn clean package -DskipTests          # full build (backend + frontend bundle)
mvn spring-boot:run                    # run locally
mvn compile -DskipTests -Dfrontend.build.skip=true  # backend only, fast
mvn test                               # run tests
```

First-time setup:
```bash
cp src/main/resources/application.example.yaml src/main/resources/application.yaml
# fill in <HOST>, <DATABASE>, <USERNAME>, <PASSWORD>, <KAFKA_HOST>
```

---

## Recent Updates (v0.1.0) ✅

### Enhanced Data Source Management

**Backend Changes**:
- Added `description` field to `DataSourceWebhook` entity and DTOs
- Enhanced webhook response builders with auto-generated paths and sample request bodies
- Simplified webhook model: one webhook per data source with auto-synced schema field properties
- Fixed mapper type inference issues and service implementations
- Updated `WebhookIngestionServiceImpl` to use schema fields instead of deprecated properties

**Frontend Changes**:
- Complete rewrite of `DataSourceForm.tsx` with multi-step wizard (Details → Ingestion → Review)
- Enhanced webhook configuration UI with description, enable/disable toggle, and sample JSON generation
- Added tabular field management using DataGrid components with real-time validation
- Fixed webhook column in `DataSources.tsx` to show status instead of count
- Full TypeScript type safety with comprehensive error handling

**Key Architecture Decisions**:
- **Schema-First Approach**: Webhook properties auto-sync from schema fields (no duplication)
- **Multi-Step UX**: Logical workflow progression with validation at each step
- **Real-Time Generation**: Auto-generated webhook paths and sample request bodies
- **Type Safety**: Strict TypeScript types with proper error handling throughout

