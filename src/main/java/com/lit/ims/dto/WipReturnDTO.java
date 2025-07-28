package com.lit.ims.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WipReturnDTO {
    private String transactionNumber;
    private String returnType;
    private LocalDate returnDate;
    private String receiptNumber;
    private Long workOrderId;
    private Long warehouseId;
    private List<WipReturnItemDTO> returnItems;
}
