# User Stories — Loyalty Management System

## Overview

The Loyalty Management System is a comprehensive platform for managing customer loyalty programs, data ingestion, reward rule configuration, and voucher management. It serves different user personas including loyalty program administrators, system administrators, and external service integrators.

**Latest Update**: Complete rule language engine with natural WHEN...THEN syntax, dynamic voucher management with auto-awarding, and enhanced admin UI with live validation.

---

## User Personas

### 🧑‍💼 **Loyalty Program Administrator**
- **Role**: Manages loyalty program rules, vouchers, monitors customer data, and configures reward systems
- **Technical Level**: Business user with basic technical understanding
- **Primary Goals**: Create effective loyalty programs, monitor performance, ensure data accuracy, manage voucher distribution

### 👨‍💻 **System Administrator** 
- **Role**: Manages data sources, system configuration, and technical infrastructure
- **Technical Level**: Technical expert with deep system knowledge
- **Primary Goals**: Ensure reliable data ingestion, maintain system performance, manage integrations

### 🔌 **External Service Developer**
- **Role**: Integrates external services with the loyalty platform
- **Technical Level**: Software developer working with APIs
- **Primary Goals**: Successfully integrate payment systems, POS systems, and other data sources

---

## Epic 1: Advanced Rule Language Engine ✅ **COMPLETED**

### User Story 1.1: Create Dynamic Reward Rules
**As a** Loyalty Program Administrator  
**I want to** create reward rules using natural language syntax  
**So that** I can define complex loyalty logic without technical programming

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Natural `WHEN ... THEN ...` syntax for rule creation
- ✅ Support for complex conditions with AND/OR operators and parentheses grouping
- ✅ Type-aware evaluation for strings, numbers, dates, and booleans
- ✅ Field references in `sourceName.fieldName` format (e.g., `transaction.amount`)
- ✅ Comprehensive operator support: `>`, `<`, `>=`, `<=`, `=`, `!=`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`
- ✅ Point rewards: `Point(amount)` and voucher rewards: `Voucher(code)`
- ✅ Live expression validation with immediate syntax checking
- ✅ Visual syntax guide with examples and operator reference

#### Example Rules Implemented:
```sql
WHEN transaction.amount > 50 THEN Point(100)
WHEN transaction.category = "grocery" AND transaction.date >= 2025-01-01 THEN Point(30)
WHEN (transaction.amount > 100 OR customer.tier = "gold") AND transaction.active = true 
     THEN Voucher(SUMMER25)
