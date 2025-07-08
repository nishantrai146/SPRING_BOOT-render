package com.lit.ims.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequisitionSummaryDTO {
    private Long id;
    private String transactionNumber;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
