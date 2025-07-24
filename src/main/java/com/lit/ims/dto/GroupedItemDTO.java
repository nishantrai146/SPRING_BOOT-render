package com.lit.ims.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupedItemDTO {
    private String code;
    private String name;
    private String uom;
    private String group;
    private Integer quantityRequested;
    private Integer stQuantity;
}
