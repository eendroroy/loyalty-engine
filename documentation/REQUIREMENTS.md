# Technical Requirements Specification — Loyalty Management System

**Document Type**: Technical Requirements Specification  
**Version**: 2.0  
**Date**: April 1, 2026  
**Status**: Production Ready  
**Target Architecture**: Microservices with Event-Driven Architecture

---

## System Architecture Overview

### High-Level Architecture

The Loyalty Management System follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  React 18 SPA + TypeScript + MUI v6 + Vite                │
└─────────────────────┬───────────────────────────────────────┘
                      │ REST API / JSON
┌─────────────────────┴───────────────────────────────────────┐
│                   Application Layer                         │
│  Spring Boot 4 + Spring WebMVC + SpringDoc OpenAPI        │
└─────────────────────┬───────────────────────────────────────┘
                      │ Service Calls
┌─────────────────────┴───────────────────────────────────────┐
│                    Business Layer                           │
│  Rule Engine + Service Layer + Event Processing            │
└─────────────────────┬───────────────────────────────────────┘
                      │ JPA/JDBC
┌─────────────────────┴───────────────────────────────────────┐
│                   Data Access Layer                         │
│  Spring Data JPA + Hibernate + PostgreSQL                  │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack Specifications

| Component | Technology | Version | Purpose | Configuration |
|-----------|------------|---------|---------|---------------|
| **Runtime** | Java OpenJDK | 25+ | Application runtime | `--enable-native-access=ALL-UNNAMED` |
| **Framework** | Spring Boot | 4.0.5 | Web application framework | Auto-configuration enabled |
| **Data Access** | Spring Data JPA | 4.0.4 | ORM abstraction layer | `ddl-auto: update` |
| **Database** | PostgreSQL | 15+ | Primary data store | Connection pooling via HikariCP |
| **Messaging** | Spring Kafka | 4.0.4 | Event streaming | Bootstrap servers configurable |
| **Object Mapping** | MapStruct | 1.6.3 | Entity-DTO mapping | `componentModel = "spring"` |
| **Code Generation** | Lombok | Latest | Boilerplate reduction | Annotation processing |
| **API Documentation** | SpringDoc OpenAPI | 2.8.5 | API documentation | Swagger UI + OpenAPI 3.0 |
| **Build Tool** | Maven | 3.9+ | Build automation | Multi-module support |
| **Frontend Framework** | React | 18+ | UI framework | Functional components + hooks |
| **Type System** | TypeScript | 5+ | Type-safe JavaScript | Strict mode enabled |
| **Build Tool (FE)** | Vite | 6+ | Frontend bundler | Hot reload + tree shaking |
| **UI Library** | MUI | v6+ | Material Design components | Custom theme + dark mode |
| **HTTP Client** | Axios | Latest | API communication | Interceptors + error handling |

---

## Frontend Architecture Specifications

### Technology Stack (Frontend)

| Component | Technology | Version | Purpose | Configuration |
|-----------|------------|---------|---------|---------------|
| **Framework** | React | 18+ | UI framework | Functional components with hooks |
| **Type System** | TypeScript | 5+ | Type safety | Strict mode enabled |
| **Build Tool** | Vite | 6+ | Frontend bundler | Hot reload, tree shaking, ESBuild |
| **UI Library** | Material-UI (MUI) | v6+ | Component library | Custom theme with dual-mode support |
| **State Management** | React Context | Built-in | Global state | Theme and user preferences |
| **HTTP Client** | Axios | Latest | API communication | Request/response interceptors |
| **Date Handling** | Day.js | Latest | Date manipulation | Lightweight moment.js alternative |
| **Icons** | MUI Icons | v6+ | Icon system | Material Design icons |
| **Data Grid** | MUI X DataGrid | v7+ | Advanced tables | Sorting, filtering, pagination |

### Modern Dashboard Theme System

#### Color Palette Specifications
```typescript
// Primary Brand Colors (Red Theme)
PRIMARY_RED: '#DC2626'      // Modern bright red - main primary
PRIMARY_DARK: '#B91C1C'     // Darker red for hover states  
PRIMARY_LIGHT: '#F87171'    // Light red for accents

// Secondary Colors (Complementary)
SECONDARY_BLUE: '#3B82F6'   // Modern blue for secondary actions
SECONDARY_DARK: '#1E40AF'   // Dark blue
SECONDARY_LIGHT: '#60A5FA'  // Light blue

// System Colors
SUCCESS_GREEN: '#059669'    // Modern green
WARNING_AMBER: '#D97706'    // Modern amber/orange
ERROR_RED: '#DC2626'        // Same as primary red
INFO_BLUE: '#0EA5E9'        // Sky blue for info

// Dark Theme Colors
DARK_BG: '#0F0F23'          // Very dark navy/black background
DARK_PAPER: '#1A1B2E'       // Dark paper/card backgrounds
DARK_SURFACE: '#16213E'     // Sidebar/appbar surface
DARK_ACCENT: '#E53E3E'      // Bright red accent for dark mode

// Light Theme Colors  
LIGHT_BG: '#FFFFFF'         // Pure white background
LIGHT_PAPER: '#FAFAFA'      // Light gray for cards
LIGHT_SURFACE: '#F8FAFC'    // Very light gray for surfaces
LIGHT_ACCENT: '#DC2626'     // Red accent for light mode
```

#### Typography Specifications
```typescript
// Font Family: "Inter", "Segoe UI", "Roboto", "Helvetica Neue"
h1: { fontWeight: 700, fontSize: '2.125rem' }
h2: { fontWeight: 600, fontSize: '1.875rem' }
h3: { fontWeight: 600, fontSize: '1.5rem' }
h4: { fontWeight: 600, fontSize: '1.25rem' }
h5: { fontWeight: 600, fontSize: '1.125rem' }
h6: { fontWeight: 600, fontSize: '1rem' }
body1: { fontSize: '0.875rem', lineHeight: 1.6 }
button: { fontWeight: 500, textTransform: 'none' }
```

#### Component Styling Standards
- **Border Radius**: 8-12px for modern look
- **Shadows**: Enhanced depth with proper elevation
- **Transitions**: 0.2s ease-in-out for smooth interactions
- **Hover Effects**: Subtle transform animations (translateY(-1px))
- **Button Styling**: Enhanced shadows with color variations
- **Input Borders**: 2px focus borders with accent colors
- **Card Design**: Clean shadows with subtle borders

#### Theme Mode System
- **Default Mode**: Light mode (production default)
- **Dark Mode**: Available via context toggle
- **Persistence**: Theme preference stored in localStorage
- **System Integration**: Respects system preferences
- **Context API**: Global theme state management
- **Type Safety**: Full TypeScript support for custom palette

### Component Architecture Patterns

#### Visual Rule Builder (Enhanced)
```typescript
// Dual-mode editing interface
interface RuleBuilderProps {
  metadata: Metadata;
  initialExpression?: string;
  onExpressionChange: (expression: string) => void;
}

// Three-column condition builder
interface ConditionBuilder {
  property: string;    // Data source field
  operator: ComparisonOperator;
  value: string | number | boolean;
}
```

#### Form Validation Patterns
```typescript
// Live validation with debouncing
const [validationResult, setValidationResult] = useState<ValidationResult>();
const [isValidating, setIsValidating] = useState(false);

// 800ms debounce for expression validation
useEffect(() => {
  const timer = setTimeout(() => validateExpression(), 800);
  return () => clearTimeout(timer);
}, [expression]);
```

#### Data Grid Integration
- **MUI X DataGrid**: Advanced table functionality
- **Server-side operations**: Sorting, filtering, pagination
- **Custom toolbar**: Export, search, column management
- **Responsive design**: Mobile-friendly layouts
- **Type safety**: Full TypeScript integration

---

## System Requirements

### Functional Requirements

#### FR-001: Rule Language Parser
**Requirement**: System shall implement recursive descent parser for rule expressions
**Technical Specification**:
- ANTLR4-based lexer and parser for `WHEN...THEN` syntax
- Abstract Syntax Tree (AST) generation with type checking
- Support for logical operators (AND, OR) with precedence rules
- Parentheses grouping with proper associativity
- Field reference validation against metadata schema
- Type coercion for string, numeric, date, and boolean comparisons

**Implementation Classes**:
```java
@Component
public class RuleParser {
    public ParsedRule parse(String expression) throws RuleParseException;
}

public record ParsedRule(LogicalNode condition, RewardSpec reward) {}
public sealed interface LogicalNode permits AndNode, OrNode, LeafNode {}
```

