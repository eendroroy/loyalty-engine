# Database Schema Diagram

```mermaid
erDiagram
    DATA_SOURCES {
        bigint id PK
        varchar name UK "Unique data source name"
        text description
        varchar destination_table
        boolean archived "Soft delete flag"
        timestamp created_at
        timestamp updated_at
    }
    
    DATA_SOURCE_SCHEMA_FIELDS {
        bigint id PK
        bigint data_source_id FK
        varchar name "Field name"
        text description
        varchar data_type "STRING|INTEGER|DECIMAL|DATE|BOOLEAN"
        timestamp created_at
        timestamp updated_at
    }
    
    DATA_SOURCE_FILES {
        bigint id PK
        bigint data_source_id FK
        varchar file_path "CSV file path (unique)"
        text description
        varchar field_separator "Default: comma"
        varchar quote_character "Default: double quote"
        varchar line_separator "Default: system"
        int skip_first_n_lines "Default: 0"
        varchar archive_directory "Optional: post-ingestion archive path"
        timestamp created_at
        timestamp updated_at
    }
    
    DATA_SOURCE_FIELDS {
        bigint id PK
        bigint data_source_file_id FK
        varchar field_name "CSV column header"
        varchar field_alias "Unique alias for rule expressions"
        int column_number "1-based column index (optional)"
        varchar data_type "STRING|INTEGER|DECIMAL|DATE|BOOLEAN"
        varchar date_format "DateTimeFormatter pattern; null = ISO-8601"
        text description "Autocomplete hint in rule editor"
        timestamp created_at
        timestamp updated_at
    }
    
    DATA_SOURCE_WEBHOOKS {
        bigint id PK
        bigint data_source_id FK
        varchar endpoint "Auto-generated path"
        boolean enabled "Active status"
        text description
        timestamp created_at
        timestamp updated_at
    }
    
    RULES {
        bigint id PK
        varchar name "Rule display name"
        text description
        text rule_expression "WHEN...THEN syntax"
        varchar status "DRAFT|ACTIVE|INACTIVE"
        int priority "Execution order"
        date active_from "Optional start date"
        date active_to "Optional end date"
        varchar frequency "Cron expression"
        timestamp last_run_at "Last evaluation time"
        timestamp created_at
        timestamp updated_at
    }
    
    RULE_ACTIONS {
        bigint id PK
        bigint rule_id FK
        varchar reward_type "POINT|VOUCHER"
        varchar reward_amount "Points or voucher code"
        text description
        timestamp created_at
        timestamp updated_at
    }
    
    VOUCHERS {
        bigint id PK
        varchar name "Display name"
        varchar code UK "Public template code"
        text description
        varchar voucher_type "DISCOUNT"
        int count "Maximum instances"
        boolean active "Available for awarding"
        boolean archived "Soft delete flag"
        timestamp created_at
        timestamp updated_at
    }
    
    VOUCHER_INSTANCES {
        bigint id PK
        bigint voucher_id FK
        varchar secret_code UK "7-char unique code"
        timestamp created_at
    }
    
    FILE_PROCESSING_LOGS {
        bigint id PK
        bigint data_source_file_id FK
        varchar source_name
        varchar file_path
        varchar file_name
        bigint file_size_bytes
        bigint last_modified_millis
        varchar status "IN_PROGRESS|COMPLETED|FAILED"
        int rows_ingested
        int rows_skipped
        int errors
        varchar instance_id "Processing instance"
        timestamp started_at
        timestamp completed_at
    }
    
    %% Relationships
    DATA_SOURCES ||--o{ DATA_SOURCE_SCHEMA_FIELDS : "has schema"
    DATA_SOURCES ||--o{ DATA_SOURCE_FILES : "has files"
    DATA_SOURCES ||--o| DATA_SOURCE_WEBHOOKS : "has webhook"
    DATA_SOURCE_FILES ||--o{ DATA_SOURCE_FIELDS : "has field mappings"
    
    RULES ||--o{ RULE_ACTIONS : "has actions"
    
    VOUCHERS ||--o{ VOUCHER_INSTANCES : "generates instances"
    
    DATA_SOURCE_FILES ||--o{ FILE_PROCESSING_LOGS : "processing history"
```

