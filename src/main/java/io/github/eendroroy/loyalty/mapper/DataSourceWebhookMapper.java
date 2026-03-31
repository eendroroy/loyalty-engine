package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.DataSourceWebhookRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceWebhookResponse;
import io.github.eendroroy.loyalty.entity.DataSourceWebhook;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for DataSourceWebhook.
 *
 * <p>The webhook endpoint is auto-generated from the parent data source name
 * and is not mapped from the request. Webhook properties are automatically
 * kept in sync with the parent data source's schema fields via service logic.
 */
@Mapper(config = MapStructConfig.class, uses = {DataSourceSchemaFieldMapper.class})
public interface DataSourceWebhookMapper {

    /**
     * Converts entity to response. The endpoint is generated via
     * {@link DataSourceWebhook#generateEndpoint()}, and properties are
     * mapped from the parent data source's schema fields.
     */
    @Mapping(target = "endpoint", ignore = true)  // populated separately
    @Mapping(target = "properties", ignore = true)  // populated separately
    DataSourceWebhookResponse toResponse(DataSourceWebhook entity);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "dataSource", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    DataSourceWebhook toEntity(DataSourceWebhookRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "dataSource", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateEntity(DataSourceWebhookRequest request, @MappingTarget DataSourceWebhook entity);
}
