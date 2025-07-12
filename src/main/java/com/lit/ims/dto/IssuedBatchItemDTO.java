package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuedBatchItemDTO {
    private String itemCode;
    private String itemName;
    private String batchNo;
    private Double quantity;
    private Double issuedQty;
    private Double variance;
}
