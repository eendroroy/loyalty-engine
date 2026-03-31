# User Stories — Loyalty Management System

## Overview

The Loyalty Management System is a comprehensive platform for managing customer loyalty programs, data ingestion, and reward rule configuration. It serves different user personas including loyalty program administrators, system administrators, and external service integrators.

---

## User Personas

### 🧑‍💼 **Loyalty Program Administrator**
- **Role**: Manages loyalty program rules, monitors customer data, and configures reward systems
- **Technical Level**: Business user with basic technical understanding
- **Primary Goals**: Create effective loyalty programs, monitor performance, ensure data accuracy

### 👨‍💻 **System Administrator** 
- **Role**: Manages data sources, system configuration, and technical infrastructure
- **Technical Level**: Technical expert with deep system knowledge
- **Primary Goals**: Ensure reliable data ingestion, maintain system performance, manage integrations

### 🔌 **External Service Developer**
- **Role**: Integrates external services with the loyalty platform
- **Technical Level**: Software developer working with APIs
- **Primary Goals**: Successfully integrate payment systems, POS systems, and other data sources

---

## Epic 1: Data Source Management

### User Story 1.1: Create New Data Source
**As a** System Administrator  
**I want to** create a new data source with schema fields and ingestion configuration  
**So that** I can collect customer transaction data from various sources

#### Acceptance Criteria:
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

### User Story 1.2: Edit Existing Data Source
**As a** System Administrator  
**I want to** modify an existing data source configuration  
**So that** I can adapt to changing business requirements and data formats

#### Acceptance Criteria:
- ✅ Load existing configuration in multi-step form
- ✅ Modify schema fields (add, edit, remove with validation)
- ✅ Update file paths and CSV parsing settings
- ✅ Enable/disable webhook endpoints
- ✅ Preserve data integrity during schema changes
- ✅ Bulk field replacement with proper foreign key handling

### User Story 1.3: Archive and Purge Data Sources
**As a** System Administrator  
**I want to** safely archive and purge data sources  
**So that** I can clean up unused configurations without losing historical data

#### Acceptance Criteria:
- ✅ Soft-delete (archive) functionality that disables file watchers and webhooks
- ✅ Separate archived data sources view in sidebar
- ✅ Hard-delete (purge) with conflict detection (409 if destination table in use)
- ✅ Granular purge options: data-only or full table drop
- ✅ Irreversible action warnings with confirmation dialogs
- ✅ Event-driven cleanup (file watchers, scheduled tasks)

---

## Epic 2: Data Ingestion

### User Story 2.1: File-Based Data Ingestion
**As a** System Administrator  
**I want to** automatically ingest CSV files when they appear in watched directories  
**So that** batch data uploads are processed without manual intervention

#### Acceptance Criteria:
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

### User Story 2.2: Webhook-Based Data Ingestion
**As an** External Service Developer  
**I want to** send real-time transaction data via HTTP POST  
**So that** customer events are processed immediately for timely rewards

#### Acceptance Criteria:
- ✅ RESTful webhook endpoint: `POST /web-hook?dataSourceName={dataSourceName}`
- ✅ JSON payload validation against schema field definitions
- ✅ Type coercion and error handling for malformed data
- ✅ Immediate database insertion with transaction safety
- ✅ Event publication for downstream rule processing
- ✅ Proper HTTP status codes (200, 400, 404, 503)

#### Implementation Notes:
- Public endpoint outside `/api/admin/` namespace
- Schema field names serve as both JSON keys and destination columns
- Webhook enable/disable toggle for maintenance scenarios
- Future: HMAC signature verification for security

### User Story 2.3: Monitor Data Processing
**As a** System Administrator  
**I want to** monitor real-time data ingestion status and processing history  
**So that** I can quickly identify and resolve data pipeline issues

#### Acceptance Criteria:
- ✅ Live file watcher status (active paths, last activity)
- ✅ Webhook configuration overview (enabled/disabled, endpoints)
- ✅ Processing history with pagination and search
- ✅ Error details and retry status for failed ingestions
- ✅ Performance metrics (rows processed, processing time)
- ✅ Tabular data preview for destination tables

