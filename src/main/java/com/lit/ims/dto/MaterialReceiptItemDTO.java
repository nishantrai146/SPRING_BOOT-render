package com.lit.ims.dto;

import lombok.Data;

@Data
public class MaterialReceiptItemDTO {
    private String itemName;
    private String itemCode;
    private Integer quantity;
    private String batchNo;
    private boolean isIssued;
}
