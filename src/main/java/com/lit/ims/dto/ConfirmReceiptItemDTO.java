package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmReceiptItemDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String batchNumber;
    private Double issuedQty;
    private Double receivedQty;
    private Double variance;
    private String notes;

}
