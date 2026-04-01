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
| Rule Engine | Custom recursive descent parser with AST evaluation |
| API docs | SpringDoc OpenAPI 2 (`/swagger-ui.html`, `/v3/api-docs`) |
| Frontend | React 18 · TypeScript · Vite · MUI v6 · MUI X DataGrid · Axios · Day.js |
| Build | Maven Wrapper (`./mvnw`) · frontend-maven-plugin |

Root package: `io.github.eendroroy.loyalty`

---

## Project Layout

```
src/main/java/.../loyalty/
  config/          # Spring beans: CorsConfig, JacksonConfig, OpenApiConfig, SchedulerConfig, SpaController
  controller/      # REST controllers (8 admin + WebhookIngestionController at /web-hook)
                   # DataSourceController, RuleController, VoucherController, MetadataController,
                   # MonitorController, DataSourceTableController, GlobalExceptionHandler
  dto/
    request/       # @Data @Builder @NoArgsConstructor @AllArgsConstructor
    response/      # @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(NON_NULL)
  entity/          # JPA entities (@Getter @Setter only — never @Data)
                   # Includes Voucher, VoucherInstance with unique secret codes
  enums/           # FieldDataType, FileProcessingStatus, RewardType, RuleStatus, TriggerType,
                   # WebhookContentType, VoucherType, ComparisonOperator
  event/           # Spring application event records (incl. RuleTriggeredEvent, DataPulledEvent)
  mapper/          # MapStruct interfaces + MapStructConfig (incl. VoucherMapper)
  model/           # Non-JPA value types: IngestedRecord, DataPullResult, EvaluationResult
  repository/      # Spring Data JPA repositories (incl. VoucherRepository, VoucherInstanceRepository)
  rule/            # Complete rule language engine
    ast/           # Abstract syntax tree: ParsedRule, LogicalNode, Condition, RewardSpec
    exception/     # RuleParseException for syntax errors
    RuleParser     # Recursive descent parser with integrated lexer
    RuleEvaluator  # Type-aware condition evaluation against IngestedRecord
  scheduler/       # RuleScheduler with RuleEvaluationService integration
  service/         # Interfaces only (incl. VoucherService, RuleEvaluationService)
  service/impl/    # @Service @RequiredArgsConstructor implementations
  watcher/         # FileWatcherService

frontend/src/
  api/             # Axios wrappers: client.ts, dataSources.ts, rules.ts, metadata.ts, 
                   # monitor.ts, vouchers.ts (with validateExpression)
  components/      # Shared UI: Layout, Sidebar, PageHeader, ConfirmDialog, FieldBrowser
  pages/           # Route-level components (incl. Vouchers, VoucherForm with live validation)
  types/index.ts   # All TypeScript interfaces and enums (incl. Voucher, VoucherInstance)
  theme.ts         # MUI dark theme (warm brick-red palette)
```

---

## Rule Language Engine — Core Implementation ✅

### Rule Language Syntax
```sql
WHEN <conditions> THEN <reward>

-- Operators: >, <, >=, <=, =, !=, CONTAINS, STARTS_WITH, ENDS_WITH
-- Logic: AND, OR, parentheses for grouping
-- Types: Numbers, dates (YYYY-MM-DD), strings, booleans
-- Rewards: Point(amount), Voucher(code)

-- Examples:
WHEN transaction.amount > 50 THEN Point(100)
WHEN transaction.category = "grocery" AND transaction.date >= 2025-01-01 THEN Point(30)
WHEN (transaction.amount > 100 OR customer.tier = "gold") THEN Voucher(SUMMER25)
```

### Parser Architecture
```java
// Rule parsing and evaluation flow:
RuleParser.parse(expression) → ParsedRule(condition, reward)
RuleEvaluator.evaluate(condition, record) → boolean
RuleEvaluationService → orchestrates evaluation + reward fulfillment
```

### AST Structure
```java
// Core AST classes in rule/ast/:
ParsedRule(LogicalNode condition, RewardSpec reward)
LogicalNode: AndNode | OrNode | LeafNode
Condition(FieldRef fieldRef, ComparisonOperator operator, String rawValue)
RewardSpec(RewardType type, String value)
```

### Expression Validation
```java
// Live validation endpoint:
POST /api/admin/rules/validate-expression
{ "expression": "WHEN transaction.amount > 50 THEN Point(100)" }
→ { "valid": true } | { "valid": false, "error": "..." }
```

---

## Voucher Management System ✅

### Entity Architecture
```java
@Entity Voucher {
    String name, code;           // Public voucher template
    Integer count;               // Max instances allowed
    Boolean active, archived;    // Status flags
    List<VoucherInstance> instances;
}

@Entity VoucherInstance {
    Voucher voucher;
    String secretCode;           // 7-char A-Z0-9, system-wide unique
}
```

### Secret Code Generation
```java
// VoucherServiceImpl pattern:
private String generateUniqueSecretCode() {
    for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
        var code = randomCode(); // 7-char A-Z0-9
        if (!voucherInstanceRepository.existsBySecretCode(code)) {
            return code;
        }
    }
    throw new IllegalStateException("Failed to generate unique code");
}
```

