package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.eendroroy.loyalty.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSourceWebhookPropertyResponse {
    private Long id;
    private String name;
    private String fieldAlias;
    private FieldDataType dataType;
    private String format;
    private String description;
}

