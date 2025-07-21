package com.lit.ims.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StockTransactionLogFilterDTO {
    private String itemCode;
    private Long warehouseId;
    private String transactionType; // INCREASE / DECREASE
    private LocalDate fromDate;
    private LocalDate toDate;
}
