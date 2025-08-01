package com.lit.ims.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductionUsageItemDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String batchNumber;
    private Integer availableQuantity;
    private Integer usedQty;
    private Integer scrapQty;
    private Integer remainingQty;
    private String status;
}