## Entity Relationships and Constraints

### Primary Key Strategy
- All tables use `BIGSERIAL` for primary keys
- Provides 64-bit integer range (up to 9.2 quintillion records)
- Auto-incrementing for simple insertion

### Foreign Key Constraints
```sql
-- Data Source relationships
ALTER TABLE data_source_schema_fields 
    ADD CONSTRAINT fk_schema_fields_data_source 
    FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE CASCADE;

ALTER TABLE data_source_files 
    ADD CONSTRAINT fk_files_data_source 
    FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE CASCADE;

ALTER TABLE data_source_webhooks 
    ADD CONSTRAINT fk_webhooks_data_source 
    FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE CASCADE;

-- Rule relationships
ALTER TABLE rule_actions 
    ADD CONSTRAINT fk_actions_rule 
    FOREIGN KEY (rule_id) REFERENCES rules(id) ON DELETE CASCADE;

-- Voucher relationships  
ALTER TABLE voucher_instances 
    ADD CONSTRAINT fk_instances_voucher 
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE;

-- Processing log relationships
ALTER TABLE file_processing_logs 
    ADD CONSTRAINT fk_logs_file 
    FOREIGN KEY (data_source_file_id) REFERENCES data_source_files(id) ON DELETE CASCADE;
```

### Unique Constraints
```sql
-- Business rule: Data source names must be unique
ALTER TABLE data_sources ADD CONSTRAINT uk_data_sources_name UNIQUE (name);

-- Business rule: Voucher codes must be unique (public identifiers)
ALTER TABLE vouchers ADD CONSTRAINT uk_vouchers_code UNIQUE (code);

-- Security rule: Secret codes must be globally unique
ALTER TABLE voucher_instances ADD CONSTRAINT uk_voucher_instances_secret_code UNIQUE (secret_code);
```

### Check Constraints
```sql
-- Data validation rules
ALTER TABLE rules 
    ADD CONSTRAINT chk_rules_status 
    CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'));

ALTER TABLE rule_actions 
    ADD CONSTRAINT chk_rule_actions_reward_type 
    CHECK (reward_type IN ('POINT', 'VOUCHER'));

ALTER TABLE data_source_schema_fields 
    ADD CONSTRAINT chk_schema_fields_data_type 
    CHECK (data_type IN ('STRING', 'INTEGER', 'DECIMAL', 'DATE', 'BOOLEAN'));

ALTER TABLE vouchers 
    ADD CONSTRAINT chk_vouchers_count 
    CHECK (count >= 0);

ALTER TABLE voucher_instances 
    ADD CONSTRAINT chk_voucher_instances_secret_code_format 
    CHECK (secret_code ~ '^[A-Z0-9]{7}$');

ALTER TABLE file_processing_logs 
    ADD CONSTRAINT chk_file_processing_logs_status 
    CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED'));
```

### Performance Indexes
```sql
-- Query optimization indexes
CREATE INDEX idx_data_sources_archived ON data_sources(archived) WHERE archived = false;
CREATE INDEX idx_rules_status_priority ON rules(status, priority DESC) WHERE status = 'ACTIVE';
CREATE INDEX idx_rules_active_dates ON rules(active_from, active_to) WHERE active_from IS NOT NULL OR active_to IS NOT NULL;
CREATE INDEX idx_vouchers_active ON vouchers(active, archived) WHERE active = true AND archived = false;
CREATE INDEX idx_voucher_instances_voucher_id ON voucher_instances(voucher_id);
CREATE INDEX idx_file_processing_logs_status_started ON file_processing_logs(status, started_at);

-- Text search indexes
CREATE INDEX idx_data_sources_name_gin ON data_sources USING gin(to_tsvector('english', name));
CREATE INDEX idx_rules_name_expression_gin ON rules USING gin(to_tsvector('english', name || ' ' || rule_expression));
```

### Audit Triggers
```sql
-- Automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to all entities with updated_at
CREATE TRIGGER update_data_sources_updated_at BEFORE UPDATE ON data_sources 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_rules_updated_at BEFORE UPDATE ON rules 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_vouchers_updated_at BEFORE UPDATE ON vouchers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```
