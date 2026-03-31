package io.github.eendroroy.loyalty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class VoucherInstanceResponse {
    private Long id;
    private Long voucherId;
    private String voucherName;
    private String voucherCode;
    private String secretCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

