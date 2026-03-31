package io.github.eendroroy.loyalty.mapper;

import io.github.eendroroy.loyalty.dto.request.VoucherRequest;
import io.github.eendroroy.loyalty.dto.response.VoucherInstanceResponse;
import io.github.eendroroy.loyalty.dto.response.VoucherResponse;
import io.github.eendroroy.loyalty.entity.Voucher;
import io.github.eendroroy.loyalty.entity.VoucherInstance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface VoucherMapper {

    @Mapping(target = "instanceCount", ignore = true)
    VoucherResponse toResponse(Voucher entity);

    @Mapping(target = "voucherId",   source = "voucher.id")
    @Mapping(target = "voucherName", source = "voucher.name")
    @Mapping(target = "voucherCode", source = "voucher.code")
    VoucherInstanceResponse toInstanceResponse(VoucherInstance entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "archived",  ignore = true)
    @Mapping(target = "instances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Voucher toEntity(VoucherRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "archived",  ignore = true)
    @Mapping(target = "instances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(VoucherRequest request, @MappingTarget Voucher entity);
}

