package com.lit.ims.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateAdjustmentRequestDTO {

    @NotBlank
    private String batchNo;

    @PositiveOrZero
    private Integer requestedQty;

    @NotBlank
    private String reason;
}
