package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldMetadataResponse {
    /** The alias used in rule expressions, e.g. {@code transaction.amount}. */
    private String alias;
    /** The raw column / field name in the source. */
    private String fieldName;
    private FieldDataType dataType;
    /** 1-based column index; present for FILE sources only. */
    private Integer columnNumber;
    /** Human-readable description surfaced as a hint in the rule-expression editor. */
    private String description;
}