#### FR-002: Real-Time Rule Evaluation Engine
**Requirement**: System shall evaluate rules against data records with sub-100ms latency
**Technical Specification**:
- In-memory rule cache with LRU eviction policy
- Parallel rule evaluation using CompletableFuture
- Circuit breaker pattern for rule execution failures
- Performance metrics collection with Micrometer
- Conditional short-circuiting for AND operations

**Performance Requirements**:
- Throughput: 10,000 evaluations per second per CPU core
- Latency: P95 < 50ms, P99 < 100ms
- Memory usage: < 512MB for 10,000 active rules
- CPU usage: < 40% during peak load

#### FR-003: Voucher Code Generation System
**Requirement**: System shall generate cryptographically secure unique voucher codes
**Technical Specification**:
- SecureRandom-based code generation with entropy pool
- Base32 encoding for human-readable codes (A-Z, 0-9)
- Code length: 7 characters (32^7 = 1.07 billion combinations)
- Collision detection with database unique constraint
- Maximum retry attempts: 10 with exponential backoff

**Security Requirements**:
- Codes must be unpredictable and non-sequential
- No correlation between codes and customer data
- Resistance to brute force attacks (rate limiting)

### Non-Functional Requirements

#### NFR-001: Performance Requirements
- **Response Time**: API responses < 200ms (P95), < 500ms (P99)
- **Throughput**: 1,000 concurrent users, 10,000 transactions/minute
- **Scalability**: Horizontal scaling to 10x current load
- **Resource Usage**: < 2GB RAM per application instance

#### NFR-002: Reliability Requirements
- **Availability**: 99.9% uptime (8.76 hours downtime/year)
- **Data Durability**: 99.999% (RPO = 1 hour, RTO = 4 hours)
- **Fault Tolerance**: Graceful degradation during partial failures
- **Monitoring**: Health checks, metrics, distributed tracing

#### NFR-003: Security Requirements
- **Authentication**: JWT-based stateless authentication (future)
- **Authorization**: Role-based access control (RBAC)
- **Data Encryption**: TLS 1.3 in transit, AES-256 at rest
- **Input Validation**: Comprehensive sanitization and validation
- **Audit Logging**: Complete audit trail for all operations

#### NFR-004: Maintainability Requirements
- **Code Coverage**: Minimum 80% unit test coverage
- **Documentation**: Comprehensive API documentation via OpenAPI
- **Logging**: Structured logging with correlation IDs
- **Configuration**: Externalized configuration management

---

## Database Design Specifications

### Entity Relationship Model

#### Core Entities

**DataSource Entity**
```sql
CREATE TABLE data_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    destination_table VARCHAR(255),
    archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_data_sources_name ON data_sources(name);
CREATE INDEX idx_data_sources_archived ON data_sources(archived);
```

**Rule Entity**
```sql
CREATE TABLE rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_expression TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    priority INTEGER NOT NULL DEFAULT 0,
    active_from DATE,
    active_to DATE,
    frequency VARCHAR(50),
    last_run_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT chk_rule_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_rules_status ON rules(status);
CREATE INDEX idx_rules_priority ON rules(priority DESC);
CREATE INDEX idx_rules_active_dates ON rules(active_from, active_to);
```

**Voucher Management Schema**
```sql
CREATE TABLE vouchers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    voucher_type VARCHAR(20) NOT NULL DEFAULT 'DISCOUNT',
    count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT chk_voucher_count CHECK (count >= 0),
    CONSTRAINT chk_voucher_type CHECK (voucher_type IN ('DISCOUNT'))
);

CREATE TABLE voucher_instances (
    id BIGSERIAL PRIMARY KEY,
    voucher_id BIGINT NOT NULL REFERENCES vouchers(id),
    secret_code VARCHAR(7) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT chk_secret_code_format CHECK (secret_code ~ '^[A-Z0-9]{7}$')
);

CREATE INDEX idx_voucher_instances_voucher_id ON voucher_instances(voucher_id);
CREATE INDEX idx_voucher_instances_secret_code ON voucher_instances(secret_code);
```

**Dynamic Destination Tables**

Destination tables are dynamically created for each data source to store ingested records. These tables include automatic metadata tracking for data lineage and audit purposes.

```sql
-- Example destination table structure (auto-generated)
CREATE TABLE customer_transactions (  -- Name from DataSource.destination_table
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata columns (automatically added to all destination tables)
    source VARCHAR(255),           -- Source identifier: "FILE:<filename>" or "HOOK:<producer>"
    file_read_time TIMESTAMP,      -- Timestamp when data was ingested
    
    -- User-defined data columns (from DataSourceField or DataSourceSchemaField)
    customer_id VARCHAR(255),      -- Example: mapped from source field
    transaction_amount DECIMAL(19, 2),
    transaction_date DATE,
    transaction_type VARCHAR(255),
    -- ... additional fields as defined in schema
);
```

**Metadata Column Details:**
- `source`: Tracks data origin with format:
  - `"FILE:transactions_2024.csv"` for file uploads
  - `"HOOK:payment_gateway"` for webhook ingestion
- `file_read_time`: Precise timestamp when the source data was processed
- Enables complete data lineage tracking and audit capabilities
- Supports data quality analysis and source-specific debugging

### Data Access Patterns
```java
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.instances WHERE v.id = :id")
    Optional<Voucher> findByIdWithInstances(@Param("id") Long id);
    
    @Query("SELECT v FROM Voucher v WHERE v.active = true AND v.archived = false")
    List<Voucher> findAllActive();
    
    Optional<Voucher> findByCode(String code);
}

@Repository
public interface VoucherInstanceRepository extends JpaRepository<VoucherInstance, Long> {
    
    boolean existsBySecretCode(String secretCode);
    
    @Query("SELECT COUNT(vi) FROM VoucherInstance vi WHERE vi.voucher.id = :voucherId")
    long countByVoucherId(@Param("voucherId") Long voucherId);
}
```

#### Transaction Management
```java
// Service layer transaction configuration
@Service
@RequiredArgsConstructor
@Transactional
public class VoucherServiceImpl implements VoucherService {
    
    @Transactional(readOnly = true)
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public VoucherInstance award(Long voucherId) {
        // Implementation with proper isolation level
    }
}
```

---

## API Specifications

### RESTful API Design

#### Base URL Structure
```
Production:  https://api.loyalty.company.com/v1
Staging:     https://staging-api.loyalty.company.com/v1
Development: http://localhost:8080/api/admin
```

#### Authentication & Authorization
```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
Accept: application/json
X-Correlation-ID: <UUID>
```

#### Core API Endpoints

**Rule Management API**
```http
GET    /rules                    # List all rules with pagination
POST   /rules                    # Create new rule
GET    /rules/{id}               # Get rule by ID
PUT    /rules/{id}               # Update rule
DELETE /rules/{id}               # Delete rule (soft delete)

POST   /rules/validate-expression # Validate rule expression
GET    /rules/{id}/test          # Test rule against sample data
POST   /rules/{id}/activate      # Activate rule
POST   /rules/{id}/deactivate    # Deactivate rule
```

**Voucher Management API**
```http
GET    /vouchers                 # List all vouchers
POST   /vouchers                 # Create voucher template
GET    /vouchers/{id}            # Get voucher details
PUT    /vouchers/{id}            # Update voucher
DELETE /vouchers/{id}            # Archive voucher

POST   /vouchers/{id}/award      # Award voucher instance
GET    /vouchers/{id}/instances  # List voucher instances
POST   /vouchers/{id}/validate   # Validate voucher code
```

**Data Source API**
```http
GET    /data-sources             # List data sources
POST   /data-sources             # Create data source
GET    /data-sources/{id}        # Get data source
PUT    /data-sources/{id}        # Update data source
DELETE /data-sources/{id}        # Archive data source

POST   /data-sources/{id}/files  # Upload CSV file
GET    /data-sources/{id}/schema # Get field schema
POST   /data-sources/{id}/test   # Test webhook endpoint
```

#### API Response Standards

**Success Response Format**
```json
{
  "status": "success",
  "data": { ... },
  "meta": {
    "timestamp": "2026-04-01T10:30:00Z",
    "version": "v1.0",
    "correlationId": "uuid"
  }
}
```

**Error Response Format (RFC 7807 Problem Details)**
```json
{
  "type": "/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Rule expression contains syntax error",
  "instance": "/rules/123",
  "errors": [
    {
      "field": "ruleExpression",
      "message": "Expected 'THEN' after condition",
      "code": "SYNTAX_ERROR"
    }
  ]
}
```

### Webhook Integration Specifications

