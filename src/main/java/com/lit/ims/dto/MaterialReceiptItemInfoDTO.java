package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReceiptItemInfoDTO {
    private String vendorCode;
    private String vendorName;
    private String itemCode;
    private String itemName;
}
