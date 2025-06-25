package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private String name;
    private String code;
    private String uom;
    private String type;
    private String barcode;
    private String group;
    private String status;
    private Double price;
    private Integer st_qty;
    private Integer life;
}
