package io.github.eendroroy.loyalty.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A single row ingested from a {@link io.github.eendroroy.loyalty.entity.DataSource}.
 *
 * <p>Each entry in {@link #fields} is keyed by the {@code fieldAlias} of the
 * corresponding {@link io.github.eendroroy.loyalty.entity.DataSourceField}
 * and holds a type-coerced value ({@code Long}, {@link java.math.BigDecimal},
 * {@link java.time.LocalDate}, {@code Boolean}, or {@code String}).
 *
 * @param dataSourceId  ID of the originating {@code DataSource}
 * @param sourceName    human-readable name of the originating source
 * @param ingestedAt    wall-clock time at which this record was produced
 * @param fields        alias → typed-value map for this row
 */
public record IngestedRecord(
        Long dataSourceId,
        String sourceName,
        LocalDateTime ingestedAt,
        Map<String, Object> fields) {
}

