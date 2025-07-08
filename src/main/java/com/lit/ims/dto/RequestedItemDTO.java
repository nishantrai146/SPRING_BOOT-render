package com.lit.ims.dto;

import lombok.Data;

@Data
public class RequestedItemDTO {
    private String name;
    private String code;
    private String type;
    private Integer quantity;
}
