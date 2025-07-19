package com.lit.ims.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private Long id;
    private String name;
    private String code;
    private String uom;
    private String groupName;
    private String status;
    private Double price;
    private Integer stQty;
    private Integer life;
    private boolean isInventoryItem;
    private boolean isIqc;
}
