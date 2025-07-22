package com.lit.ims.dto;

import lombok.Data;

@Data
public class WarehouseTransferLogFilterDTO {
    private String itemCode;
    private Long sourceWarehouseId;
}