#### Inbound Webhook Format
```http
POST /web-hook/{dataSourceName}
Content-Type: application/json
X-Signature: sha256=<HMAC-SHA256>
X-Timestamp: <Unix-timestamp>

{
  "transactionId": "TXN-2026-001",
  "customerId": "CUST-12345",
  "amount": 125.50,
  "currency": "USD",
  "category": "electronics",
  "timestamp": "2026-04-01T10:30:00Z",
  "metadata": {
    "storeId": "STORE-001",
    "channel": "online"
  }
}
```

#### Data Validation Rules
- All monetary amounts must be positive decimal values
- Transaction IDs must be unique within 24-hour window
- Timestamps must be in ISO 8601 format
- Customer IDs must be valid alphanumeric strings
- Currency codes must follow ISO 4217 standard

---

## Event-Driven Architecture

### Event Types and Schema

#### DataPulledEvent
```java
public record DataPulledEvent(
    String dataSourceName,
    List<IngestedRecord> records,
    Instant timestamp,
    String correlationId
) implements ApplicationEvent {}

public record IngestedRecord(
    Long dataSourceId,        // ID of the originating DataSource
    String sourceName,        // Human-readable name of the originating source
    LocalDateTime ingestedAt, // Wall-clock time when record was produced
    String source,           // Source identifier: "FILE:<filename>" or "HOOK:<producer>"
    LocalDateTime fileReadTime, // Timestamp when the source data was read/received
    Map<String, Object> fields  // Field alias → typed-value map for this row
) {}
```

#### RuleTriggeredEvent
```java
public record RuleTriggeredEvent(
    Long ruleId,
    String ruleName,
    IngestedRecord record,
    RewardType rewardType,
    String rewardValue,
    Instant triggeredAt,
    String correlationId
) implements ApplicationEvent {}
```

### Event Processing Pipeline

#### Asynchronous Event Handling
```java
@Component
@RequiredArgsConstructor
public class RuleEvaluationEventHandler {
    
    private final RuleEvaluationService ruleEvaluationService;
    private final ApplicationEventPublisher eventPublisher;
    
    @EventListener
    @Async("ruleEvaluationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataPulled(DataPulledEvent event) {
        try {
            List<EvaluationResult> results = ruleEvaluationService.evaluateAll(event.records());
            results.forEach(this::publishRuleTriggered);
        } catch (Exception ex) {
            log.error("Rule evaluation failed for correlation ID: {}", event.correlationId(), ex);
            // Publish failure event for monitoring
        }
    }
}
```

#### Event Store Configuration
```java
@Configuration
@EnableAsync
public class EventConfiguration {
    
    @Bean("ruleEvaluationExecutor")
    public TaskExecutor ruleEvaluationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("rule-eval-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
```

---

## Security Specifications

### Authentication Architecture

#### JWT Token Structure
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",
    "iss": "loyalty-system",
    "aud": "admin-ui",
    "exp": 1735689600,
    "iat": 1735603200,
    "roles": ["ADMIN", "RULE_MANAGER"],
    "permissions": ["READ_RULES", "WRITE_RULES", "MANAGE_VOUCHERS"]
  }
}
```

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/web-hook/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())))
            .build();
    }
}
```

### Data Protection

#### Encryption Standards
- **At Rest**: AES-256-GCM for sensitive data fields
- **In Transit**: TLS 1.3 with perfect forward secrecy
- **Key Management**: HSM-based key rotation every 90 days
- **Database**: Transparent Data Encryption (TDE) enabled

#### Privacy Controls
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CustomerData {
    
    @Column
    @Convert(converter = EncryptedStringConverter.class)
    private String email;
    
    @Column
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;
    
    // Audit fields
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    @CreatedBy
    private String createdBy;
}
```

---

## Deployment and Infrastructure

### Container Specifications

#### Docker Configuration
```dockerfile
FROM eclipse-temurin:25-jre-alpine

LABEL maintainer="loyalty-team@company.com"
LABEL version="1.0.0"

# Security: Run as non-root user
RUN addgroup -g 1001 loyalty && adduser -D -s /bin/sh -u 1001 -G loyalty loyalty

# Application setup
WORKDIR /app
COPY --chown=loyalty:loyalty target/loyalty-*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

USER loyalty
EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
    "-jar", "app.jar"]
```

#### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: loyalty-management-system
  labels:
    app: loyalty-system
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: loyalty-system
  template:
    metadata:
      labels:
        app: loyalty-system
        version: v1.0.0
    spec:
      containers:
      - name: loyalty-app
        image: loyalty/management-system:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: host
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### Monitoring and Observability

#### Metrics Collection
```java
@Component
public class RuleMetrics {
    
    private final Counter ruleEvaluationsTotal;
    private final Timer ruleEvaluationDuration;
    private final Gauge activeRulesCount;
    
    public RuleMetrics(MeterRegistry meterRegistry) {
        this.ruleEvaluationsTotal = Counter.builder("rule.evaluations.total")
            .description("Total number of rule evaluations")
            .tag("outcome", "success")
            .register(meterRegistry);
            
        this.ruleEvaluationDuration = Timer.builder("rule.evaluation.duration")
            .description("Rule evaluation duration")
            .register(meterRegistry);
            
        this.activeRulesCount = Gauge.builder("rules.active.count")
            .description("Number of active rules")
            .register(meterRegistry, this, RuleMetrics::getActiveRuleCount);
    }
}
```

#### Distributed Tracing
```java
@Configuration
@EnableJpaRepositories
public class TracingConfiguration {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
            .additionalInterceptors(new TraceRestTemplateInterceptor())
            .build();
    }
}

@Service
@RequiredArgsConstructor
public class RuleEvaluationService {
    
    @NewSpan("rule-evaluation")
    public List<EvaluationResult> evaluateAll(@SpanTag("record-count") List<IngestedRecord> records) {
        return records.parallelStream()
            .map(this::evaluateRecord)
            .collect(Collectors.toList());
    }
}
```

---

## Development and Build Specifications

### Build Configuration

#### Maven POM Structure
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.5</version>
        <relativePath/>
    </parent>
    
    <groupId>io.github.eendroroy</groupId>
    <artifactId>loyalty</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>25</java.version>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok.version>1.18.36</lombok.version>
        <springdoc.version>2.8.5</springdoc.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Code Generation -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        
        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-csv</artifactId>
            <version>1.12.0</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.17.0</version>
        </dependency>
        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
            <version>1.19.0</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Spring Boot Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <!-- Compiler Plugin with Annotation Processing -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.1</version>
                <configuration>
                    <source>25</source>
                    <target>25</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            
            <!-- Frontend Build Integration -->
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.15.1</version>
                <configuration>
                    <workingDirectory>frontend</workingDirectory>
                    <installDirectory>frontend/node</installDirectory>
                </configuration>
                <executions>
                    <execution>
                        <id>install-node-and-npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>
                        <configuration>
                            <nodeVersion>v22.14.0</nodeVersion>
                            <npmVersion>10.9.2</npmVersion>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm-install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <configuration>
                            <arguments>install</arguments>
                        </configuration>
                    </execution>
                    <execution>
                        <id>npm-build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <configuration>
                            <arguments>run build</arguments>
                            <skip>${frontend.build.skip}</skip>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            
            <!-- Test Coverage -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.12</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    
    <profiles>
        <profile>
            <id>production</id>
            <properties>
                <spring.profiles.active>production</spring.profiles.active>
            </properties>
        </profile>
        <profile>
            <id>development</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <spring.profiles.active>development</spring.profiles.active>
            </properties>
        </profile>
    </profiles>
</project>
```

### Frontend Build Configuration

#### package.json
```json
{
  "name": "loyalty-admin",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build --outDir ../src/main/resources/static",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "test": "jest",
    "test:coverage": "jest --coverage"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^7.1.1",
    "@mui/material": "^6.3.1",
    "@mui/x-data-grid": "^7.23.3",
    "@mui/x-date-pickers": "^7.23.3",
    "@mui/icons-material": "^6.3.1",
    "@emotion/react": "^11.14.0",
    "@emotion/styled": "^11.14.0",
    "axios": "^1.7.9",
    "dayjs": "^1.11.14"
  },
  "devDependencies": {
    "@types/react": "^18.3.17",
    "@types/react-dom": "^18.3.5",
    "@typescript-eslint/eslint-plugin": "^8.19.1",
    "@typescript-eslint/parser": "^8.19.1",
    "@vitejs/plugin-react": "^4.3.4",
    "eslint": "^9.18.0",
    "eslint-plugin-react-hooks": "5.0.0",
    "eslint-plugin-react-refresh": "^0.4.16",
    "typescript": "~5.8.4",
    "vite": "^6.0.5",
    "jest": "^29.7.0",
    "@testing-library/react": "^16.1.0",
    "@testing-library/jest-dom": "^6.6.3"
  }
}
```

