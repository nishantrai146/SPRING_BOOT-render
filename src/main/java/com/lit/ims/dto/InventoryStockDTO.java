package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockDTO {
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private Long warehouseId;
    private String warehouseName;
}
