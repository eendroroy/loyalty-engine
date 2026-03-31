package io.github.eendroroy.loyalty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "A file path to watch for ingestion, with its own field schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DataSourceFileRequest {

    @Schema(description = "Absolute path to the file or directory to watch", example = "/data/txn/feed.csv")
    @NotBlank(message = "File path is required")
    private String filePath;

    @Schema(description = "Optional note about this file path", example = "Daily transaction export")
    private String description;

    @Schema(description = "CSV field delimiter character. Defaults to comma if blank.", example = ",")
    private String fieldSeparator;

    @Schema(description = "CSV quote character for fields containing the delimiter. Defaults to double-quote if blank.",
            example = "\"")
    private String quoteCharacter;

    @Schema(description = "Line separator string (informational; auto-detected during parsing).", example = "\\n")
    private String lineSeparator;

    @Schema(description = "Number of leading lines to skip before the header row. Use when files have preamble lines.",
            example = "0")
    private Integer skipFirstNLines;

    @Schema(description = "Field schema definitions for this file. "
            + "On PUT replaces all existing fields when non-null.")
    @Valid
    private List<DataSourceFieldRequest> fields;
}
