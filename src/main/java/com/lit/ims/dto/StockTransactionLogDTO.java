package com.lit.ims.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockTransactionLogDTO {
    private String itemCode;
    private String itemName;
    private String transactionType;
    private int quantityChanged;
    private String referenceType;
    private Long referenceId;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime transactionDate;
    private String remarks;
}
