package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.dto.request.DataSourceFieldRequest;
import io.github.eendroroy.loyalty.dto.request.DataSourceFileRequest;
import io.github.eendroroy.loyalty.dto.request.DataSourceRequest;
import io.github.eendroroy.loyalty.dto.request.DataSourceSchemaFieldRequest;
import io.github.eendroroy.loyalty.dto.request.DataSourceWebhookRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceFieldResponse;
import io.github.eendroroy.loyalty.dto.response.DataSourceFileResponse;
import io.github.eendroroy.loyalty.dto.response.DataSourceResponse;
import io.github.eendroroy.loyalty.dto.response.DataSourceSchemaFieldResponse;
import io.github.eendroroy.loyalty.dto.response.DataSourceWebhookResponse;
import io.github.eendroroy.loyalty.mapper.DataSourceFieldMapper;
import io.github.eendroroy.loyalty.mapper.DataSourceFileMapper;
import io.github.eendroroy.loyalty.mapper.DataSourceMapper;
import io.github.eendroroy.loyalty.mapper.DataSourceSchemaFieldMapper;
import io.github.eendroroy.loyalty.mapper.DataSourceWebhookMapper;
import io.github.eendroroy.loyalty.service.DataSourceFieldService;
import io.github.eendroroy.loyalty.service.DataSourceFileService;
import io.github.eendroroy.loyalty.service.DataSourceSchemaFieldService;
import io.github.eendroroy.loyalty.service.DataSourceService;
import io.github.eendroroy.loyalty.service.DataSourceTableService;
import io.github.eendroroy.loyalty.service.DataSourceWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@link io.github.eendroroy.loyalty.entity.DataSource} resources
 * and their nested files, fields, and webhooks.
 *
 * <p>Base path: {@code /api/admin/data-sources}
 *
 * <p>Field definitions now live on individual files. A {@code fields} list may be
 * included directly inside each file payload in the {@code POST}/{@code PUT} body
 * to create or replace fields atomically with the file.
 * Individual field endpoints under {@code /files/{fileId}/fields/{fieldId}} remain
 * available for fine-grained updates.
 */
@Tag(name = "Data Sources",
     description = "Manage ingestion sources, their schema fields, file paths (with per-file field schemas), "
             + "webhook endpoints, and destination tables")
