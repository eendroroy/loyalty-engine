package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.RuleRequest;
import io.github.eendroroy.loyalty.dto.response.RuleResponse;
import io.github.eendroroy.loyalty.entity.Rule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class, uses = RuleActionMapper.class)
public interface RuleMapper {

    RuleResponse toResponse(Rule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actions", ignore = true)
    @Mapping(target = "lastRunAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rule toEntity(RuleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actions", ignore = true)
    @Mapping(target = "lastRunAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RuleRequest request, @MappingTarget Rule entity);
}

