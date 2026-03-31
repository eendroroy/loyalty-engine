package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.DataSourceWebhookPropertyRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceWebhookPropertyResponse;
import io.github.eendroroy.loyalty.entity.DataSourceWebhookProperty;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface DataSourceWebhookPropertyMapper {

    DataSourceWebhookPropertyResponse toResponse(DataSourceWebhookProperty entity);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "webhook", ignore = true)
    DataSourceWebhookProperty toEntity(DataSourceWebhookPropertyRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "webhook", ignore = true)
    void updateEntity(DataSourceWebhookPropertyRequest request,
                      @MappingTarget DataSourceWebhookProperty entity);
}