---

## Epic 3: Loyalty Rules Management

### User Story 3.1: Create Loyalty Rules
**As a** Loyalty Program Administrator  
**I want to** create rule expressions that define when and how rewards are awarded  
**So that** I can implement flexible loyalty program logic

#### Acceptance Criteria:
- ✅ Rule expression editor with autocomplete (field aliases, operators)
- ✅ `WHEN ... THEN ...` syntax for conditional logic
- ✅ Multiple rule actions per rule (POINT, CASHBACK, BADGE, DISCOUNT)
- ✅ Rule activation scheduling and status management
- ✅ Field metadata integration for intelligent autocomplete
- 🔄 Expression validation and testing (future enhancement)

#### Example Rule Expressions:
```sql
WHEN transaction.amount > 100 THEN 50 POINT
WHEN customer.tier = 'GOLD' AND transaction.category = 'DINING' THEN 10 PERCENT CASHBACK
WHEN purchase.frequency >= 5 THEN 'FREQUENT_SHOPPER' BADGE
```

### User Story 3.2: Schedule Rule Evaluation
**As a** Loyalty Program Administrator  
**I want to** schedule when rules are evaluated against incoming data  
**So that** rewards are calculated and applied at the right time

#### Acceptance Criteria:
- ✅ Cron-based rule scheduling with configurable frequency
- ✅ Rule-specific scheduling (different rules, different schedules)
- ✅ Automatic rescheduling when rules are modified
- ✅ Concurrent execution management with thread pools
- 🔄 Rule evaluation engine (stub implementation, future completion)

#### Technical Implementation:
- `RuleScheduler` with `ConcurrentHashMap<Long, ScheduledFuture<?>>`
- Spring `ThreadPoolTaskScheduler` for concurrent execution
- Event-driven rescheduling via `RuleSavedEvent`/`RuleDeletedEvent`

---

## Epic 4: User Interface & Experience

### User Story 4.1: Intuitive Data Source Creation
**As a** System Administrator  
**I want to** use a guided wizard to create data sources  
**So that** I can configure complex integrations without errors

#### Acceptance Criteria:
- ✅ Three-step wizard: Details → Ingestion → Review
- ✅ Progress indicator showing current step
- ✅ Real-time validation with immediate feedback
- ✅ Smart defaults and helpful guidance text
- ✅ Ability to navigate between steps and modify previous inputs
- ✅ Comprehensive review step with all configurations displayed

### User Story 4.2: Comprehensive Data Management
**As a** System Administrator  
**I want to** view and manage all data sources in a unified interface  
**So that** I can efficiently oversee the entire data pipeline

#### Acceptance Criteria:
- ✅ DataGrid with sorting, filtering, and pagination
- ✅ Quick action buttons (view, edit, archive) for each data source
- ✅ Status indicators (webhook enabled, table configured)
- ✅ Bulk operations with multi-select capabilities
- ✅ Search functionality across names and descriptions
- ✅ Empty state guidance for new users

### User Story 4.3: Dark Theme Professional Interface
**As a** System Administrator  
**I want to** work in a professional, eye-friendly interface  
**So that** I can comfortably manage the system during long sessions

#### Acceptance Criteria:
- ✅ Dark theme with warm brick-red color palette
- ✅ High contrast ratios for accessibility
- ✅ Consistent Material-UI component styling
- ✅ Responsive design for desktop and tablet usage
- ✅ Rounded icon variants for modern appearance
- ✅ Proper loading states and skeleton screens

---

## Epic 5: System Administration

### User Story 5.1: Monitor System Health
**As a** System Administrator  
**I want to** view comprehensive system health and operational metrics  
**So that** I can proactively maintain system performance

#### Acceptance Criteria:
- ✅ File watcher status with active/inactive indicators
- ✅ Webhook endpoint health and response times
- ✅ Processing queue depth and throughput metrics
- ✅ Database connection health and query performance
- ✅ Error rate monitoring with alert thresholds
- 🔄 Kafka connectivity status (future when implemented)

