package com.lit.ims.dto;

import lombok.Data;

@Data
public class UpdateQcStatusDTO {
    private Long id;
    private String qcStatus;
    private String defectCategory;
    private String remarks;
    private Long warehouseId;
}
