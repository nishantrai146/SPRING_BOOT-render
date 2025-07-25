package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiptIdNumberDTO {
    private Long id;
    private String transactionNumber;
}