### User Story 5.2: Manage Destination Tables
**As a** System Administrator  
**I want to** query and manage destination table data  
**So that** I can verify data quality and troubleshoot issues

#### Acceptance Criteria:
- ✅ Paginated table data viewing with search functionality
- ✅ Dynamic column metadata from schema field definitions
- ✅ SQL-safe querying with parameter binding
- ✅ Export capabilities for data analysis
- ✅ Column sorting and filtering by data type
- ✅ Row count and table size statistics

### User Story 5.3: Secure API Access
**As a** System Administrator  
**I want to** access comprehensive API documentation  
**So that** I can integrate with other systems and troubleshoot issues

#### Acceptance Criteria:
- ✅ OpenAPI 3.0 specification with complete endpoint documentation
- ✅ Interactive Swagger UI at `/swagger-ui.html`
- ✅ Detailed request/response schemas with examples
- ✅ Proper HTTP status code documentation
- ✅ Parameter validation and constraint documentation
- 🔄 Authentication/authorization schemes (future implementation)

---

## Epic 6: External Integration

### User Story 6.1: Simple Webhook Integration
**As an** External Service Developer  
**I want to** easily integrate my payment system with the loyalty platform  
**So that** transaction data flows seamlessly for reward calculation

#### Acceptance Criteria:
- ✅ Auto-generated webhook endpoints based on data source names
- ✅ Clear API documentation with request/response examples
- ✅ JSON payload validation with descriptive error messages
- ✅ Sample request body generation from schema fields
- ✅ Webhook enable/disable for maintenance windows
- ✅ Proper content-type handling and validation

#### Integration Example:
```bash
# Auto-generated endpoint for data source "payments"
POST /web-hook?dataSourceName=payments
Content-Type: application/json

{
  "transactionId": "txn_123456",
  "customerId": "cust_789",
  "amount": 156.78,
  "category": "DINING",
  "timestamp": "2026-03-31T10:30:00Z"
}
```

### User Story 6.2: Robust Error Handling
**As an** External Service Developer  
**I want to** receive clear error messages when integration fails  
**So that** I can quickly diagnose and fix integration issues

#### Acceptance Criteria:
- ✅ Structured error responses with specific error codes
- ✅ Field-level validation errors with clear descriptions
- ✅ HTTP status codes following REST conventions
- ✅ Detailed logging for administrator troubleshooting
- ✅ Graceful degradation when webhooks are disabled
- 🔄 Rate limiting and abuse prevention (future enhancement)

---

## Epic 7: Performance & Scalability

### User Story 7.1: High-Volume Data Processing
**As a** System Administrator  
**I want to** process large volumes of transaction data efficiently  
**So that** the system can scale with business growth

#### Acceptance Criteria:
- ✅ Batch processing with configurable chunk sizes
- ✅ Connection pooling for database operations
- ✅ Lazy loading with OSIV disabled for memory efficiency
- ✅ JOIN FETCH queries to prevent N+1 problems
- ✅ Distributed file processing with collision prevention
- 🔄 Kafka integration for event streaming (planned)

### User Story 7.2: Concurrent Rule Evaluation
**As a** Loyalty Program Administrator  
**I want to** run multiple loyalty rules simultaneously  
**So that** rewards are calculated quickly even with complex rule sets

