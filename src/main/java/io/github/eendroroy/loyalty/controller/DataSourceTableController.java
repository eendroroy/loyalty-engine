package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.dto.response.TableDataResponse;
import io.github.eendroroy.loyalty.service.DataSourceService;
import io.github.eendroroy.loyalty.service.DataSourceTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for querying ingested data from destination tables.
 *
 * <p>Base path: {@code /api/admin/data-sources/{id}/table}
 *
 * <p>Supports paginated, searchable access to the destination table associated
 * with a given {@code DataSource}.  The search term is matched (ILIKE) against
 * all {@code STRING}-typed columns; sorting and pagination are server-side.
 */
@Tag(name = "Imported Data", description = "Query paginated records from a data source's destination table")
@RestController
@RequestMapping("/api/admin/data-sources/{id}/table")
@RequiredArgsConstructor
public class DataSourceTableController {

    private final DataSourceService      dataSourceService;
    private final DataSourceTableService tableService;

    /**
     * Returns a paginated, optionally filtered page of records from the destination
     * table that belongs to the specified data source.
     *
     * <p>The response includes the column schema (derived from the data source's
     * field definitions) so the client can render a dynamic grid without prior
     * knowledge of the table structure.
     *
     * @param id      data source primary key
     * @param search  optional free-text search; applied as ILIKE against all
     *                {@code STRING} columns; blank = no filter
     * @param page    0-based page index (default 0)
     * @param size    page size, clamped to [1, 200] (default 20)
     * @param sortBy  field alias to sort by; omit for default (newest first)
     * @param sortDir sort direction — {@code "asc"} or {@code "desc"} (default asc)
     * @return {@code 200 OK} with paginated table data, or {@code 404} if the
     *         data source does not exist
     */
    @Operation(
            summary = "Query destination table data",
            description = "Returns a paginated, searchable view of all records ingested into the "
                    + "data source's destination table.  The `columns` array in the response "
                    + "describes the schema; `rows` contains the matching records.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data returned"),
            @ApiResponse(responseCode = "404", description = "Data source not found"),
    })
    @GetMapping("/data")
    public ResponseEntity<TableDataResponse> getData(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "Free-text search (ILIKE on all STRING columns)")
            @RequestParam(defaultValue = "") String search,
            @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 200)")  @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field alias") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction: asc|desc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        return dataSourceService.findById(id)
                .map(ds -> tableService.queryRecords(ds, search, page, size, sortBy, sortDir))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

