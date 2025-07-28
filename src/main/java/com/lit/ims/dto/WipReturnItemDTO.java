package com.lit.ims.dto;

import lombok.Data;

@Data
public class WipReturnItemDTO {
    private Long itemCode;
    private String itemName;
    private String batchNo;
    private Integer originalQty;
    private Integer returnQty;
    private String returnReason;
}
