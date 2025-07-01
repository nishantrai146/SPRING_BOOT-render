package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomItemDTO {
    private Long itemId;
    private String itemName;
    private String itemCode;
    private String uom;
    private Double quantity;
    private Long warehouseId;
    private String warehouseName;

}
