package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.DataSourceRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceResponse;
import io.github.eendroroy.loyalty.dto.response.DataSourceSchemaFieldResponse;
import io.github.eendroroy.loyalty.dto.response.DataSourceWebhookResponse;
import io.github.eendroroy.loyalty.entity.DataSource;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class,
        uses = {DataSourceFileMapper.class, DataSourceWebhookMapper.class,
                DataSourceSchemaFieldMapper.class})
public interface DataSourceMapper {

    @Mapping(target = "fields", source = "schemaFields")
    @Mapping(target = "webhook", ignore = true)  // handled by custom logic
    DataSourceResponse toResponse(DataSource entity);

    default DataSourceResponse toResponseWithWebhook(DataSource entity) {
        var base = toResponse(entity);
        var whResponse = entity.getWebhook() != null
                ? toWebhookResponse(entity.getWebhook(), entity.getSchemaFields())
                : null;
        return DataSourceResponse.builder()
                .id(base.getId())
                .name(base.getName())
                .description(base.getDescription())
                .fields(base.getFields())
                .files(base.getFiles())
                .webhook(whResponse)
                .destinationTable(base.getDestinationTable())
                .createdAt(base.getCreatedAt())
                .updatedAt(base.getUpdatedAt())
                .archived(base.isArchived())
                .build();
    }

    private DataSourceWebhookResponse toWebhookResponse(
            io.github.eendroroy.loyalty.entity.DataSourceWebhook webhook,
            java.util.List<io.github.eendroroy.loyalty.entity.DataSourceSchemaField> schemaFields) {
        if (webhook == null) return null;
        
        java.util.List<DataSourceSchemaFieldResponse> schemaFieldResponses = schemaFields == null 
            ? java.util.List.of() 
            : schemaFields.stream()
                .map(sf -> DataSourceSchemaFieldResponse.builder()
                    .id(sf.getId())
                    .name(sf.getName())
                    .description(sf.getDescription())
                    .dataType(sf.getDataType())
                    .createdAt(sf.getCreatedAt())
                    .updatedAt(sf.getUpdatedAt())
                    .build())
                .toList();
        
        return DataSourceWebhookResponse.builder()
            .id(webhook.getId())
            .endpoint(webhook.generateEndpoint())
            .enabled(webhook.isEnabled())
            .description(webhook.getDescription())
            .properties(schemaFieldResponses)
            .createdAt(webhook.getCreatedAt())
            .updatedAt(webhook.getUpdatedAt())
            .build();
    }

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "schemaFields", ignore = true)
    @Mapping(target = "files",        ignore = true)
    @Mapping(target = "webhook",      ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "archived",     ignore = true)
    @Mapping(target = "allFields",    ignore = true)
    DataSource toEntity(DataSourceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "schemaFields", ignore = true)
    @Mapping(target = "files",        ignore = true)
    @Mapping(target = "webhook",      ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "archived",     ignore = true)
    @Mapping(target = "allFields",    ignore = true)
    void updateEntity(DataSourceRequest request, @MappingTarget DataSource entity);
}
