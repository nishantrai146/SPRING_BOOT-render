package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmReceiptDTO {
    private String transactionNumber;
    private String issueNumber;
    private String requisitionNumber;
    private LocalDate issueDate;
    private LocalDate receiptDate;
    private List<ConfirmReceiptItemDTO> items;

}
