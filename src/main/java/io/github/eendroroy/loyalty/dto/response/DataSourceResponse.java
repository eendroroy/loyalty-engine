package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSourceResponse {
    private Long id;
    private String name;
    private String description;
    private List<DataSourceSchemaFieldResponse> fields;
    private List<DataSourceFileResponse> files;
    private DataSourceWebhookResponse webhook;  // singular: one webhook per data source
    private String destinationTable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean archived;
}
