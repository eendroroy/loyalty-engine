package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Top-level metadata payload returned by {@code GET /api/admin/metadata}.
 * <p>
 * Contains every configured data source together with its field aliases and
 * data types.  The frontend uses this to drive autocomplete suggestions in
 * the rule-expression editor.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataResponse {
    /** All configured data sources, each with their declared fields. */
    private List<DataSourceMetadataResponse> dataSources;
    /** Total number of field aliases available across all data sources. */
    private int totalFields;
}

