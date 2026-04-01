# GitHub Copilot Instructions — Loyalty Management System

## Documentation Management Standards

### **CRITICAL: Documentation Standards & Maintenance**

**Documentation Files Structure:**
- `README.md` — Main repository landing page (standard GitHub format)
- `documentation/REQUIREMENTS.md` — All technical specifications and system architecture
- `documentation/BRD.md` — Business requirements document (for business stakeholders only)
- `documentation/diagrams/` — Technical diagrams (system architecture, database schema, deployment)
- `.github/copilot-instructions.md` — This file (complete technical implementation guide)

**Documentation Rules:**
1. **NEVER create new `.md` files** — only maintain existing ones
2. **Always update documentation** when project changes occur
3. **Keep technical content isolated** in `documentation/REQUIREMENTS.md` only
4. **Keep business content isolated** in `documentation/BRD.md` only (strictly business-focused)
5. **Maintain complete rebuild instructions** across these files
6. **Update this file** when architecture or patterns change

**Project Recreation Capability:**
These documentation files contain ALL information needed to recreate the entire project from scratch, including detailed technical specifications, business requirements, implementation patterns, and architectural decisions.

**When making ANY project changes:**
1. Update relevant documentation files immediately
2. Ensure these files collectively contain complete rebuild capability
3. Keep README.md as professional repository overview
4. Maintain consistency across all documentation
5. Document architectural decisions in this file
6. Keep business requirements in `documentation/BRD.md` (strictly business-focused)
7. Keep technical specifications in `documentation/REQUIREMENTS.md`

---

# GitHub Copilot Instructions — Loyalty Management System

## Stack at a Glance

| Layer | Technology |
|---|---|
| Backend | Java 25 · Spring Boot 4 · Spring Data JPA · Spring Kafka |
| Database | PostgreSQL — schema via Hibernate `ddl-auto: update`, no migrations |
| Messaging | Apache Kafka (dependency present; producers/consumers not yet coded) |
| ORM | Hibernate / Spring Data JPA + modern `JdbcClient` for destination tables |
| Mapping | MapStruct 1.6.3 (`componentModel = "spring"`) |
| Code gen | Lombok |
| Rule Engine | Custom recursive descent parser with AST evaluation |
| API docs | SpringDoc OpenAPI 2 (`/swagger-ui.html`, `/v3/api-docs`) |
| Frontend | React 18 · TypeScript · Vite · MUI v6 · MUI X DataGrid · Axios · Day.js · Recharts |
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
  model/           # Non-JPA value types: IngestedRecord (with metadata tracking), DataPullResult, EvaluationResult
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
  theme.ts         # Dual-mode design system: light (default) + dark (near-black navy, vivid crimson #E53935)
                   # createAppTheme(mode) — light is default, dark matches high-contrast dashboard aesthetic
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

### Enhanced Visual Rule Builder Component
```tsx
// RuleBuilder.tsx - Complete visual rule construction interface
const RuleBuilder = ({ metadata, initialExpression, onExpressionChange }: Props) => {
  const [conditionGroups, setConditionGroups] = useState<ConditionGroup[]>([]);
  const [reward, setReward] = useState<Reward>({ type: 'POINT', value: '' });
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  
  // Load active vouchers for dropdown
  useEffect(() => {
    getVouchers().then(response => 
      setVouchers(response.data.filter(v => v.active && !v.archived))
    );
  }, []);
  
  // Generate expression from visual components
  const generateExpression = (): string => {
    // Complex logic for building WHEN...THEN expressions with proper parentheses
  };
  
  // Render structured WHEN/THEN interface with:
  // - Expandable condition groups
  // - Three-column condition builder
  // - Live voucher dropdown
  // - Real-time expression preview
};
```

