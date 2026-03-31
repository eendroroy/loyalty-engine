package io.github.eendroroy.loyalty.dto.request;

import io.github.eendroroy.loyalty.enums.VoucherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating or updating a {@link io.github.eendroroy.loyalty.entity.Voucher}.
 */
@Schema(description = "Payload for creating or updating a voucher")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRequest {

    @Schema(description = "Human-readable voucher name", example = "Summer Discount 2025")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Public voucher code used to identify this template", example = "SUMMER25")
    @NotBlank(message = "Code is required")
    private String code;

    @Schema(description = "Optional description of the voucher's purpose or terms",
            example = "25% discount on all orders above £50 during summer 2025")
    private String description;

    @Schema(description = "Category of voucher", example = "DISCOUNT")
    @Builder.Default
    private VoucherType voucherType = VoucherType.DISCOUNT;

    @Schema(description = "Maximum number of secret-code instances that may be generated", example = "500")
    @NotNull(message = "Count is required")
    @Positive(message = "Count must be a positive number")
    private Integer count;

    @Schema(description = "Whether this voucher is currently active and can be awarded", example = "true")
    @Builder.Default
    private Boolean active = true;
}

