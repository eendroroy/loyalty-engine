package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.RuleActionRequest;
import io.github.eendroroy.loyalty.dto.response.RuleActionResponse;
import io.github.eendroroy.loyalty.entity.RuleAction;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface RuleActionMapper {

    RuleActionResponse toResponse(RuleAction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rule", ignore = true)
    RuleAction toEntity(RuleActionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rule", ignore = true)
    void updateEntity(RuleActionRequest request, @MappingTarget RuleAction entity);
}