### Dual-Mode Rule Form Integration
```tsx
// RuleForm.tsx - Toggle between text and visual modes
const RuleForm = () => {
  const [ruleEditorMode, setRuleEditorMode] = useState<'text' | 'visual'>('text');
  
  // Toggle interface with seamless mode switching
  <ToggleButtonGroup
    value={ruleEditorMode}
    exclusive
    onChange={(_, newMode) => setRuleEditorMode(newMode)}
  >
    <ToggleButton value="text">
      <CodeRoundedIcon /> Text Editor
    </ToggleButton>
    <ToggleButton value="visual">
      <BuildRoundedIcon /> Visual Builder
    </ToggleButton>
  </ToggleButtonGroup>
  
  // Conditional rendering based on mode
  {ruleEditorMode === 'text' ? (
    <TextField /* enhanced text editor with syntax guide */ />
  ) : (
    <RuleBuilder
      metadata={metadata}
      initialExpression={form.ruleExpression}
      onExpressionChange={(expression) => setForm(prev => ({ ...prev, ruleExpression: expression }))}
    />
  )}
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

### ✅ Automatic Data Lineage & Metadata Tracking
- **Source Tracking**: Every imported row includes `source` metadata ("FILE:<filename>" or "HOOK:<producer>")
- **Temporal Tracking**: `file_read_time` timestamp captures exact ingestion time
- **Destination Table Enhancement**: All tables automatically include metadata columns
- **Complete Audit Trail**: Full traceability from raw data to processed records
- **Type-Safe Integration**: Enhanced `IngestedRecord` model with metadata fields
- **Modern JDBC**: Upgraded to Spring 6.1+ `JdbcClient` for improved performance and safety

### ✅ Live Validation & UI Enhancements
- Real-time rule expression validation with visual feedback
- Comprehensive syntax guide with examples
- Smart field insertion with `source.field` auto-formatting
- Enhanced error handling with detailed parse error messages
- Debounced validation to prevent API spam

### ✅ Dual-Mode Design System (v0.4.0)
- **Light theme (default)**: Soft blue-grey canvas (`#F1F4F9`), white cards, vivid crimson accents
- **Dark theme**: Near-black navy canvas (`#0B0C12`), dark charcoal cards (`#14151F`), vivid crimson (`#E53935`)
- Glassmorphism AppBar with backdrop-blur in both modes
- Red gradient buttons with crimson glow shadow
- Custom scrollbar, refined typography scale with negative letter-spacing
- Context-aware Chip, TableCell, Tooltip, Dialog, ToggleButton overrides

### ✅ Enhanced Dashboard (v0.4.0)
- 4 stat cards with gradient icon bubbles, accent top-bar, trend badges
- Recharts AreaChart with red gradient fill (matches dashboard image aesthetic)
- Activity Feed panel with colored dots and Rule Health progress bar
- Recent Rules table with priority badges and monospace expression preview

### ✅ Enhanced UI Shell (v0.4.0)
- Sidebar: gradient brand logo, version badge, "Main Menu" section label
- AppBar: glassmorphism blur, notification badge, gradient avatar with hover scale
- PageHeader: subtitle support, secondary action button, NavigateNext breadcrumb separator
- **Dual-Mode Interface**: Toggle between text editor and visual builder
- **Structured WHEN/THEN Sections**: Clear visual hierarchy with prominent section headers
- **Three-Column Condition Builder**: Property → Operator → Value layout with smart filtering
- **Advanced Grouping**: Visual condition groups with expand/collapse and parentheses support
- **Live Voucher Integration**: Real-time voucher dropdown with availability tracking
- **Expression Preview**: Prominent rule text display at top with copy functionality
- **Context-Aware UI**: Field type-based operator filtering and input controls
- **Progressive Disclosure**: Expandable groups for better organization
- **Mobile Responsive**: Full mobile and desktop support with accessibility features

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

## Current Status (v0.3.0) — Production Ready ✅

### Enhanced Visual Rule Builder & Complete Platform

**Enhanced Visual Rule Builder (v0.3.0)**:
- Dual-mode interface with seamless text/visual switching
- Structured WHEN/THEN sections with clear visual hierarchy  
- Three-column condition builder: Property → Operator → Value
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