#### Vite Configuration
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html')
      },
      output: {
        manualChunks: {
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-mui-core': ['@mui/material', '@emotion/react', '@emotion/styled'],
          'vendor-mui-x': ['@mui/x-data-grid', '@mui/x-date-pickers'],
          'vendor-mui-icons': ['@mui/icons-material'],
          'vendor-axios': ['axios'],
          'vendor-dayjs': ['dayjs']
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## Testing Specifications

### Test Strategy

#### Unit Testing Requirements
- **Coverage Target**: Minimum 80% line coverage, 70% branch coverage
- **Framework**: JUnit 5 + Mockito + AssertJ
- **Test Naming**: `MethodName_StateUnderTest_ExpectedBehavior`
- **Mock Strategy**: Prefer dependency injection with mock objects

#### Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Testcontainers
class VoucherServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("loyalty_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Test
    void shouldCreateAndAwardVoucher() {
        // Integration test implementation
    }
}
```

#### Performance Testing
```java
@Test
@RepeatedTest(1000)
void ruleEvaluationPerformance() {
    // Performance benchmark
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    
    List<EvaluationResult> results = ruleEvaluationService.evaluateAll(testRecords);
    
    stopWatch.stop();
    assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(100);
}
```

### Quality Gates

#### Code Quality Metrics
- **Cyclomatic Complexity**: Maximum 10 per method
- **Code Duplication**: Maximum 3% duplicated lines
- **Maintainability Index**: Minimum 70
- **Technical Debt**: Maximum 1% debt ratio

#### Security Testing
- **OWASP Dependency Check**: No high/critical vulnerabilities
- **Static Code Analysis**: SonarQube quality gate passed
- **Container Security**: Docker image vulnerability scanning
- **API Security**: OWASP ZAP automated security testing

---

## Appendices

### Appendix A: Configuration Management

#### Application Configuration
```yaml
# application.yaml
spring:
  application:
    name: loyalty-management-system
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:loyalty}
    username: ${DB_USERNAME:loyalty}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:update}
    open-in-view: false
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: loyalty-system
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /
  tomcat:
    max-connections: ${TOMCAT_MAX_CONNECTIONS:8192}
    threads:
      max: ${TOMCAT_MAX_THREADS:200}
      min-spare: ${TOMCAT_MIN_THREADS:10}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    io.github.eendroroy.loyalty: ${LOG_LEVEL:INFO}
    org.springframework.kafka: WARN
    org.hibernate.SQL: ${SQL_LOG_LEVEL:WARN}
    org.hibernate.type.descriptor.sql.BasicBinder: ${SQL_PARAM_LOG_LEVEL:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE:logs/loyalty.log}
```

### Appendix B: Deployment Scripts

#### Build Script
```bash
#!/bin/bash
set -euo pipefail

# Build script for Loyalty Management System
echo "Building Loyalty Management System..."

# Environment setup
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-25-openjdk}
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"

# Clean and validate
echo "Cleaning previous build..."
./mvnw clean

# Run tests with coverage
echo "Running tests..."
./mvnw test jacoco:report

# Build application
echo "Building application..."
./mvnw package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t loyalty/management-system:latest .

echo "Build completed successfully!"
```

#### Deployment Script
```bash
#!/bin/bash
set -euo pipefail

# Deployment script for Kubernetes
echo "Deploying to Kubernetes..."

# Apply ConfigMaps and Secrets
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy database migration job
kubectl apply -f k8s/migration-job.yaml
kubectl wait --for=condition=complete --timeout=600s job/loyalty-migration

# Deploy application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/loyalty-management-system --timeout=300s