### Rule Integration
```java
// Auto-awarding via RuleEvaluationService:
if (reward.type() == RewardType.VOUCHER) {
    var voucherOpt = voucherService.findByCode(reward.value());
    var instance = voucherService.award(voucherOpt.get().getId());
    // Returns awarded secret code
}
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
    private final RuleParser ruleParser; // Inject for expression validation
    // ...
}
```
Controllers inject the **interface**, never the impl class.

### Rule Expression Validation Pattern
```java
// In RuleServiceImpl.save():
if (entity.getRuleExpression() != null && !entity.getRuleExpression().isBlank()) {
    ruleParser.parse(entity.getRuleExpression()); // Throws RuleParseException on error
}
```

### Transactional + Event-Driven Architecture
```java
@Transactional(readOnly = true)   // all read methods
@Transactional                     // all write methods

// Event publishing after commit:
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onDataPulled(DataPulledEvent event) {
    ruleEvaluationService.evaluate(event.records()); // Auto-evaluation
}
```

### OSIV is OFF — always JOIN FETCH before leaving a transaction
`spring.jpa.open-in-view: false`. Use the JOIN FETCH repository queries:

```java
// Good — loads relationships in one pass
ruleRepository.findByIdWithActions(id)
voucherRepository.findByIdWithInstances(id)

// Bad — lazy collections will explode outside transaction
repository.findById(id)
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
- Ignore `id`, `createdAt`, `updatedAt`, and relationship fields (`voucher`, `rule`) in `toEntity`
- Use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` on `updateEntity` for PATCH semantics

```java
@Mapper(config = MapStructConfig.class)
public interface VoucherMapper {
    VoucherResponse toResponse(Voucher entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "archived",  ignore = true)
    @Mapping(target = "instances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Voucher toEntity(VoucherRequest request);
}
```

### Archive / purge lifecycle (DataSource + Voucher)
```java
// Soft-delete pattern:
dataSourceService.archiveById(id);  // Sets archived = true, stops watchers
voucherService.archiveById(id);     // Sets archived = true, active = false

// Hard-delete pattern:
dataSourceService.purgeArchivedById(id); // 409 if destinationTable in use
voucherService.purgeArchivedById(id);     // 409 if not archived

// deleteById throws UnsupportedOperationException — use archive/purge instead
```

### Spring events — complete event system ✅
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onDataPulled(DataPulledEvent event) { ... }
```

| Event | Publisher | Listener |
|---|---|---|
| `DataPulledEvent(List<IngestedRecord>)` | `DataPullService`, `WebhookIngestionService` | `RuleEvaluationService` |
| `RuleTriggeredEvent(ruleId, record, rewardType, rewardValue)` | `RuleEvaluationService` | Future audit systems |
| `RuleSavedEvent` / `RuleDeletedEvent` | `RuleService` | `RuleScheduler` |
| `DataSourceFileSavedEvent` / `DataSourceFileDeletedEvent` | `DataSourceFileService` | `FileWatcherService` |

### Global Exception Handling ✅
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuleParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleRuleParseException(RuleParseException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Rule Expression Parse Error");
        return detail;
    }
}
```

### API response conventions
- `POST` → `201 Created`
- `DELETE` → `204 No Content` 
- `DELETE` archive/purge → `204` success, `404` not found, `409` conflict
- Missing resource → `404 Not Found`
- Rule parse error → `400 Bad Request` with ProblemDetail
- Every controller method needs `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`
- Every request DTO field needs `@Schema(description, example)`

---

## Frontend — Enhanced Patterns and Rules

### API layer with validation
```typescript
// api/rules.ts — includes live validation
export const validateExpression = (expression: string) =>
  client.post<{ valid: boolean; error?: string }>('/rules/validate-expression', { expression });

// api/vouchers.ts — complete CRUD + awarding
export const awardVoucher = (id: number) =>
  client.post<VoucherInstance>(`/vouchers/${id}/award`);
```

### Live Validation Pattern
```tsx
// In RuleForm.tsx — debounced expression validation
useEffect(() => {
  const timer = setTimeout(() => {
    if (form.ruleExpression.trim()) {
      setValidating(true);
      validateExpression(form.ruleExpression)
        .then((r) => setValidationResult(r.data))
        .catch(() => setValidationResult({ valid: false, error: 'Validation failed' }))
        .finally(() => setValidating(false));
    }
  }, 800); // 800ms debounce
  return () => clearTimeout(timer);
}, [form.ruleExpression]);
```

### Enhanced Component Patterns
```tsx
// Copy-to-clipboard with visual feedback (VoucherForm.tsx)
const handleCopy = (code: string) => {
  navigator.clipboard.writeText(code).then(() => {
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2000);
  });
};

// Syntax guide component with examples
<Box sx={{ mt: 1, p: 2, bgcolor: 'background.paper', border: '1px solid' }}>
  <Typography variant="subtitle2">📖 Rule Language Syntax</Typography>
  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
    <strong>Basic:</strong> WHEN [condition] THEN [reward]
  </Typography>
  {/* ...more examples... */}
</Box>
```