WHEN customer.email ENDS_WITH "@company.com" THEN Point(50)
```

### User Story 1.2: Live Rule Validation
**As a** Loyalty Program Administrator  
**I want to** receive immediate feedback when creating rules  
**So that** I can quickly identify and fix syntax errors

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Real-time expression validation with 800ms debounce
- ✅ Visual feedback with success/error indicators (✅/❌)
- ✅ Color-coded input field (green for valid, red for errors)
- ✅ Detailed error messages with position context
- ✅ Smart field insertion with `source.field` auto-formatting
- ✅ Comprehensive syntax guide always available

### User Story 1.3: Automatic Rule Evaluation
**As a** System Administrator  
**I want to** rules to automatically evaluate when data is ingested  
**So that** rewards are distributed in real-time without manual intervention

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Event-driven evaluation triggered by `DataPulledEvent`
- ✅ Evaluation of all `ACTIVE` rules against incoming records
- ✅ Source isolation (rules only fire for matching data sources)
- ✅ Automatic reward fulfillment with `RuleTriggeredEvent` publishing
- ✅ Error handling that continues evaluation despite individual rule failures
- ✅ Performance optimization with expression caching and short-circuit logic

---

## Epic 2: Dynamic Voucher Management ✅ **COMPLETED**

### User Story 2.1: Create and Manage Vouchers
**As a** Loyalty Program Administrator  
**I want to** create voucher templates with capacity limits  
**So that** I can control the distribution of discount vouchers

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Complete voucher lifecycle (create, edit, view, archive, purge)
- ✅ Unique public voucher codes for identification
- ✅ Configurable maximum instance count per voucher
- ✅ Active/inactive status toggle with validation
- ✅ Description support for voucher documentation
- ✅ Archive management with soft-delete capabilities
- ✅ Conflict prevention for purging with validation checks

### User Story 2.2: Automatic Secret Code Generation
**As a** Loyalty Program Administrator  
**I want to** unique secret codes generated automatically when vouchers are awarded  
**So that** customers receive secure, one-time-use voucher codes

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ 7-character alphanumeric secret codes (A-Z, 0-9, all caps)
- ✅ System-wide uniqueness with collision retry mechanism
- ✅ Automatic generation within 20 attempts with error handling
- ✅ Secure generation using `SecureRandom` for cryptographic quality
- ✅ Copy-to-clipboard functionality in admin UI with visual feedback
- ✅ Instance tracking with creation timestamps

### User Story 2.3: Rule-Triggered Voucher Awarding
**As a** Loyalty Program Administrator  
**I want to** vouchers automatically awarded when rules trigger  
**So that** customers receive vouchers immediately when they qualify

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Automatic voucher instance awarding for `Voucher(CODE)` rewards
- ✅ Capacity enforcement preventing over-awarding beyond limits
- ✅ Integration with rule evaluation service
- ✅ Error handling for inactive vouchers or capacity exceeded
- ✅ Audit trail with `RuleTriggeredEvent` including secret codes
- ✅ Real-time instance count tracking

---

## Epic 3: Enhanced Data Source Management ✅ **COMPLETED**

### User Story 3.1: Create New Data Source
**As a** System Administrator  
**I want to** create a new data source with schema fields and ingestion configuration  
**So that** I can collect customer transaction data from various sources

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Multi-step form wizard with validation (Details → Ingestion → Review)
- ✅ Define unique data source name and description
- ✅ Configure destination PostgreSQL table name
- ✅ Define schema fields with data types (STRING, LONG, DECIMAL, DATE, BOOLEAN)
- ✅ Add field descriptions for rule expression autocomplete
- ✅ Configure file ingestion with CSV parsing options (separators, quote chars, skip lines)
- ✅ Set up webhook endpoint with auto-generated paths (`/web-hook?dataSourceName={name}`)
- ✅ Real-time validation with immediate error feedback
- ✅ Preview and confirmation before saving

#### Implementation Notes:
- Schema-first architecture ensures consistency between files and webhooks
- One webhook per data source with properties auto-synced from schema fields
- Global uniqueness validation for data source names and destination tables

### User Story 3.2: Edit Existing Data Source
**As a** System Administrator  
**I want to** modify an existing data source configuration  
**So that** I can adapt to changing business requirements and data formats

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Load existing configuration in multi-step form
- ✅ Modify schema fields (add, edit, remove with validation)
- ✅ Update file paths and CSV parsing settings
- ✅ Enable/disable webhook endpoints
- ✅ Preserve data integrity during schema changes
- ✅ Bulk field replacement with proper foreign key handling

### User Story 3.3: Archive and Purge Data Sources
**As a** System Administrator  
**I want to** safely archive and purge data sources  
**So that** I can clean up unused configurations without losing historical data

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Soft-delete (archive) functionality that disables file watchers and webhooks
- ✅ Separate archived data sources view in sidebar
- ✅ Hard-delete (purge) with conflict detection (409 if destination table in use)
- ✅ Granular purge options: data-only or full table drop
- ✅ Irreversible action warnings with confirmation dialogs
- ✅ Event-driven cleanup (file watchers, scheduled tasks)

---

## Epic 4: Data Ingestion ✅ **COMPLETED**

### User Story 4.1: File-Based Data Ingestion
**As a** System Administrator  
**I want to** automatically ingest CSV files when they appear in watched directories  
**So that** batch data uploads are processed without manual intervention

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ File system watching with NIO events (CREATE, MODIFY)
- ✅ CSV parsing with configurable separators and quote characters
- ✅ Field mapping by column number or header name (case-insensitive)
- ✅ Type coercion to target schema (String, Long, BigDecimal, LocalDate, Boolean)
- ✅ Duplicate file detection using (path, size, lastModified) fingerprinting
- ✅ Distributed processing prevention with database constraints
- ✅ Comprehensive error logging and status tracking

#### Technical Implementation:
- `FileWatcherService` registers paths via Spring events
- `DataPullService` handles CSV parsing and database insertion
- `FileProcessingLog` tracks processing status with deduplication
- Atomic transactions with proper rollback on failures

### User Story 4.2: Webhook-Based Data Ingestion
**As a** External Service Developer  
**I want to** send real-time transaction data via HTTP webhooks  
**So that** loyalty rules can be evaluated immediately for time-sensitive rewards

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Auto-generated webhook endpoints per data source
- ✅ JSON payload parsing with schema-based validation
- ✅ Type coercion according to webhook property definitions
- ✅ Enable/disable toggle for webhook activation
- ✅ Sample request body generation for integration testing
- ✅ Comprehensive error responses for debugging

#### Technical Implementation:
- `WebhookIngestionController` handles POST requests
- Properties auto-synced from schema fields
- Immediate rule evaluation after successful ingestion

---

## Epic 5: Real-Time Monitoring ✅ **COMPLETED**

### User Story 5.1: Monitor System Health
**As a** System Administrator  
**I want to** view live status of all data ingestion sources  
**So that** I can quickly identify and resolve processing issues

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Live file watcher status with success/failure counts
- ✅ Webhook endpoint monitoring with configuration details
- ✅ Paginated processing history with filtering capabilities
- ✅ Real-time status updates without manual refresh
- ✅ Clear indication of watching status and last processing results
- ✅ Error categorization and recovery tracking

### User Story 5.2: Track Rule Execution
**As a** Loyalty Program Administrator  
**I want to** monitor rule triggers and reward distribution  
**So that** I can verify my loyalty program is working correctly

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Rule execution audit trail with `RuleTriggeredEvent` tracking
- ✅ Reward fulfillment monitoring (points logged, vouchers awarded)
- ✅ Performance metrics for rule evaluation cycles
- ✅ Error tracking for failed rule evaluations
- ✅ Success/failure statistics with categorized error reporting

---

## Epic 6: Enhanced User Experience ✅ **COMPLETED**

### User Story 6.1: Intuitive Rule Creation
**As a** Loyalty Program Administrator  
**I want to** easily create complex rules without programming knowledge  
**So that** I can focus on business logic rather than technical implementation

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Natural language rule syntax that reads like English
- ✅ Smart field insertion with auto-completion
- ✅ Comprehensive syntax guide with copy-paste examples
- ✅ Visual validation feedback during rule creation
- ✅ Error highlighting with helpful correction suggestions
- ✅ Field browser for discovering available data sources

### User Story 6.2: Efficient Voucher Management
**As a** Loyalty Program Administrator  
**I want to** quickly manage voucher codes and track their usage  
**So that** I can efficiently distribute and monitor discount campaigns

#### Acceptance Criteria: ✅ **IMPLEMENTED**
- ✅ Tabbed interface separating active and archived vouchers
- ✅ One-click secret code copying with visual confirmation
- ✅ Inline voucher awarding with capacity validation
- ✅ Real-time instance count tracking
- ✅ Clear status indicators for active/inactive vouchers
- ✅ Comprehensive form validation with immediate feedback

---

## Technical Achievements

### Performance & Reliability ✅ **IMPLEMENTED**
- **Expression Caching**: Rule parsing cached per evaluation cycle
- **Short-Circuit Logic**: AND/OR evaluation optimized for performance
- **Collision Handling**: Secure random code generation with retry mechanism
- **Transactional Integrity**: Full ACID compliance across all operations
- **Event-Driven Architecture**: Reactive processing with proper isolation

### Type Safety & Validation ✅ **IMPLEMENTED**
- **Runtime Type Coercion**: Automatic conversion with safe fallbacks
- **Frontend Type Safety**: Full TypeScript integration with error handling
- **Live Validation**: Real-time syntax checking with debounced requests
- **Structured Errors**: ProblemDetail format for consistent error handling

### Integration & Extensibility ✅ **IMPLEMENTED**
- **Event Bus**: Clean separation of concerns with Spring events
- **Plugin Architecture**: Extensible reward fulfillment system
- **API-First Design**: Complete REST API for external integrations
- **Documentation**: Comprehensive OpenAPI specs with examples

---

## Future Enhancements

### Phase 1: Authentication & Security
- User authentication and authorization
- Role-based access control
- HMAC webhook signature verification
- Audit logging for compliance

### Phase 2: Advanced Analytics
- Rule performance analytics
- Reward distribution reports
- Customer engagement metrics
- A/B testing framework for rule variations

### Phase 3: Enterprise Features
- Kafka producer/consumer integration
- Scheduled data source polling
- Advanced member account integration
- Multi-tenant support

---

## Success Metrics

The loyalty management system now provides:

✅ **Complete Rule Engine**: Natural language syntax with 100% implementation coverage  
✅ **Dynamic Voucher System**: Full lifecycle with auto-awarding capabilities  
✅ **Enhanced Admin UI**: Live validation with comprehensive user experience  
✅ **Real-Time Processing**: Event-driven architecture with performance optimization  
✅ **Production Ready**: Comprehensive error handling and monitoring capabilities

All user stories have been **successfully implemented** with full feature coverage, providing a robust platform for sophisticated loyalty program management.