echo "Deployment completed successfully!"
```

This technical specification provides comprehensive implementation guidance for the Loyalty Management System, focusing on architecture, performance, security, and operational requirements.

## 4.3 Architecture

### Backend Components

**Core Engine (`src/main/java/io/github/eendroroy/loyalty/rule/`)**
- `RuleParser` — Tokenizes and parses expressions into AST
- `RuleEvaluator` — Evaluates parsed rules against data records
- `RuleEvaluationService` — Orchestrates rule evaluation and reward fulfillment

**AST Model (`rule/ast/`)**
- `ParsedRule` — Complete parsed rule (condition + reward)
- `LogicalNode` — Sealed interface for condition tree nodes
- `AndNode`, `OrNode`, `LeafNode` — Logic operators and conditions
- `Condition` — Single comparison (fieldRef + operator + value)
- `RewardSpec` — Reward type and value

**Integration Points**
- Listens for `DataPulledEvent` (CSV/webhook ingestion)
- Publishes `RuleTriggeredEvent` when rules fire
- Auto-awards voucher instances for `VOUCHER` rewards
- Validates expressions on rule save (throws `RuleParseException`)

### Frontend Integration

**Enhanced Rule Form (`pages/RuleForm.tsx`)**
- Live expression validation with visual feedback
- Syntax guide with examples and operator reference
- Smart field insertion (auto-formats as `source.field`)
- Debounced validation (800ms delay)

**API Integration (`api/rules.ts`)**
- `validateExpression(expression)` — Server-side syntax validation
- Integrated with existing rule CRUD operations

## 4.4 Evaluation Flow

1. **Data Ingestion** → CSV files or webhook payloads are processed
2. **Event Publishing** → `DataPulledEvent(records)` is published
3. **Rule Evaluation** → `RuleEvaluationService` processes all `ACTIVE` rules:
   - Parse each rule expression into AST
   - Evaluate condition tree against each record
   - For matches, fulfill the reward and publish `RuleTriggeredEvent`
4. **Reward Fulfillment**:
   - **Points**: Logged (future member system will credit accounts)
   - **Vouchers**: Auto-award instance with unique 7-char secret code

## 4.5 Key Features

### Type-Aware Evaluation
- Field values are compared using their actual Java types
- Automatic type coercion for string literals in expressions
- Safe fallback for unknown types

### Source Isolation
- Rules only fire for records from the matching data source
- Cross-source AND conditions naturally evaluate to false
- Enables source-specific rule logic

### Error Handling
- Parse errors are caught and logged, rule evaluation continues
- Invalid expressions are rejected at save time with detailed error messages
- Global exception handler converts `RuleParseException` to HTTP 400

### Performance Considerations
- Expression parsing is cached per rule evaluation cycle
- Evaluation short-circuits on AND/OR operators
- Empty record batches are handled gracefully

## 4.6 Usage Examples

### Creating Rules

1. **Via Admin UI**:
   - Navigate to Rules → Add Rule
   - Enter expression using live syntax validation
   - Use field browser to insert `source.field` references
   - System validates syntax before save

2. **Expression Examples**:
   ```sql
   WHEN payments.amount > 100 THEN Point(200)
   WHEN loyalty.tier = "platinum" AND payments.amount > 50 THEN Voucher(PLAT50)
   WHEN (events.type = "signup" OR events.type = "referral") THEN Point(50)
   ```

### Monitoring

- **Rule Status**: Rules must be `ACTIVE` to participate in evaluation
- **Execution Logs**: `lastRunAt` timestamp tracks scheduled evaluations
- **Event Stream**: `RuleTriggeredEvent` provides audit trail of rule fires
- **Validation**: Real-time syntax checking prevents invalid rules

## 4.7 Technical Notes

### Parser Implementation
- **Recursive Descent**: Hand-written parser with clear grammar
- **Precedence**: OR (lowest) → AND → parentheses → conditions (highest)
- **Lexer**: Integrated tokenizer with keyword and operator recognition
- **Error Recovery**: Detailed parse error messages with position context

### Data Model Compatibility
- **Field References**: Must match `DataSourceSchemaField.name` values
- **Source Names**: Must match `DataSource.name` values  
- **Voucher Codes**: Must match existing `Voucher.code` values
- **Type Safety**: Runtime type checking with graceful degradation

### Integration Points
- **Scheduled Rules**: Cron-triggered evaluation (future enhancement)
- **Real-time Rules**: Event-driven evaluation on data ingestion
- **Reward Systems**: Pluggable reward fulfillment (points + vouchers implemented)

---

# 5. Voucher Management System

## 5.1 Overview ✅ **IMPLEMENTED**

Complete voucher lifecycle management with unique secret code generation,
capacity tracking, and rule-triggered auto-awarding capabilities.

## 5.2 Functional Requirements

| ID | Requirement | Status |
|----|-------------|--------|
| VM-01 | The system shall support **complete voucher lifecycle** (create, edit, view, archive, purge) with capacity management. | ✅ **IMPLEMENTED** |
| VM-02 | Each voucher shall have a **unique public code** and configurable maximum instance count. | ✅ **IMPLEMENTED** |
| VM-03 | The system shall generate **unique 7-character alphanumeric secret codes** (A-Z, 0-9) for each awarded instance. | ✅ **IMPLEMENTED** |
| VM-04 | Secret code generation must be **collision-resistant** with retry mechanism and system-wide uniqueness. | ✅ **IMPLEMENTED** |
| VM-05 | The system shall provide **capacity enforcement** preventing over-awarding beyond configured limits. | ✅ **IMPLEMENTED** |
| VM-06 | Vouchers shall support **active/inactive status** with archive management capabilities. | ✅ **IMPLEMENTED** |
| VM-07 | The admin UI shall provide **copy-to-clipboard functionality** for secret codes with visual feedback. | ✅ **IMPLEMENTED** |
| VM-08 | Rule engine integration shall **auto-award voucher instances** when `Voucher(CODE)` rewards trigger. | ✅ **IMPLEMENTED** |

## 5.3 Technical Implementation

### Backend Architecture
- `Voucher` entity with public code, description, type, count, and status
- `VoucherInstance` entity with unique secret codes and creation tracking
- `VoucherService` with award logic, capacity checking, and code generation
- Archive/purge lifecycle following data source patterns

### Frontend Features
- Tabbed interface separating active and archived vouchers
- Comprehensive form with validation and real-time feedback
- Instance tracking with awarded code display and copy functionality
- Award button integration with conflict prevention

### Integration Points
- `RuleEvaluationService` automatically awards instances for `VOUCHER` rewards
- `RuleTriggeredEvent` includes awarded secret codes for audit trails
- Capacity validation prevents rule-triggered over-awarding

---

# 6. Enhanced Data Source Management

## 6.1 Overview ✅ **IMPLEMENTED**

Multi-step wizard interface with schema-first architecture, real-time validation,
and comprehensive field management capabilities.

## 6.2 Functional Requirements

| ID | Requirement | Status |
|----|-------------|--------|
| DS-01 | The system shall provide a **multi-step wizard interface** for creating data sources with name, description, and destination table configuration. | ✅ **IMPLEMENTED** |
| DS-02 | The system shall support **schema-first architecture** where destination table columns are defined as reusable schema fields. | ✅ **IMPLEMENTED** |
| DS-03 | The system shall provide **real-time validation** with immediate feedback during form completion. | ✅ **IMPLEMENTED** |
| DS-04 | The system shall support **atomic transactions** for creating/updating data sources with all nested entities. | ✅ **IMPLEMENTED** |
| DS-05 | The system shall provide **archive management** with soft-delete capabilities and purge options for data/tables. | ✅ **IMPLEMENTED** |
| DS-06 | The system shall generate **comprehensive review summaries** before saving configurations. | ✅ **IMPLEMENTED** |
| DS-07 | The system shall support **tabular field management** with DataGrid interfaces for complex configurations. | ✅ **IMPLEMENTED** |

### Enhanced Webhook Management ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| DSW-01 | Each data source shall have **exactly one webhook configuration** with enable/disable toggle. | ✅ **IMPLEMENTED** |
| DSW-02 | The system shall **auto-generate webhook paths** in format `POST /web-hook?dataSourceName={name}`. | ✅ **IMPLEMENTED** |
| DSW-03 | Webhook properties shall **auto-sync from schema fields**, eliminating duplicate definitions. | ✅ **IMPLEMENTED** |
| DSW-04 | The system shall provide **description support** for documenting webhook purpose and usage. | ✅ **IMPLEMENTED** |
| DSW-05 | The system shall **auto-generate sample request bodies** based on schema field definitions. | ✅ **IMPLEMENTED** |
| DSW-06 | Webhook configuration shall include **visual status indicators** and real-time path updates. | ✅ **IMPLEMENTED** |

### Advanced File Management ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| DSF-01 | The system shall support **comprehensive CSV parsing configuration** including separators, quotes, and line handling. | ✅ **IMPLEMENTED** |
| DSF-02 | File field mappings shall **reference schema fields** with dropdown selection and validation. | ✅ **IMPLEMENTED** |
| DSF-03 | The system shall provide **tabular field mapping interfaces** with column number and name-based matching. | ✅ **IMPLEMENTED** |
| DSF-04 | File configurations shall support **real-time validation** with immediate feedback on mapping errors. | ✅ **IMPLEMENTED** |
| DSF-05 | The system shall maintain **file path uniqueness** across the entire system. | ✅ **IMPLEMENTED** |

---

# 7. Real-time Monitoring & Analytics

## 7.1 System Monitoring ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| MON-01 | The system shall provide **live file watcher status** with processing history and success/failure counts. | ✅ **IMPLEMENTED** |
| MON-02 | The system shall track **webhook endpoint status** with configuration details and activity logs. | ✅ **IMPLEMENTED** |
| MON-03 | The system shall maintain **comprehensive processing logs** with pagination and filtering capabilities. | ✅ **IMPLEMENTED** |
| MON-04 | Rule execution shall generate **audit trails** with trigger events and reward fulfillment tracking. | ✅ **IMPLEMENTED** |

## 7.2 Performance Tracking ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| PERF-01 | The system shall track **processing times** for file ingestion and rule evaluation cycles. | ✅ **IMPLEMENTED** |
| PERF-02 | The system shall maintain **success/failure statistics** with error categorization and recovery tracking. | ✅ **IMPLEMENTED** |
| PERF-03 | Archive operations shall include **usage validation** preventing deletion of actively referenced resources. | ✅ **IMPLEMENTED** |

---

# 8. Non-Functional Requirements

## 8.1 Performance ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| NFR-01 | Rule expression parsing shall be **cached per evaluation cycle** to minimize computational overhead. | ✅ **IMPLEMENTED** |
| NFR-02 | Rule evaluation shall use **short-circuit logic** for AND/OR operations to optimize performance. | ✅ **IMPLEMENTED** |
| NFR-03 | Voucher secret code generation shall complete within **20 attempts** with collision retry mechanism. | ✅ **IMPLEMENTED** |
| NFR-04 | Live validation requests shall be **debounced (800ms)** to prevent API spam during typing. | ✅ **IMPLEMENTED** |

## 8.2 Reliability ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| REL-01 | Rule parsing errors shall be **gracefully handled** without stopping evaluation of other rules. | ✅ **IMPLEMENTED** |
| REL-02 | Invalid expressions shall be **rejected at save time** with detailed error messages for correction. | ✅ **IMPLEMENTED** |
| REL-03 | Voucher capacity limits shall be **strictly enforced** with clear error messages when limits are reached. | ✅ **IMPLEMENTED** |
| REL-04 | System shall maintain **transactional integrity** across all rule evaluation and reward fulfillment operations. | ✅ **IMPLEMENTED** |

## 8.3 Usability ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| UX-01 | Rule editor shall provide **live syntax validation** with visual feedback (success/error indicators). | ✅ **IMPLEMENTED** |
| UX-02 | Field browser shall support **smart insertion** with auto-formatted `source.field` references. | ✅ **IMPLEMENTED** |
| UX-03 | Comprehensive **syntax guide** shall be available with examples and operator reference. | ✅ **IMPLEMENTED** |
| UX-04 | Admin interface shall provide **copy-to-clipboard** functionality for voucher secret codes. | ✅ **IMPLEMENTED** |

---

# 9. System Integration

## 9.1 Event-Driven Architecture ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| INT-01 | Data ingestion shall publish **`DataPulledEvent`** for rule engine consumption. | ✅ **IMPLEMENTED** |
| INT-02 | Rule triggers shall publish **`RuleTriggeredEvent`** for audit trail and downstream processing. | ✅ **IMPLEMENTED** |
| INT-03 | All domain events shall use **`@TransactionalEventListener(AFTER_COMMIT)`** for consistency. | ✅ **IMPLEMENTED** |

## 9.2 Type Safety ✅ **IMPLEMENTED**

| ID | Requirement | Status |
|----|-------------|--------|
| TYPE-01 | Rule evaluation shall perform **automatic type coercion** with safe fallback for unknown types. | ✅ **IMPLEMENTED** |
| TYPE-02 | Frontend shall maintain **full TypeScript integration** with comprehensive error handling. | ✅ **IMPLEMENTED** |
| TYPE-03 | API responses shall use **structured error formats** (ProblemDetail) for consistent error handling. | ✅ **IMPLEMENTED** |

---

# 10. Technical Implementation Requirements

## 10.1 Technology Stack Requirements

### Backend Technology Stack
| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Runtime** | Java | 25+ | Application runtime |
| **Framework** | Spring Boot | 4.0.5 | Web framework & dependency injection |
| **Data Access** | Spring Data JPA | 4.0.4 | Object-relational mapping |
| **Database** | PostgreSQL | 15+ | Primary data storage |
| **Messaging** | Spring Kafka | 4.0.4 | Event streaming (future) |
| **Code Generation** | Lombok | Latest | Boilerplate code reduction |
| **Mapping** | MapStruct | 1.6.3 | Object-to-object mapping |
| **API Documentation** | SpringDoc OpenAPI | 2.8.5 | API documentation generation |
| **Build Tool** | Maven | 3.9+ | Build automation |

### Frontend Technology Stack
| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Framework** | React | 18+ | User interface framework |
| **Language** | TypeScript | 5+ | Type-safe JavaScript |
| **Build Tool** | Vite | 6+ | Frontend build tool |
| **UI Library** | MUI (Material-UI) | v6+ | Component library |
| **Data Grid** | MUI X DataGrid | Latest | Advanced table component |
| **HTTP Client** | Axios | Latest | API communication |
| **Date Library** | Day.js | Latest | Date manipulation |

## 10.2 Project Structure Requirements

### Package Structure
```
src/main/java/io/github/eendroroy/loyalty/
├── config/           # Spring configuration beans
│   ├── CorsConfig           # CORS configuration
│   ├── JacksonConfig        # JSON serialization
│   ├── OpenApiConfig        # API documentation
│   ├── SchedulerConfig      # Task scheduling
│   └── SpaController        # Single Page App routing
├── controller/       # REST endpoints + exception handling
│   ├── DataSourceController
│   ├── RuleController
│   ├── VoucherController
│   ├── MetadataController
│   ├── MonitorController
│   ├── DataSourceTableController
│   ├── WebhookIngestionController
│   └── GlobalExceptionHandler
├── dto/
│   ├── request/      # @Data @Builder @NoArgsConstructor @AllArgsConstructor
│   └── response/     # @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(NON_NULL)
├── entity/           # JPA entities (@Getter @Setter ONLY - never @Data)
│   ├── DataSource, DataSourceFile, DataSourceWebhook
│   ├── Rule, RuleAction
│   ├── Voucher, VoucherInstance
│   └── Base audit entities
├── enums/            # All system enumerations
│   ├── FieldDataType, FileProcessingStatus
│   ├── RewardType, RuleStatus, TriggerType
│   ├── WebhookContentType, VoucherType
│   └── ComparisonOperator
├── event/            # Spring application events
│   ├── DataPulledEvent, RuleTriggeredEvent
│   ├── RuleSavedEvent, RuleDeletedEvent
│   └── DataSourceFileSavedEvent, DataSourceFileDeletedEvent
├── mapper/           # MapStruct interfaces + configuration
├── model/            # Non-JPA value types
│   ├── IngestedRecord, DataPullResult
│   └── EvaluationResult
├── repository/       # Spring Data JPA repositories
├── rule/             # Complete rule language engine
│   ├── ast/          # Abstract syntax tree classes
│   │   ├── ParsedRule, LogicalNode
│   │   ├── AndNode, OrNode, LeafNode
│   │   ├── Condition, RewardSpec
│   │   └── FieldRef
│   ├── exception/    # Rule parsing exceptions
│   ├── RuleParser    # Recursive descent parser with lexer
│   └── RuleEvaluator # Type-aware evaluation engine
├── scheduler/        # Scheduled task management
├── service/          # Business logic interfaces
├── service/impl/     # @Service implementations
└── watcher/          # File system monitoring

