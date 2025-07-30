package com.lit.ims.dto;

import lombok.Data;

@Data
public class AdjustmentRequestDTO {
    private String itemCode;
    private Integer adjustedQuantity;
    private String reason;
}