### Smart Field Insertion
```tsx
// Enhanced handleInsertField in RuleForm.tsx
const handleInsertField = (alias: string) => {
  // Auto-format as source.field
  let fieldRef = alias;
  if (metadata) {
    for (const source of metadata.dataSources) {
      const field = source.fields.find(f => f.alias === alias);
      if (field) {
        fieldRef = `${source.name}.${alias}`;
        break;
      }
    }
  }
  // Insert at cursor position...
};
```

### Route Management
```tsx
// App.tsx routing — includes voucher routes
<Route path="/vouchers"           element={<Vouchers />} />
<Route path="/vouchers/new"       element={<VoucherForm />} />
<Route path="/vouchers/:id"       element={<VoucherForm readOnly />} />
<Route path="/vouchers/:id/edit"  element={<VoucherForm />} />
```

### SpaController Updates ✅
```java
// SpaController.java — includes voucher routes
@GetMapping(value = {
    "/rules", "/rules/**",
    "/vouchers", "/vouchers/**",
    // ...existing routes...
})
```

---

## What Is NOW Implemented ✅

These features are **complete and production-ready**:

### ✅ Rule Language Engine
- Complete recursive descent parser with AST evaluation
- Live expression validation with syntax guide in admin UI
- Type-aware evaluation supporting all data types
- Complex logic with AND/OR operators and parentheses
- Automatic reward fulfillment (points logged, vouchers awarded)
- Event-driven evaluation triggered by data ingestion
- Reactive architecture with proper transaction isolation

### ✅ Voucher Management System
- Complete lifecycle: create, edit, view, archive, purge
- Unique 7-character alphanumeric secret code generation
- Capacity management with instance tracking
- Rule-triggered auto-awarding with conflict prevention
- Copy-to-clipboard functionality with visual feedback
- Archive/active separation with comprehensive status tracking

### ✅ Enhanced Data Source Management
- Multi-step wizard interface (Details → Ingestion → Review)
- Schema-first architecture with auto-synced webhook properties
- Real-time validation with immediate feedback
- Tabular field management using DataGrid components
- Auto-generated webhook paths and sample request bodies

### ✅ Live Validation & UI Enhancements
- Real-time rule expression validation with visual feedback
- Comprehensive syntax guide with examples
- Smart field insertion with `source.field` auto-formatting
- Enhanced error handling with detailed parse error messages
- Debounced validation to prevent API spam

---

## What Is NOT Yet Implemented

These features are planned but have no code — do not hallucinate their existence:
- `SCHEDULED` trigger type and `DataSourceScheduler`
- Kafka producers / consumers
- Authentication / authorization on any endpoint
- HMAC signature verification on `WebhookIngestionController` (`secretKey` stored, not yet verified)
- Advanced reporting / analytics dashboards
- Member account integration for point crediting

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

## Current Status (v0.2.0) — Production Ready ✅

### Rule Language & Voucher Management

**Complete Rule Language Engine**:
- Natural `WHEN ... THEN ...` syntax with full parser and evaluator
- Live expression validation with syntax guide in admin UI
- Type-aware evaluation supporting strings, numbers, dates, booleans
- Complex logic with AND/OR operators and parentheses grouping
- Automatic reward fulfillment for points and vouchers
- Event-driven evaluation on data ingestion

**Voucher Management System**:
- Complete voucher lifecycle (create, edit, archive, purge)
- Unique 7-character alphanumeric secret code generation
- Capacity management with instance tracking
- Auto-awarding through rule triggers with conflict prevention
- Copy-to-clipboard functionality for secret codes
- Archive/active separation with comprehensive status tracking

**Enhanced UI Experience**:
- Live rule expression validation with visual feedback
- Comprehensive syntax guide with examples and operator reference
- Smart field insertion with auto-formatted `source.field` references
- Debounced validation to prevent API spam
- Enhanced error handling with detailed parse error messages

### Enhanced Data Source Management (v0.1.0)

**Backend Improvements**:
- Schema-first architecture with auto-synced webhook properties
- Simplified webhook model (one per data source)
- Enhanced field management with atomic transactions
- Archive/purge lifecycle with conflict detection
- Comprehensive event system for reactive processing

**Frontend Enhancements**:
- Multi-step wizard interface (Details → Ingestion → Review)
- Real-time validation with immediate feedback
- Tabular field management using DataGrid components
- Auto-generated webhook paths and sample request bodies
- Enhanced error handling with comprehensive type safety

**Key Architecture Decisions**:
- **Schema-First Approach**: Webhook properties auto-sync from schema fields (no duplication)
- **Multi-Step UX**: Logical workflow progression with validation at each step
- **Real-Time Generation**: Auto-generated webhook paths and sample request bodies
- **Type Safety**: Strict TypeScript types with proper error handling throughout
- **Rule Engine Integration**: Complete evaluation system with reward fulfillment
- **Event-Driven Processing**: Reactive architecture with proper transaction isolation