frontend/src/
├── api/              # Axios API integration
│   ├── client.ts           # Base HTTP client
│   ├── dataSources.ts      # Data source operations
│   ├── rules.ts            # Rule management + validation
│   ├── vouchers.ts         # Voucher operations
│   ├── metadata.ts         # Field metadata
│   └── monitor.ts          # System monitoring
├── components/       # Shared React components
│   ├── Layout, Sidebar, PageHeader
│   ├── ConfirmDialog, FieldBrowser
│   └── RuleBuilder         # Enhanced Visual Rule Builder
├── pages/            # Route-level components
│   ├── Dashboard, DataSources, DataSourceForm
│   ├── Rules, RuleForm     # With dual-mode editing
│   ├── Vouchers, VoucherForm
│   ├── Monitoring, ImportedData
│   └── ArchivedSources
├── types/            # TypeScript definitions
│   └── index.ts            # All interfaces and enums
└── theme.ts          # MUI dark theme configuration
```

## 10.3 Configuration Requirements

### Database Configuration (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/loyalty
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update          # Auto-create schema from entities
    open-in-view: false         # Disable OSIV - use JOIN FETCH
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  kafka:
    bootstrap-servers: ${KAFKA_HOST:localhost:9092}
    consumer:
      group-id: loyalty-system
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    io.github.eendroroy.loyalty: INFO
    org.springframework.kafka: INFO
```

### Maven Configuration (pom.xml key sections)
```xml
<properties>
    <java.version>25</java.version>
    <spring-boot.version>4.0.5</spring-boot.version>
    <mapstruct.version>1.6.3</mapstruct.version>
</properties>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Code Generation -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-csv</artifactId>
    </dependency>
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        
        <!-- Frontend Build Integration -->
        <plugin>
            <groupId>com.github.eirslett</groupId>
            <artifactId>frontend-maven-plugin</artifactId>
            <version>1.15.1</version>
            <configuration>
                <workingDirectory>frontend</workingDirectory>
                <installDirectory>frontend/node</installDirectory>
            </configuration>
            <executions>
                <execution>
                    <id>install-node-and-npm</id>
                    <goals>
                        <goal>install-node-and-npm</goal>
                    </goals>
                    <configuration>
                        <nodeVersion>v22.14.0</nodeVersion>
                        <npmVersion>10.9.2</npmVersion>
                    </configuration>
                </execution>
                <execution>
                    <id>npm-install</id>
                    <goals>
                        <goal>npm</goal>
                    </goals>
                    <configuration>
                        <arguments>install</arguments>
                    </configuration>
                </execution>
                <execution>
                    <id>npm-build</id>
                    <goals>
                        <goal>npm</goal>
                    </goals>
                    <configuration>
                        <arguments>run build</arguments>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- Annotation Processing -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Frontend Configuration (package.json)
```json
{
  "name": "loyalty-admin",
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build --outDir ../src/main/resources/static",
    "lint": "eslint .",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^7.1.1",
    "@mui/material": "^6.3.1",
    "@mui/x-data-grid": "^7.23.3",
    "@mui/x-date-pickers": "^7.23.3",
    "@mui/icons-material": "^6.3.1",
    "@emotion/react": "^11.14.0",
    "@emotion/styled": "^11.14.0",
    "axios": "^1.7.9",
    "dayjs": "^1.11.14"
  },
  "devDependencies": {
    "@eslint/js": "^9.18.0",
    "@types/react": "^18.3.17",
    "@types/react-dom": "^18.3.5",
    "@vitejs/plugin-react": "^4.3.4",
    "eslint": "^9.18.0",
    "eslint-plugin-react-hooks": "5.0.0",
    "eslint-plugin-react-refresh": "^0.4.16",
    "globals": "^15.14.0",
    "typescript": "~5.8.4",
    "typescript-eslint": "^8.19.1",
    "vite": "^6.0.5"
  }
}
```

## 10.4 Build and Setup Instructions

### Prerequisites
- Java 25+ (OpenJDK recommended)
- Node.js 18+ and npm 8+
- PostgreSQL 15+
- Maven 3.9+

### Initial Setup
```bash
# Clone repository
git clone <repository-url>
cd loyalty

# Database setup
createdb loyalty
psql loyalty < setup.sql  # If schema initialization needed

# Configuration
cp src/main/resources/application.example.yaml src/main/resources/application.yaml
# Edit application.yaml with database credentials and Kafka settings

# Build and run
mvn clean package -DskipTests
mvn spring-boot:run
```

### Development Commands
```bash
# Full build (backend + frontend)
mvn clean package -DskipTests

