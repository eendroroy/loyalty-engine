package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Paginated response for querying records from a destination table.
 * The {@link #columns} list is derived from the parent {@code DataSource} field definitions.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableDataResponse {

    /** The physical SQL table name where records are stored. */
    private String tableName;

    /** Ordered list of column metadata derived from the DataSource field definitions. */
    private List<ColumnMeta> columns;

    /** Rows of data; each map is keyed by field alias. */
    private List<Map<String, Object>> rows;

    private long totalElements;
    private int  totalPages;
    private int  page;
    private int  size;

    /** Metadata for a single column in the destination table. */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnMeta {
        private String       alias;
        private String       fieldName;
        private FieldDataType dataType;
        private String       description;
    }
}

