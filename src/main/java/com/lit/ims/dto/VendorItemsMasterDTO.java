package com.lit.ims.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorItemsMasterDTO {

    private Long id;

    private String vendorCode;
    private String vendorName;

    private String itemCode;
    private String itemName;

    private Integer days;
    private Integer quantity;
    private Double price;

    private String status;
}
