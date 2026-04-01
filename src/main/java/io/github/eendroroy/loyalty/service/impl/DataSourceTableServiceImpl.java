package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.dto.response.TableDataResponse;
import io.github.eendroroy.loyalty.dto.response.TableDataResponse.ColumnMeta;
import io.github.eendroroy.loyalty.entity.DataSource;
import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import io.github.eendroroy.loyalty.model.IngestedRecord;
import io.github.eendroroy.loyalty.repository.DataSourceSchemaFieldRepository;
import io.github.eendroroy.loyalty.service.DataSourceTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JDBC-backed implementation of {@link DataSourceTableService}.
 *
 * <p><strong>SQL safety</strong>: table names and column identifiers are validated
 * against an allow-list pattern and double-quoted (PostgreSQL identifier quoting)
 * before being interpolated into SQL strings.  All user-supplied search/filter
 * <em>values</em> are passed via JDBC {@code ?} parameters — never interpolated.
 *
 * <p><strong>Specification pattern</strong>: {@link #buildWhereClause} acts as a
 * JDBC analogue of JPA {@code Specification} — it returns a validated SQL fragment
 * and the corresponding parameter list, which are composed into the final query.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceTableServiceImpl implements DataSourceTableService {

    private final JdbcClient jdbcClient;
    private final DataSourceSchemaFieldRepository schemaFieldRepository;

    // ── Table DDL ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void createDestinationTableIfNotExists(DataSource dataSource) {
        var schemaFields = schemaFieldRepository.findByDataSourceIdOrderById(dataSource.getId());
        if (!schemaFields.isEmpty()) {
            createDestinationTableFromSchema(dataSource, schemaFields);
        } else {
            // Fallback: derive DDL from file-level fields (backward compat)
            var fileFields = dataSource.getAllFields();
            if (fileFields.isEmpty()) {
                log.warn("DataSource {} has destinationTable='{}' but no field definitions — "
                                + "table creation skipped until fields are added",
                        dataSource.getId(), dataSource.getDestinationTable());
                return;
            }
            createDestinationTableIfNotExists(dataSource, fileFields);
        }
    }

    @Override
    @Transactional
    public void createDestinationTableIfNotExists(DataSource dataSource, List<DataSourceField> fields) {
        String tableName = validTableName(dataSource.getDestinationTable());
        if (tableName == null) {
            log.warn("DataSource {} has no destination table defined", dataSource.getId());
            return;
        }
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("Cannot create destination table without field definitions");
        }

        var sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(quoteTableName(tableName))
                .append(" (id BIGSERIAL PRIMARY KEY,")
                .append(" created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,")
                .append(" source VARCHAR(255),")
                .append(" file_read_time TIMESTAMP");

        for (DataSourceField field : fields) {
            sql.append(", ").append(quoteIdentifier(field.getFieldAlias()))
               .append(" ").append(toSqlType(field.getDataType()));
        }
        sql.append(")");

        try {
            jdbcClient.sql(sql.toString()).update();
            log.info("Ensured destination table '{}' for DataSource {}", tableName, dataSource.getId());
        } catch (Exception e) {
            log.error("Failed to create destination table '{}': {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create destination table", e);
        }
    }

    /**
     * Creates the destination table using the canonical {@link DataSourceSchemaField} list.
     * Column names come from {@link DataSourceSchemaField#getName()} and types from
     * {@link DataSourceSchemaField#getDataType()} (the <em>target</em> DDL type).
     */
    private void createDestinationTableFromSchema(DataSource dataSource,
                                                   List<DataSourceSchemaField> schemaFields) {
        String tableName = validTableName(dataSource.getDestinationTable());
        if (tableName == null) {
            log.warn("DataSource {} has no destination table defined", dataSource.getId());
            return;
        }
        if (schemaFields.isEmpty()) {
            throw new IllegalArgumentException("Cannot create destination table without field definitions");
        }

        var sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(quoteTableName(tableName))
                .append(" (id BIGSERIAL PRIMARY KEY,")
                .append(" created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,")
                .append(" source VARCHAR(255),")
                .append(" file_read_time TIMESTAMP");

        for (DataSourceSchemaField field : schemaFields) {
            sql.append(", ").append(quoteIdentifier(field.getName()))
               .append(" ").append(toSqlType(field.getDataType()));
        }
        sql.append(")");

        try {
            jdbcClient.sql(sql.toString()).update();
            log.info("Ensured destination table '{}' for DataSource {} (schema-based)",
                    tableName, dataSource.getId());
        } catch (Exception e) {
            log.error("Failed to create destination table '{}': {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to create destination table", e);
        }
    }

    // ── Insert ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public int insertRecords(DataSource dataSource, List<IngestedRecord> records) {
        return insertRecords(dataSource, records, dataSource.getAllFields());
    }

    @Override
    @Transactional
    public int insertRecords(DataSource dataSource, List<IngestedRecord> records,
                             List<DataSourceField> fields) {
        String tableName = validTableName(dataSource.getDestinationTable());
        if (tableName == null || records.isEmpty()) return 0;

        Map<String, DataSourceField> byAlias = fields.stream()
                .collect(Collectors.toMap(DataSourceField::getFieldAlias, f -> f));

        int inserted = 0;
        for (IngestedRecord record : records) {
            try {
                var cols = new ArrayList<String>();
                var vals = new ArrayList<>();
                
                // Add metadata columns first
                cols.add("source");
                vals.add(record.source());
                
                cols.add("file_read_time");
                vals.add(record.fileReadTime());
                
                // Add user data fields
                for (var entry : record.fields().entrySet()) {
                    if (byAlias.containsKey(entry.getKey())) {
                        cols.add(quoteIdentifier(entry.getKey()));
                        vals.add(entry.getValue());
                    }
                }
                
                if (cols.isEmpty()) continue;
                String colList = String.join(", ", cols);
                String placeholders = cols.stream().map(_c -> "?").collect(Collectors.joining(", "));
                String insertSql = "INSERT INTO " + quoteTableName(tableName)
                        + " (" + colList + ") VALUES (" + placeholders + ")";
                
                jdbcClient.sql(insertSql)
                        .params(vals)
                        .update();
                inserted++;
            } catch (Exception e) {
                log.warn("Failed to insert record into {}: {}", tableName, e.getMessage());
            }
        }
        log.info("Inserted {} records into table '{}'", inserted, tableName);
        return inserted;
    }

    // ── findAllRecords (rule engine / legacy) ─────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<IngestedRecord> findAllRecords(DataSource dataSource) {
        String tableName = validTableName(dataSource.getDestinationTable());
        if (tableName == null) return new ArrayList<>();
        try {
            return jdbcClient.sql("SELECT * FROM " + quoteTableName(tableName))
                    .query()
                    .listOfRows()
                    .stream()
                    .map(this::rowToIngestedRecord)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to query table '{}': {}", tableName, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Paginated query with search (Specification-style WHERE builder) ────────

    @Override
    @Transactional(readOnly = true)
    public TableDataResponse queryRecords(DataSource dataSource,
                                          String search,
                                          int page,
                                          int size,
                                          String sortBy,
                                          String sortDir) {
        // Schema fields are canonical (tables are created from them); fall back to file-level fields
        // for backward-compat sources that were defined before schema fields existed.
        var fields = effectiveFields(dataSource);
        var columns = buildColumnMeta(fields);
        String tableName = validTableName(dataSource.getDestinationTable());

        if (tableName == null) {
            return TableDataResponse.builder()
                    .tableName(dataSource.getDestinationTable())
                    .columns(columns).rows(List.of())
                    .totalElements(0).totalPages(0).page(page).size(size)
                    .build();
        }

        int clampedSize = Math.max(1, Math.min(size, 200));
        int offset = Math.max(0, page) * clampedSize;

        // ── Specification-style WHERE clause ──────────────────────────────
        var where = buildWhereClause(search, fields);
        String orderBy = buildOrderBy(sortBy, sortDir, fields);

        String countSql = "SELECT COUNT(*) FROM " + quoteTableName(tableName) + where.sql();
        String dataSql  = "SELECT * FROM " + quoteTableName(tableName)
                + where.sql() + orderBy + " LIMIT ? OFFSET ?";

        long total;
        try {
            total = jdbcClient.sql(countSql)
                    .params(where.params())
                    .query(Long.class)
                    .single();
        } catch (Exception e) {
            log.warn("Table '{}' not yet created or query failed: {}", tableName, e.getMessage());
            return TableDataResponse.builder()
                    .tableName(tableName).columns(columns).rows(List.of())
                    .totalElements(0).totalPages(0).page(page).size(clampedSize)
                    .build();
        }

        var pageParams = new ArrayList<>(where.params());
        pageParams.add(clampedSize);
        pageParams.add(offset);

        List<Map<String, Object>> rows;
        try {
            rows = jdbcClient.sql(dataSql)
                    .params(pageParams)
                    .query()
                    .listOfRows()
                    .stream()
                    .map(this::normaliseRow)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to query data from '{}': {}", tableName, e.getMessage());
            rows = List.of();
        }

        int totalPages = (int) Math.ceil((double) total / clampedSize);
        return TableDataResponse.builder()
                .tableName(tableName).columns(columns).rows(rows)
                .totalElements(total).totalPages(totalPages).page(page).size(clampedSize)
                .build();
    }

    // ── deleteAllRecords ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteAllRecords(DataSource dataSource) {
        String tableName = validTableName(dataSource.getDestinationTable());
        if (tableName == null) return;
        try {
            jdbcClient.sql("DELETE FROM " + quoteTableName(tableName)).update();
            log.info("Deleted all records from table '{}'", tableName);
        } catch (Exception e) {
            log.error("Failed to delete from '{}': {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to delete records", e);
        }
    }

    // ── dropDestinationTable ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void dropDestinationTable(DataSource dataSource) {
        String tableName = validTableName(dataSource.getDestinationTable());
        if (tableName == null) return;
        try {
            jdbcClient.sql("DROP TABLE IF EXISTS " + quoteTableName(tableName)).update();
            log.info("Dropped destination table '{}'", tableName);
        } catch (Exception e) {
            log.error("Failed to drop '{}': {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to drop table", e);
        }
    }

    // ── Specification-style WHERE builder ─────────────────────────────────────

    /**
     * Builds a validated WHERE-clause fragment + bind-parameter list from the
     * supplied free-text search string.  Only {@link FieldDataType#STRING} columns
     * are searched (ILIKE).  No user value is ever interpolated into SQL.
     *
     * <p>This is the JDBC analogue of a JPA {@code Specification}: it encapsulates
     * the predicate logic and is composed into the final query by the caller.
     */
    private WhereClause buildWhereClause(String search, List<DataSourceField> fields) {
        var conditions = new ArrayList<String>();
        var params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            var textCols = fields.stream()
                    .filter(f -> f.getDataType() == FieldDataType.STRING)
                    .toList();
            if (!textCols.isEmpty()) {
                String orParts = textCols.stream()
                        .map(f -> "CAST(" + quoteIdentifier(f.getFieldAlias()) + " AS TEXT) ILIKE ? ESCAPE '\\\\'")
                        .collect(Collectors.joining(" OR "));
                conditions.add("(" + orParts + ")");
                String escapedSearch = search.replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                textCols.forEach(_f -> params.add("%" + escapedSearch + "%"));
            }
        }

        String sql = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new WhereClause(sql, params);
    }

    private record WhereClause(String sql, List<Object> params) {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Validates a table name against the allow-list pattern and returns it, or
     * {@code null} if the name is absent/blank.  Throws if the name is non-blank
     * but fails the pattern (prevents SQL injection via identifiers).
     */
    private String validTableName(String name) {
        if (name == null || name.isBlank()) return null;
        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)?$")) {
            throw new IllegalArgumentException("Invalid table name: " + name);
        }
        return name;
    }

    /** Double-quotes a column identifier (single identifier, dots allowed in name). */
    private String quoteIdentifier(String name) {
        if (!name.matches("^[a-zA-Z][a-zA-Z0-9._]*$")) {
            throw new IllegalArgumentException("Invalid identifier: " + name);
        }
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    /** Quotes a table name; supports schema-qualified names by quoting each part. */
    private String quoteTableName(String tableName) {
        String[] parts = tableName.split("\\.");
        for (String part : parts) {
            if (!part.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                throw new IllegalArgumentException("Invalid table name: " + tableName);
            }
        }
        return java.util.Arrays.stream(parts)
                .map(part -> "\"" + part.replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining("."));
    }

    private String buildOrderBy(String sortBy, String sortDir, List<DataSourceField> fields) {
        String dir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        if (sortBy == null || sortBy.isBlank()) return " ORDER BY created_at DESC";
        
        boolean valid = "id".equals(sortBy) || "created_at".equals(sortBy) 
                || "source".equals(sortBy) || "file_read_time".equals(sortBy)
                || fields.stream().anyMatch(f -> f.getFieldAlias().equals(sortBy));
                
        if (!valid) return " ORDER BY created_at DESC";
        
        String col;
        if ("id".equals(sortBy) || "created_at".equals(sortBy) 
                || "source".equals(sortBy) || "file_read_time".equals(sortBy)) {
            col = sortBy;
        } else {
            col = quoteIdentifier(sortBy);
        }
        return " ORDER BY " + col + " " + dir;
    }

    private String toSqlType(FieldDataType dataType) {
        return switch (dataType) {
            case STRING  -> "VARCHAR(255)";
            case INTEGER -> "BIGINT";
            case DECIMAL -> "DECIMAL(19, 2)";
            case DATE    -> "DATE";
            case BOOLEAN -> "BOOLEAN";
        };
    }

    private List<ColumnMeta> buildColumnMeta(List<DataSourceField> fields) {
        var columns = new ArrayList<ColumnMeta>();
        
        // Add system metadata columns first
        columns.add(ColumnMeta.builder()
                .alias("source")
                .fieldName("source")
                .dataType(FieldDataType.STRING)
                .description("Data source identifier (FILE:<filename> or HOOK:<producer>)")
                .build());
                
        columns.add(ColumnMeta.builder()
                .alias("file_read_time")
                .fieldName("file_read_time")
                .dataType(FieldDataType.DATE)
                .description("Timestamp when the data was ingested")
                .build());
        
        // Add user-defined data fields
        columns.addAll(fields.stream()
                .map(f -> ColumnMeta.builder()
                        .alias(f.getFieldAlias())
                        .fieldName(f.getFieldName())
                        .dataType(f.getDataType())
                        .description(f.getDescription())
                        .build())
                .collect(Collectors.toList()));
                
        return columns;
    }

    /**
     * Returns the effective field list for query metadata and WHERE/ORDER-BY building.
     * Schema fields are canonical (the destination table is created from them); file-level
     * fields are used only as a backward-compat fallback for sources that pre-date schema fields.
     */
    private List<DataSourceField> effectiveFields(DataSource dataSource) {
        var schemaFields = dataSource.getSchemaFields();
        if (schemaFields != null && !schemaFields.isEmpty()) {
            return toSyntheticFields(schemaFields);
        }
        // Backward compat: sources without schema fields use their file-level field aliases.
        var fileFields = dataSource.getAllFields();
        if (!fileFields.isEmpty()) return fileFields;
        // Last resort: query the repository (handles detached entities where collection was not loaded)
        return toSyntheticFields(schemaFieldRepository.findByDataSourceIdOrderById(dataSource.getId()));
    }

    /** Converts schema fields into transient {@link DataSourceField} objects for use in SQL building. */
    private List<DataSourceField> toSyntheticFields(List<DataSourceSchemaField> schemaFields) {
        if (schemaFields == null) return List.of();
        return schemaFields.stream().map(sf -> {
            var f = new DataSourceField();
            f.setFieldName(sf.getName());
            f.setFieldAlias(sf.getName());
            f.setDataType(sf.getDataType());
            f.setDescription(sf.getDescription());
            return f;
        }).toList();
    }

    /** Converts JDBC row map values to JSON-friendly types. */
    private Map<String, Object> normaliseRow(Map<String, Object> row) {
        var out = new HashMap<String, Object>();
        row.forEach((k, v) -> {
            if (v instanceof java.sql.Date d)           out.put(k, d.toLocalDate().toString());
            else if (v instanceof java.sql.Timestamp ts) out.put(k, ts.toLocalDateTime().toString());
            else                                         out.put(k, v);
        });
        return out;
    }

    private IngestedRecord rowToIngestedRecord(Map<String, Object> row) {
        var data = new HashMap<String, Object>();
        row.forEach((k, v) -> {
            if (!k.equals("id") && !k.equals("created_at") && !k.equals("source") && !k.equals("file_read_time")) {
                data.put(k, v);
            }
        });
        return new IngestedRecord(null, null, null, null, null, data);
    }
}
