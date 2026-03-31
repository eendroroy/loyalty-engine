package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSourceFileResponse {
    private Long id;
    private String filePath;
    private String description;
    private String fieldSeparator;
    private String quoteCharacter;
    private String lineSeparator;
    private Integer skipFirstNLines;
    private List<DataSourceFieldResponse> fields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
