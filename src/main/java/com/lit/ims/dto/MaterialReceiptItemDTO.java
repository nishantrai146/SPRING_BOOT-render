package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReceiptItemDTO {
    private String vendorCode;
    private String vendorName;
    private String itemName;
    private String itemCode;
    private Integer quantity;
    private String batchNo;
    private boolean isIssued;
    private Long warehouseId;
    private Boolean isInventory;
    private Boolean isIqc;

}
