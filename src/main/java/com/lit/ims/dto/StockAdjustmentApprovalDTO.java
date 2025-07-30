package com.lit.ims.dto;

import lombok.Data;

@Data
public class StockAdjustmentApprovalDTO  {
    private String batchNo;
    private Integer requestedQty;
    private String reason;
}
