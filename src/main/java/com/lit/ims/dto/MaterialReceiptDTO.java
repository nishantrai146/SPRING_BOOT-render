package com.lit.ims.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MaterialReceiptDTO {
    private String mode;
    private  String vendor;
    private String vendorCode;
    private List<MaterialReceiptItemDTO> items;
}
