package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.eendroroy.loyalty.enums.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoucherResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private VoucherType voucherType;
    private Integer count;
    private Boolean active;
    private Boolean archived;
    private Long instanceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

