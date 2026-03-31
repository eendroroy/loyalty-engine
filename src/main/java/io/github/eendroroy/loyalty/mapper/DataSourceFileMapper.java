package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.DataSourceFileRequest;
import io.github.eendroroy.loyalty.dto.response.DataSourceFileResponse;
import io.github.eendroroy.loyalty.entity.DataSourceFile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class, uses = {DataSourceFieldMapper.class})
public interface DataSourceFileMapper {

    DataSourceFileResponse toResponse(DataSourceFile entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataSource", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DataSourceFile toEntity(DataSourceFileRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataSource", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DataSourceFileRequest request, @MappingTarget DataSourceFile entity);
}
