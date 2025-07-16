package com.lit.ims.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionReceiptTableDTO {
    private String transactionNumber;
    private LocalDate receiptDate;
    private String type;
    private List<ItemCodeNameDTO> items;
    private String status;
}