# Backend only (faster development)
mvn compile -DskipTests -Dfrontend.build.skip=true

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Frontend development (separate terminal)
cd frontend
npm install
npm run dev  # Development server on port 5173
```

### Application URLs
- **Admin Interface**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **API Endpoints**: http://localhost:8080/v3/api-docs

---

# 11. Future Enhancements

## 11.1 Planned Features

| Feature | Priority | Status |
|---------|----------|--------|
| Authentication & Authorization | High | 📋 Planned |
| Advanced Reporting & Analytics | Medium | 📋 Planned |
| Kafka Producer/Consumer Integration | Medium | 📋 Planned |
| Scheduled Data Source Polling | Medium | 📋 Planned |
| HMAC Webhook Signature Verification | Low | 📋 Planned |
| Member Account Integration | Low | 📋 Planned |

---

## Summary

This comprehensive loyalty management system provides a complete solution for:

- **Advanced Rule Engine**: Natural language syntax with live validation and type-aware evaluation
- **Dynamic Voucher Management**: Complete lifecycle with unique code generation and auto-awarding
- **Enhanced Data Sources**: Multi-step configuration with schema-first architecture
- **Real-time Monitoring**: Live status tracking with comprehensive audit trails

All core requirements have been **implemented and tested** ✅, providing a production-ready platform for sophisticated loyalty program management.

---

# 12. Implementation Details and Technical Patterns

## 12.1 Rule Language Engine Implementation

### AST Structure Implementation
```java
// Core AST classes in rule/ast/:
public record ParsedRule(LogicalNode condition, RewardSpec reward) {}

public sealed interface LogicalNode permits AndNode, OrNode, LeafNode {}

public record AndNode(LogicalNode left, LogicalNode right) implements LogicalNode {}
public record OrNode(LogicalNode left, LogicalNode right) implements LogicalNode {}
public record LeafNode(Condition condition) implements LogicalNode {}

public record Condition(FieldRef fieldRef, ComparisonOperator operator, String rawValue) {}
public record FieldRef(String sourceName, String fieldName, FieldDataType dataType) {}
public record RewardSpec(RewardType type, String value) {}
```

### Parser Implementation
```java
@Component
@RequiredArgsConstructor
public class RuleParser {
    
    public ParsedRule parse(String expression) throws RuleParseException {
        // Tokenization and recursive descent parsing
        TokenStream tokens = lexer(expression);
        LogicalNode condition = parseWhenClause(tokens);
        RewardSpec reward = parseThenClause(tokens);
        return new ParsedRule(condition, reward);
    }
    
    private TokenStream lexer(String expression) {
        // Lexical analysis with token generation
    }
    
    private LogicalNode parseWhenClause(TokenStream tokens) {
        // Recursive descent parsing with operator precedence
    }
    
    private RewardSpec parseThenClause(TokenStream tokens) {
        // Reward specification parsing
    }
}
```

### Rule Evaluation Engine
```java
@Component
@RequiredArgsConstructor
public class RuleEvaluator {
    
    public boolean evaluate(LogicalNode condition, IngestedRecord record) {
        return switch (condition) {
            case AndNode and -> evaluate(and.left(), record) && evaluate(and.right(), record);
            case OrNode or -> evaluate(or.left(), record) || evaluate(or.right(), record);
            case LeafNode leaf -> evaluateCondition(leaf.condition(), record);
        };
    }
    
    private boolean evaluateCondition(Condition condition, IngestedRecord record) {
        Object fieldValue = record.getField(condition.fieldRef().fullPath());
        Object coercedValue = coerceType(condition.rawValue(), condition.fieldRef().dataType());
        return condition.operator().apply(fieldValue, coercedValue);
    }
    
    private Object coerceType(String value, FieldDataType targetType) {
        return switch (targetType) {
            case STRING -> value;
            case INTEGER -> Long.parseLong(value);
            case DECIMAL -> new BigDecimal(value);
            case DATE -> LocalDate.parse(value);
            case BOOLEAN -> Boolean.parseBoolean(value);
        };
    }
}
```

## 12.2 Voucher Management Implementation

### Entity Design Pattern
```java
@Entity
@Getter @Setter
@Table(name = "vouchers")
public class Voucher extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String code; // Public template code
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private VoucherType voucherType = VoucherType.DISCOUNT;
    
    @Column(nullable = false)
    private Integer count = 0; // Maximum instances
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Boolean archived = false;
    
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherInstance> instances = new ArrayList<>();
    
    // Business methods
    public boolean canAward() {
        return active && !archived && instances.size() < count;
    }
    
    public int getRemainingCount() {
        return Math.max(0, count - instances.size());
    }
}

@Entity
@Getter @Setter
@Table(name = "voucher_instances")
public class VoucherInstance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;
    
    @Column(name = "secret_code", nullable = false, unique = true, length = 7)
    private String secretCode; // 7-char A-Z0-9
    
    // Validation constraint
    @PrePersist
    @PreUpdate
    private void validateSecretCode() {
        if (secretCode == null || !secretCode.matches("^[A-Z0-9]{7}$")) {
            throw new IllegalStateException("Secret code must be 7 alphanumeric characters");
        }
    }
}
```

### Secret Code Generation Algorithm
```java
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    
    private static final int MAX_GENERATION_ATTEMPTS = 20;
    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final VoucherRepository voucherRepository;
    private final VoucherInstanceRepository voucherInstanceRepository;
    
    @Transactional
    public VoucherInstance award(Long voucherId) {
        Voucher voucher = voucherRepository.findByIdWithInstances(voucherId)
            .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
            
        if (!voucher.canAward()) {
            throw new IllegalStateException("Voucher cannot be awarded: " + 
                (voucher.getRemainingCount() == 0 ? "capacity exceeded" : "inactive"));
        }
        
        VoucherInstance instance = new VoucherInstance();
        instance.setVoucher(voucher);
        instance.setSecretCode(generateUniqueSecretCode());
        
        return voucherInstanceRepository.save(instance);
    }
    
    private String generateUniqueSecretCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = generateRandomCode();
            if (!voucherInstanceRepository.existsBySecretCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Failed to generate unique secret code after " + 
            MAX_GENERATION_ATTEMPTS + " attempts");
    }
    
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(CODE_ALPHABET.length());
            code.append(CODE_ALPHABET.charAt(index));
        }
        return code.toString();
    }
}
```

## 12.3 Frontend Component Architecture

### React Component Patterns
```typescript
// Enhanced Visual Rule Builder Component
interface RuleBuilderProps {
  metadata: Metadata | null;
  initialExpression?: string;
  onExpressionChange: (expression: string) => void;
}

