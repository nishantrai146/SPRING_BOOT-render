package com.lit.ims.dto;

import lombok.Data;

import java.util.List;

@Data
public class MaterialRequisitionDTO {
    private String type;
    private String transactionNumber;
    private Long warehouseId;
    private List<RequestedItemDTO> items;
}
