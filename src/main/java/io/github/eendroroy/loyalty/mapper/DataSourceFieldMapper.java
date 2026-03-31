package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.DataSourceFieldRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceFieldResponse;
import io.github.eendroroy.loyalty.entity.DataSourceField;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface DataSourceFieldMapper {

    DataSourceFieldResponse toResponse(DataSourceField entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataSourceFile", ignore = true)
    DataSourceField toEntity(DataSourceFieldRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataSourceFile", ignore = true)
    void updateEntity(DataSourceFieldRequest request, @MappingTarget DataSourceField entity);
}
