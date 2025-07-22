package com.lit.ims.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarehouseTransferLogDTO {
    private String trNo;
    private String itemCode;
    private String itemName;
    private Integer quantity;

    private Long sourceWarehouseId;
    private String sourceWarehouseName;

    private Long targetWarehouseId;
    private String targetWarehouseName;

    private String transferType;
    private String referenceType;
    private Long referenceId;

    private String transferredBy;
    private LocalDateTime transferredAt;
}
