package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.DataSourceSchemaFieldRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceSchemaFieldResponse;
import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface DataSourceSchemaFieldMapper {

    DataSourceSchemaFieldResponse toResponse(DataSourceSchemaField entity);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "dataSource", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    DataSourceSchemaField toEntity(DataSourceSchemaFieldRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "dataSource", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateEntity(DataSourceSchemaFieldRequest request, @MappingTarget DataSourceSchemaField entity);
}