@RestController
@RequestMapping("/api/admin/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService           dataSourceService;
    private final DataSourceFileService       dataSourceFileService;
    private final DataSourceFieldService      dataSourceFieldService;
    private final DataSourceWebhookService    dataSourceWebhookService;
    private final DataSourceSchemaFieldService dataSourceSchemaFieldService;
    private final DataSourceTableService      dataSourceTableService;
    private final DataSourceMapper            dataSourceMapper;
    private final DataSourceFileMapper        dataSourceFileMapper;
    private final DataSourceFieldMapper       dataSourceFieldMapper;
    private final DataSourceWebhookMapper     dataSourceWebhookMapper;
    private final DataSourceSchemaFieldMapper dataSourceSchemaFieldMapper;

    // ── Data Source CRUD ─────────────────────────────────────────────────────

    @Operation(summary = "List all data sources",
               description = "Returns all data sources with their files (including per-file field schemas).")
    @GetMapping
    public List<DataSourceResponse> getAll() {
        return dataSourceService.findAll().stream().map(dataSourceMapper::toResponse).toList();
    }

    @Operation(summary = "List data sources with destination tables",
               description = "Returns only data sources that have a configured destinationTable.")
    @GetMapping("/with-tables")
    public List<DataSourceResponse> getWithTables() {
        return dataSourceService.findAllWithDestinationTable()
                .stream().map(dataSourceMapper::toResponse).toList();
    }

    @Operation(summary = "Get data source by ID")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @GetMapping("/{id}")
    public ResponseEntity<DataSourceResponse> getById(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        return dataSourceService.findById(id)
                .map(dataSourceMapper::toResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a data source",
               description = "Creates the source with optional inline `fields` (schema) and `files`. "
                   + "Webhook is auto-created (disabled by default).")
    @ApiResponse(responseCode = "201", description = "Data source created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataSourceResponse create(@Valid @RequestBody DataSourceRequest request) {
        var entity = dataSourceMapper.toEntity(request);

        if (request.getFields() != null) {
            request.getFields().stream().map(dataSourceSchemaFieldMapper::toEntity)
                    .forEach(f -> { f.setDataSource(entity); entity.getSchemaFields().add(f); });
        }
        if (request.getFiles() != null) {
            request.getFiles().forEach(fileReq -> {
                var file = dataSourceFileMapper.toEntity(fileReq);
                file.setDataSource(entity);
                if (fileReq.getFields() != null) {
                    fileReq.getFields().stream().map(dataSourceFieldMapper::toEntity)
                            .forEach(f -> { f.setDataSourceFile(file); file.getFields().add(f); });
                }
                entity.getFiles().add(file);
            });
        }

        var saved = dataSourceService.save(entity);
        return dataSourceService.findById(saved.getId())
                .map(dataSourceMapper::toResponseWithWebhook).orElseThrow();
    }

    @Operation(summary = "Update a data source",
               description = "Non-null `fields`/`files` replace existing ones. "
                   + "Webhook enabled status can be updated; properties are auto-synced to schema fields.")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @PutMapping("/{id}")
    public ResponseEntity<DataSourceResponse> update(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Valid @RequestBody DataSourceRequest request) {

        var newSchemaFields = request.getFields() == null ? null :
                request.getFields().stream().map(dataSourceSchemaFieldMapper::toEntity).toList();

        List<io.github.eendroroy.loyalty.entity.DataSourceFile> newFiles = null;
        if (request.getFiles() != null) {
            newFiles = request.getFiles().stream().map(fileReq -> {
                var file = dataSourceFileMapper.toEntity(fileReq);
                if (fileReq.getFields() != null) {
                    fileReq.getFields().stream().map(dataSourceFieldMapper::toEntity)
                            .forEach(f -> { f.setDataSourceFile(file); file.getFields().add(f); });
                }
                return file;
            }).toList();
        }

        // Convert singular webhook request to list for service
        List<io.github.eendroroy.loyalty.entity.DataSourceWebhook> newWebhooks = null;
        if (request.getWebhook() != null) {
            var wh = dataSourceWebhookMapper.toEntity(request.getWebhook());
            newWebhooks = List.of(wh);
        }

        return dataSourceService.update(id,
                existing -> dataSourceMapper.updateEntity(request, existing),
                newFiles, newWebhooks, newSchemaFields)
                .map(saved -> ResponseEntity.ok(
                        dataSourceService.findById(saved.getId())
                                .map(dataSourceMapper::toResponseWithWebhook).orElseThrow()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a data source",
               description = "Direct deletion is disabled. Use POST /{id}/archive followed by "
                       + "DELETE /{id}/purge instead.")
    @ApiResponses({ @ApiResponse(responseCode = "405", description = "Method not allowed — use archive/purge") })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // ── Schema Field Management ────────────────────────────────────────────────

    @Operation(summary = "List schema fields for a data source",
               description = "Returns the canonical destination-column schema for the data source.")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @GetMapping("/{id}/schema-fields")
    public ResponseEntity<List<DataSourceSchemaFieldResponse>> getSchemaFields(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataSourceSchemaFieldService.findByDataSourceId(id).stream()
                .map(dataSourceSchemaFieldMapper::toResponse).toList());
    }

    @Operation(summary = "Add a schema field to a data source")
    @ApiResponses({ @ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "404") })
    @PostMapping("/{id}/schema-fields")
    public ResponseEntity<DataSourceSchemaFieldResponse> addSchemaField(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Valid @RequestBody DataSourceSchemaFieldRequest request) {
        return dataSourceService.findById(id).map(ds -> {
            var field = dataSourceSchemaFieldMapper.toEntity(request);
            field.setDataSource(ds);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(dataSourceSchemaFieldMapper.toResponse(
                            dataSourceSchemaFieldService.save(field)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a schema field")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @PutMapping("/{id}/schema-fields/{fieldId}")
    public ResponseEntity<DataSourceSchemaFieldResponse> updateSchemaField(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "Schema field ID") @PathVariable Long fieldId,
            @Valid @RequestBody DataSourceSchemaFieldRequest request) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        return dataSourceSchemaFieldService.findById(fieldId).map(existing -> {
            dataSourceSchemaFieldMapper.updateEntity(request, existing);
            return ResponseEntity.ok(dataSourceSchemaFieldMapper.toResponse(
                    dataSourceSchemaFieldService.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a schema field")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404") })
    @DeleteMapping("/{id}/schema-fields/{fieldId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteSchemaField(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "Schema field ID") @PathVariable Long fieldId) {
        if (dataSourceService.findById(id).isEmpty()
                || dataSourceSchemaFieldService.findById(fieldId).isEmpty())
            return ResponseEntity.notFound().build();
        dataSourceSchemaFieldService.deleteById(fieldId);
        return ResponseEntity.noContent().build();
    }

    // ── File Management ───────────────────────────────────────────────────────

    @Operation(summary = "List watched files for a data source")
    @GetMapping("/{id}/files")
    public ResponseEntity<List<DataSourceFileResponse>> getFiles(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataSourceFileService.findByDataSourceId(id).stream()
                .map(dataSourceFileMapper::toResponse).toList());
    }

    @Operation(summary = "Add a file path to a data source (with optional inline fields)")
    @ApiResponses({ @ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "404") })
    @PostMapping("/{id}/files")
    public ResponseEntity<DataSourceFileResponse> addFile(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Valid @RequestBody DataSourceFileRequest request) {
        return dataSourceService.findById(id).map(ds -> {
            var file = dataSourceFileMapper.toEntity(request);
            file.setDataSource(ds);
            if (request.getFields() != null) {
                request.getFields().stream().map(dataSourceFieldMapper::toEntity)
                        .forEach(f -> { f.setDataSourceFile(file); file.getFields().add(f); });
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(dataSourceFileMapper.toResponse(dataSourceFileService.save(file)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a watched file path")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @PutMapping("/{id}/files/{fileId}")
    public ResponseEntity<DataSourceFileResponse> updateFile(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Valid @RequestBody DataSourceFileRequest request) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        List<io.github.eendroroy.loyalty.entity.DataSourceField> newFields = null;
        if (request.getFields() != null) {
            newFields = request.getFields().stream().map(dataSourceFieldMapper::toEntity).toList();
        }
        var finalNewFields = newFields;
        return dataSourceFileService
                .updateWithFields(fileId,
                        existing -> dataSourceFileMapper.updateEntity(request, existing),
                        finalNewFields)
                .map(dataSourceFileMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove a watched file path")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404") })
    @DeleteMapping("/{id}/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "File ID") @PathVariable Long fileId) {
        if (dataSourceService.findById(id).isEmpty()
                || dataSourceFileService.findById(fileId).isEmpty()) return ResponseEntity.notFound().build();
        dataSourceFileService.deleteById(fileId);
        return ResponseEntity.noContent().build();
    }

    // ── Webhook Management ────────────────────────────────────────────────────

    @Operation(summary = "Get webhook config for a data source")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @GetMapping("/{id}/webhook")
    public ResponseEntity<DataSourceWebhookResponse> getWebhook(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        var dsOpt = dataSourceService.findById(id);
        if (dsOpt.isEmpty()) return ResponseEntity.notFound().build();

        var ds = dsOpt.get();
        if (ds.getWebhook() != null) {
            var response = DataSourceWebhookResponse.builder()
                    .id(ds.getWebhook().getId())
                    .endpoint(ds.getWebhook().generateEndpoint())
                    .enabled(ds.getWebhook().isEnabled())
                    .description(ds.getWebhook().getDescription())
                    .properties(ds.getSchemaFields().stream()
                        .map(sf -> DataSourceSchemaFieldResponse.builder()
                            .id(sf.getId())
                            .name(sf.getName())
                            .description(sf.getDescription())
                            .dataType(sf.getDataType())
                            .createdAt(sf.getCreatedAt())
                            .updatedAt(sf.getUpdatedAt())
                            .build())
                        .toList())
                    .createdAt(ds.getWebhook().getCreatedAt())
                    .updatedAt(ds.getWebhook().getUpdatedAt())
                    .build();
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update webhook enabled status",
               description = "Toggle webhook active/inactive status. Properties are auto-synced to schema fields.")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @PutMapping("/{id}/webhook")
    public ResponseEntity<DataSourceWebhookResponse> updateWebhookStatus(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Valid @RequestBody DataSourceWebhookRequest request) {
        var dsOpt = dataSourceService.findById(id);
        if (dsOpt.isEmpty()) return ResponseEntity.notFound().build();

        var ds = dsOpt.get();
        if (ds.getWebhook() != null) {
            ds.getWebhook().setEnabled(request.getEnabled() != null ? request.getEnabled() : false);
            if (request.getDescription() != null) {
                ds.getWebhook().setDescription(request.getDescription());
            }
            dataSourceWebhookService.save(ds.getWebhook());
            var response = DataSourceWebhookResponse.builder()
                    .id(ds.getWebhook().getId())
                    .endpoint(ds.getWebhook().generateEndpoint())
                    .enabled(ds.getWebhook().isEnabled())
                    .description(ds.getWebhook().getDescription())
                    .properties(ds.getSchemaFields().stream()
                        .map(sf -> DataSourceSchemaFieldResponse.builder()
                            .id(sf.getId())
                            .name(sf.getName())
                            .description(sf.getDescription())
                            .dataType(sf.getDataType())
                            .createdAt(sf.getCreatedAt())
                            .updatedAt(sf.getUpdatedAt())
                            .build())
                        .toList())
                    .createdAt(ds.getWebhook().getCreatedAt())
                    .updatedAt(ds.getWebhook().getUpdatedAt())
                    .build();
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    // ── Field Management (nested under files) ────────────────────────────────

    @Operation(summary = "List field definitions for a specific file")
    @GetMapping("/{id}/files/{fileId}/fields")
    public ResponseEntity<List<DataSourceFieldResponse>> getFields(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "File ID") @PathVariable Long fileId) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        if (dataSourceFileService.findById(fileId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataSourceFieldService.findByDataSourceFileId(fileId).stream()
                .map(dataSourceFieldMapper::toResponse).toList());
    }

    @Operation(summary = "Add a field definition to a file")
    @ApiResponses({ @ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "404") })
    @PostMapping("/{id}/files/{fileId}/fields")
    public ResponseEntity<DataSourceFieldResponse> addField(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Valid @RequestBody DataSourceFieldRequest request) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        return dataSourceFileService.findById(fileId).map(file -> {
            var field = dataSourceFieldMapper.toEntity(request);
            field.setDataSourceFile(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(dataSourceFieldMapper.toResponse(dataSourceFieldService.save(field)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a field definition")
    @ApiResponses({ @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404") })
    @PutMapping("/{id}/files/{fileId}/fields/{fieldId}")
    public ResponseEntity<DataSourceFieldResponse> updateField(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "Field ID") @PathVariable Long fieldId,
            @Valid @RequestBody DataSourceFieldRequest request) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        if (dataSourceFileService.findById(fileId).isEmpty()) return ResponseEntity.notFound().build();
        return dataSourceFieldService.findById(fieldId).map(existing -> {
            dataSourceFieldMapper.updateEntity(request, existing);
            return ResponseEntity.ok(dataSourceFieldMapper.toResponse(dataSourceFieldService.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a field definition")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404") })
    @DeleteMapping("/{id}/files/{fileId}/fields/{fieldId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteField(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "File ID") @PathVariable Long fileId,
            @Parameter(description = "Field ID") @PathVariable Long fieldId) {
        if (dataSourceService.findById(id).isEmpty()
                || dataSourceFileService.findById(fileId).isEmpty()
                || dataSourceFieldService.findById(fieldId).isEmpty()) return ResponseEntity.notFound().build();
        dataSourceFieldService.deleteById(fieldId);
        return ResponseEntity.noContent().build();
    }

    // ── Archive / Purge ──────────────────────────────────────────────────────

    @Operation(summary = "List all archived data sources")
    @GetMapping("/archived")
    public List<DataSourceResponse> getArchived() {
        return dataSourceService.findArchived().stream().map(dataSourceMapper::toResponse).toList();
    }

    @Operation(summary = "Archive a data source")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404") })
    @PostMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> archive(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        if (dataSourceService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        dataSourceService.archiveById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Purge an archived data source")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404"),
                    @ApiResponse(responseCode = "409") })
    @DeleteMapping("/{id}/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> purge(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        var dsOpt = dataSourceService.findById(id);
        if (dsOpt.isEmpty()) return ResponseEntity.notFound().build();
        try {
            dataSourceService.purgeArchivedById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Purge all rows from the destination table",
               description = "Irreversible. Keeps the table structure.")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404") })
    @DeleteMapping("/{id}/destination-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> purgeDestinationData(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        return dataSourceService.findById(id)
                .<ResponseEntity<Void>>map(ds -> {
                    dataSourceTableService.deleteAllRecords(ds);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Drop the destination table entirely", description = "Irreversible.")
    @ApiResponses({ @ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404") })
    @DeleteMapping("/{id}/destination-table")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> dropDestinationTable(
            @Parameter(description = "Data source ID") @PathVariable Long id) {
        return dataSourceService.findById(id)
                .<ResponseEntity<Void>>map(ds -> {
                    dataSourceTableService.dropDestinationTable(ds);
                    dataSourceService.update(id, existing -> existing.setDestinationTable(null),
                            null, null, null);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