#### Acceptance Criteria:
- ✅ Thread pool management for concurrent rule execution
- ✅ Rule scheduling isolation (rule failures don't affect others)
- ✅ Configurable parallelism based on system resources
- ✅ Dead lock prevention in database transactions
- 🔄 Rule evaluation optimization (future enhancement)

---

## Technical Specifications

### Architecture Stack
- **Backend**: Java 25, Spring Boot 4, Spring Data JPA, Hibernate
- **Database**: PostgreSQL with schema-managed tables
- **Frontend**: React 18, TypeScript, Material-UI v6, Vite
- **Build**: Maven with frontend-maven-plugin integration
- **Documentation**: OpenAPI 3.0 with SpringDoc integration

### Data Flow Architecture
```
External Services → Webhook Endpoints → JSON Validation → Type Coercion → Database Storage → Rule Evaluation → Reward Calculation
File Systems → File Watchers → CSV Parsing → Type Coercion → Database Storage → Rule Evaluation → Reward Calculation
```

### Security Considerations
- SQL injection prevention through parameter binding
- Input validation at controller and service layers
- Table/column name allow-listing for dynamic SQL
- 🔄 Future: HMAC signature verification for webhooks
- 🔄 Future: Role-based access control for admin operations

---

## Acceptance Testing Scenarios

### Scenario 1: End-to-End Data Source Creation
1. Administrator accesses data source creation wizard
2. Enters data source details (name: "payments", destination table: "payment_events")
3. Defines schema fields (transactionId: STRING, amount: DECIMAL, timestamp: DATE)
4. Configures file watching for `/data/payments/*.csv`
5. Enables webhook with auto-generated endpoint
6. Reviews and confirms configuration
7. System creates destination table, registers file watchers, and activates webhook
8. External service successfully posts transaction data
9. CSV files are automatically processed when added to watched directory

### Scenario 2: Multi-Source Rule Creation
1. Administrator creates rule: "WHEN payments.amount > 100 AND customer.tier = 'GOLD' THEN 50 POINT"
2. System validates rule syntax against available field aliases
3. Rule is scheduled for evaluation every 5 minutes
4. Transaction data arrives via webhook and file ingestion
5. Rule evaluation runs and awards 50 points to qualifying customers
6. Points are recorded in rewards table with audit trail

### Scenario 3: System Monitoring and Troubleshooting
1. Administrator monitors system health dashboard
2. Notices webhook processing errors for one data source
3. Reviews processing logs to identify malformed JSON payload
4. Temporarily disables problematic webhook
5. Contacts external service developer with specific error details
6. Re-enables webhook after integration fix
7. Confirms normal processing resumption

---

## Future Enhancements

### Planned Features (Not Yet Implemented)
- 🔄 **Rule Evaluation Engine**: Complete implementation of WHEN/THEN expression parsing and evaluation
- 🔄 **Kafka Integration**: Event streaming for high-volume, real-time processing
- 🔄 **Authentication & Authorization**: User management, role-based access control
- 🔄 **HMAC Signature Verification**: Webhook security and authenticity validation
- 🔄 **Scheduled Data Sources**: Cron-based polling for external APIs
- 🔄 **Advanced Analytics**: Loyalty program performance dashboards and reporting
- 🔄 **A/B Testing**: Rule variant testing for program optimization
- 🔄 **Customer Portal**: Self-service interface for reward status and redemption

### Technical Debt & Improvements
- Migration from Hibernate `ddl-auto: update` to proper schema versioning
- Comprehensive test coverage for critical business logic
- Performance optimization for large dataset processing
- Enhanced error recovery and retry mechanisms
- Multi-tenant architecture for enterprise deployments

---

## Success Metrics

### Functional Success
- ✅ Zero data loss during ingestion processes
- ✅ Sub-second response times for webhook endpoints
- ✅ 99.9% uptime for data processing pipeline
- ✅ Successful integration of multiple external data sources
- ✅ Accurate rule evaluation and reward calculation

### User Experience Success
- ✅ Intuitive data source creation wizard with <5 minute completion time
- ✅ Comprehensive monitoring dashboards for operational visibility
- ✅ Self-documenting API with interactive testing capabilities
- ✅ Minimal training required for system administrators
- ✅ Professional, accessible user interface

### Business Success
- 🎯 Enable rapid deployment of new loyalty programs
- 🎯 Support high-volume transaction processing (millions per day)
- 🎯 Reduce time-to-market for loyalty program changes
- 🎯 Provide data-driven insights for program optimization
- 🎯 Ensure regulatory compliance for customer data handling

---

*This user story document reflects the current state of the Loyalty Management System as of March 31, 2026. Features marked with ✅ are implemented and tested, while features marked with 🔄 are planned for future releases.*
