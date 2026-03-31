package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.dto.response.MetadataResponse;
import io.github.eendroroy.loyalty.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the field-alias registry for the rule-expression editor.
 *
 * <p>Base path: {@code /api/admin/metadata}
 *
 * <p>The metadata endpoint aggregates all configured {@code DataSourceField} aliases,
 * their data types, and their parent source names into a single response used by the
 * React admin panel to provide autocomplete suggestions in the rule-expression editor.
 */
@Tag(name = "Metadata", description = "Field-alias registry for rule-expression autocomplete")
@RestController
@RequestMapping("/api/admin/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    /**
     * Returns all configured data-source field aliases with their data types and
     * parent source information. Used by the rule-expression editor for autocomplete.
     *
     * @return aggregated metadata containing all field aliases grouped by data source
     */
    @Operation(summary = "Get field-alias metadata",
               description = "Returns all field aliases grouped by data source. "
                             + "Used by the rule-expression editor for autocomplete suggestions.")
    @GetMapping
    public MetadataResponse getMetadata() {
        return metadataService.getMetadata();
    }
}
