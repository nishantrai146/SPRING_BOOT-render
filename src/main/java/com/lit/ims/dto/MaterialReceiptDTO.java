package com.lit.ims.dto;

import lombok.Data;

import java.util.List;

@Data
public class MaterialReceiptDTO {
    private String mode;
    private  String vendor;
    private String vendorCode;
    private List<MaterialReceiptItemDTO> items;
}