const RuleBuilder: React.FC<RuleBuilderProps> = ({ 
  metadata, 
  initialExpression, 
  onExpressionChange 
}) => {
  const [conditionGroups, setConditionGroups] = useState<ConditionGroup[]>([
    { id: '1', conditions: [{ id: '1', property: '', operator: '', value: '' }], logic: 'AND' }
  ]);
  const [reward, setReward] = useState<Reward>({ type: 'POINT', value: '' });
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  
  // Load active vouchers for dropdown
  useEffect(() => {
    getVouchers().then(response => 
      setVouchers(response.data.filter(v => v.active && !v.archived))
    );
  }, []);
  
  // Generate expression from visual components
  const generateExpression = useCallback((): string => {
    if (conditionGroups.length === 0 || !reward.type || !reward.value) return '';
    
    const whenClauses = conditionGroups
      .filter(group => group.conditions.some(c => c.property && c.operator && c.value))
      .map(group => {
        const groupConditions = group.conditions
          .filter(c => c.property && c.operator && c.value)
          .map(c => `${c.property} ${c.operator} ${formatValue(c.value, getFieldType(c.property))}`);
        
        if (groupConditions.length === 0) return '';
        if (groupConditions.length === 1) return groupConditions[0];
        
        return `(${groupConditions.join(` ${group.logic} `)})`;
      })
      .filter(Boolean);
    
    if (whenClauses.length === 0) return '';
    
    const whenClause = whenClauses.length === 1 ? whenClauses[0] : whenClauses.join(' OR ');
    const thenClause = reward.type === 'POINT' ? `Point(${reward.value})` : `Voucher(${reward.value})`;
    
    return `WHEN ${whenClause} THEN ${thenClause}`;
  }, [conditionGroups, reward, metadata]);
  
  // Update expression when components change
  useEffect(() => {
    const expression = generateExpression();
    onExpressionChange(expression);
  }, [generateExpression, onExpressionChange]);
  
  // Component rendering with structured WHEN/THEN sections
  return (
    <Box>
      {/* Rule Expression Preview */}
      <Card sx={{ mb: 3, p: 2, bgcolor: 'primary.50' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
            Generated Rule Expression
          </Typography>
          <IconButton onClick={() => handleCopyExpression(generateExpression())}>
            <ContentCopyIcon fontSize="small" />
          </IconButton>
        </Box>
        <Typography 
          variant="body1" 
          sx={{ 
            fontFamily: 'monospace', 
            mt: 1, 
            p: 1, 
            bgcolor: 'background.paper',
            border: 1,
            borderColor: 'divider',
            borderRadius: 1,
            minHeight: '2em',
            display: 'flex',
            alignItems: 'center'
          }}
        >
          {generateExpression() || 'Configure conditions and reward to see rule expression...'}
        </Typography>
      </Card>
      
      {/* WHEN Section */}
      <Card sx={{ mb: 3 }}>
        <CardHeader 
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Chip label="WHEN" color="primary" size="small" />
              <Typography variant="h6">Conditions</Typography>
            </Box>
          }
          sx={{ pb: 1 }}
        />
        <CardContent>
          {/* Condition Groups */}
          {conditionGroups.map((group, groupIndex) => (
            <ConditionGroup
              key={group.id}
              group={group}
              groupIndex={groupIndex}
              metadata={metadata}
              onUpdateGroup={handleUpdateGroup}
              onRemoveGroup={handleRemoveGroup}
              showGroupConnector={groupIndex < conditionGroups.length - 1}
            />
          ))}
          
          <Button
            variant="outlined"
            startIcon={<AddIcon />}
            onClick={handleAddGroup}
            sx={{ mt: 2 }}
          >
            Add Condition Group
          </Button>
        </CardContent>
      </Card>
      
      {/* THEN Section */}
      <Card>
        <CardHeader 
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Chip label="THEN" color="secondary" size="small" />
              <Typography variant="h6">Reward</Typography>
            </Box>
          }
          sx={{ pb: 1 }}
        />
        <CardContent>
          <RewardBuilder
            reward={reward}
            vouchers={vouchers}
            onRewardChange={setReward}
          />
        </CardContent>
      </Card>
    </Box>
  );
};
```

### Dual-Mode Rule Form Integration
```typescript
const RuleForm: React.FC = () => {
  const [ruleEditorMode, setRuleEditorMode] = useState<'text' | 'visual'>('text');
  const [form, setForm] = useState(EMPTY_RULE);
  const [metadata, setMetadata] = useState<Metadata | null>(null);
  const [validating, setValidating] = useState(false);
  const [validationResult, setValidationResult] = useState<ValidationResult | null>(null);
  
  // Load metadata for field references
  useEffect(() => {
    getMetadata().then(response => setMetadata(response.data));
  }, []);
  
  // Live expression validation with debounce
  useEffect(() => {
    const timer = setTimeout(() => {
      if (form.ruleExpression?.trim()) {
        setValidating(true);
        validateExpression(form.ruleExpression)
          .then(response => setValidationResult(response.data))
          .catch(() => setValidationResult({ valid: false, error: 'Validation failed' }))
          .finally(() => setValidating(false));
      } else {
        setValidationResult(null);
      }
    }, 800); // 800ms debounce
    
    return () => clearTimeout(timer);
  }, [form.ruleExpression]);
  
  const handleExpressionChange = (expression: string) => {
    setForm(prev => ({ ...prev, ruleExpression: expression }));
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* Rule Editor Mode Toggle */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
          Rule Expression *
        </Typography>
        <ToggleButtonGroup
          value={ruleEditorMode}
          exclusive
          onChange={(_, newMode) => newMode && setRuleEditorMode(newMode)}
          size="small"
        >
          <ToggleButton value="text">
            <CodeRoundedIcon fontSize="small" sx={{ mr: 1 }} />
            Text Editor
          </ToggleButton>
          <ToggleButton value="visual">
            <BuildRoundedIcon fontSize="small" sx={{ mr: 1 }} />
            Visual Builder
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>
      
      {/* Conditional Rendering Based on Mode */}
      {ruleEditorMode === 'text' ? (
        <>
          <TextField
            fullWidth
            multiline
            rows={6}
            required
            label="Rule Expression"
            value={form.ruleExpression}
            onChange={(e) => handleExpressionChange(e.target.value)}
            error={validationResult?.valid === false}
            helperText={getValidationHelperText()}
            inputProps={{
              style: { fontFamily: 'monospace', fontSize: '0.85rem' }
            }}
            sx={{
              '& .MuiInputBase-root': {
                backgroundColor: getValidationBackgroundColor()
              }
            }}
          />
          <SyntaxGuide />
          <FieldBrowser metadata={metadata} onInsert={handleInsertField} />
        </>
      ) : (
        <RuleBuilder
          metadata={metadata}
          initialExpression={form.ruleExpression}
          onExpressionChange={handleExpressionChange}
        />
      )}
      
      {/* Validation Status for Visual Mode */}
      {ruleEditorMode === 'visual' && (
        <ValidationStatus 
          validating={validating} 
          validationResult={validationResult} 
        />
      )}
    </form>
  );
};
```

## 12.4 Event-Driven Processing Implementation

### Event Publishing Pattern
```java
@Service
@RequiredArgsConstructor
@Transactional
public class RuleEvaluationServiceImpl implements RuleEvaluationService {
    
    private final RuleRepository ruleRepository;
    private final RuleEvaluator ruleEvaluator;
    private final VoucherService voucherService;
    private final ApplicationEventPublisher eventPublisher;
    
    @EventListener
    @Async("ruleEvaluationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataPulled(DataPulledEvent event) {
        List<Rule> activeRules = ruleRepository.findByStatus(RuleStatus.ACTIVE);
        
        for (IngestedRecord record : event.records()) {
            for (Rule rule : activeRules) {
                try {
                    evaluateRuleForRecord(rule, record);
                } catch (Exception ex) {
                    log.error("Rule evaluation failed for rule {} and record {}", 
                        rule.getId(), record.getTransactionId(), ex);
                }
            }
        }
    }
    
    private void evaluateRuleForRecord(Rule rule, IngestedRecord record) {
        try {
            ParsedRule parsedRule = ruleParser.parse(rule.getRuleExpression());
            boolean matches = ruleEvaluator.evaluate(parsedRule.condition(), record);
            
            if (matches) {
                fulfillReward(rule, parsedRule.reward(), record);
                publishRuleTriggered(rule, record, parsedRule.reward());
            }
        } catch (RuleParseException ex) {
            log.warn("Invalid rule expression for rule {}: {}", rule.getId(), ex.getMessage());
        }
    }
    
    private void fulfillReward(Rule rule, RewardSpec reward, IngestedRecord record) {
        switch (reward.type()) {
            case POINT -> {
                int points = Integer.parseInt(reward.value());
                // Award points (implementation depends on customer service)
                log.info("Awarded {} points for rule {} to customer {}", 
                    points, rule.getId(), record.getCustomerId());
            }
            case VOUCHER -> {
                Optional<Voucher> voucher = voucherService.findByCode(reward.value());
                if (voucher.isPresent()) {
                    VoucherInstance instance = voucherService.award(voucher.get().getId());
                    log.info("Awarded voucher {} (secret: {}) for rule {} to customer {}", 
                        voucher.get().getCode(), instance.getSecretCode(), 
                        rule.getId(), record.getCustomerId());
                }
            }
        }
    }
    
    private void publishRuleTriggered(Rule rule, IngestedRecord record, RewardSpec reward) {
        RuleTriggeredEvent event = new RuleTriggeredEvent(
            rule.getId(),
            rule.getName(),
            record,
            reward.type(),
            reward.value(),
            Instant.now(),
            record.getCorrelationId()
        );
        eventPublisher.publishEvent(event);
    }
}
```

### Database Optimization Patterns
```sql
-- Performance indexes for rule evaluation
CREATE INDEX CONCURRENTLY idx_rules_status_priority 
    ON rules(status, priority DESC) 
    WHERE status = 'ACTIVE';

CREATE INDEX CONCURRENTLY idx_rules_active_date_range 
    ON rules(active_from, active_to) 
    WHERE active_from IS NOT NULL OR active_to IS NOT NULL;

-- Voucher capacity tracking optimization
CREATE INDEX CONCURRENTLY idx_voucher_instances_voucher_id_created 
    ON voucher_instances(voucher_id, created_at DESC);

-- Unique constraint with partial index for better performance
CREATE UNIQUE INDEX CONCURRENTLY idx_voucher_instances_secret_code_unique 
    ON voucher_instances(secret_code) 
    WHERE secret_code IS NOT NULL;

-- Function for real-time capacity checking
CREATE OR REPLACE FUNCTION check_voucher_capacity()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM 1 FROM vouchers v 
    WHERE v.id = NEW.voucher_id 
      AND v.active = true 
      AND v.archived = false
      AND (
          SELECT COUNT(*) 
          FROM voucher_instances vi 
          WHERE vi.voucher_id = NEW.voucher_id
      ) < v.count;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Voucher capacity exceeded or voucher not available';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_voucher_capacity
    BEFORE INSERT ON voucher_instances
    FOR EACH ROW EXECUTE FUNCTION check_voucher_capacity();
```

