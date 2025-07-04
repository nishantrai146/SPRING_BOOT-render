package com.lit.ims.dto;

import lombok.Data;

@Data
public class MaterialReceiptItemDTO {
    private String name;
    private String code;
    private Integer quantity;
    private String batchNo;
}
