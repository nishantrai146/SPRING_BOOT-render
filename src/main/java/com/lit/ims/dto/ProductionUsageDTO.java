package com.lit.ims.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionUsageDTO {
    private String transactionNumber;
    private String workOrder;
    private LocalDateTime usageDate;
    private List<ProductionUsageItemDTO> items;

}
