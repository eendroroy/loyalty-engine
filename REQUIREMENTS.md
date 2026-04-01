# Requirements Document — Loyalty Management System

> Version: 0.2.0  
> Date: 2026-03-31  
> Author: eendroroy  
> Status: **COMPLETE** ✅

---

## 1. Purpose

This document defines the functional and non-functional requirements for the
**Loyalty Management System** backend. The system provides a comprehensive REST API 
consumed by an enhanced React admin panel to configure ingestion sources, define 
dynamic reward rules with natural language syntax, manage voucher distribution,
and monitor real-time system performance.

**Latest Implementation**: Complete rule language engine with live validation,
dynamic voucher management system, and enhanced admin UI experience.

---

## 2. Scope

| In Scope | Out of Scope |
|---|---|
| ✅ **Advanced Rule Language Engine** with natural WHEN...THEN syntax | End-user-facing customer API |
| ✅ **Dynamic Voucher Management** with secret code generation | Payment gateway integration |
| ✅ **Live Expression Validation** with syntax guide and smart editor | Customer account management |
| ✅ **Type-Aware Rule Evaluation** with automatic type coercion | Reporting / analytics dashboards |
| ✅ **Event-Driven Reward Fulfillment** with audit trails | Authentication / authorisation (future) |
| ✅ **Enhanced Data Source Management** with multi-step wizard | Kafka consumer logic (future) |
| ✅ **Schema-First Architecture** with auto-synced webhook properties | Data archival / GDPR deletion |
| ✅ **Real-time Monitoring** with live status tracking | SCHEDULED cron-based polling (future) |

---

## 3. Stakeholders

| Role | Responsibility |
|---|---|
| Loyalty Admin | Creates reward rules, manages vouchers, monitors system performance |
| System Administrator | Configures data sources, manages integrations, ensures data quality |
| Developer | Maintains rule engine, extends system capabilities, handles integrations |
| DevOps | Manages PostgreSQL, Kafka, and deployment infrastructure |

---

# 4. Rule Language System — Complete Implementation

## 4.1 Overview

The loyalty management system includes a complete rule language engine that allows administrators to define dynamic reward rules using a natural `WHEN ... THEN ...` syntax. Rules are automatically evaluated against incoming data records and can award points or vouchers when conditions are met.

## 4.2 Rule Language Syntax

### Basic Structure
```
WHEN <conditions> THEN <reward>
```

### Conditions
- **Field References**: `sourceName.fieldName` (e.g., `transaction.amount`)
- **Comparison Operators**: 
  - Numeric/Date: `>`, `<`, `>=`, `<=`, `=`, `!=`
  - String: `=`, `!=`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`
  - Boolean: `=`, `!=`
- **Logical Operators**: `AND`, `OR`
- **Grouping**: Parentheses `()` for complex expressions

### Data Types
- **Numbers**: `50`, `99.99`, `-10`
- **Dates**: `2025-12-31` (YYYY-MM-DD format)
- **Strings**: `"grocery"`, `'premium'`
- **Booleans**: `true`, `false`

### Rewards
- **Points**: `Point(30)` — awards 30 loyalty points
- **Vouchers**: `Voucher(SUMMER25)` — awards an instance of voucher with code "SUMMER25"

### Example Rules

```sql
-- Simple point reward for high-value transactions
WHEN transaction.amount > 50 THEN Point(100)

-- Category-based rewards with date range
WHEN transaction.category = "grocery" AND transaction.date >= 2025-01-01 THEN Point(30)

-- Complex logic with grouping
WHEN (transaction.amount > 100 OR customer.tier = "gold") AND transaction.active = true 
     THEN Voucher(VIP2025)

-- String matching
WHEN customer.email ENDS_WITH "@company.com" THEN Point(50)

-- Boolean conditions
WHEN transaction.verified = true AND transaction.amount > 20 THEN Point(25)
```

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

# 10. Future Enhancements

## 10.1 Planned Features

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
