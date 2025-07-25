package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductionReceiptItemDTO {
    private String itemCode;
    private String itemName;
    private String batchNumber;
    private Double issuedQuantity;
    private Double receivedQuantity;
    private Double variance;
    private String note;
}
