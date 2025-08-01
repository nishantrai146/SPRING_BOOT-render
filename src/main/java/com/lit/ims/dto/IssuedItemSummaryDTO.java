package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedItemSummaryDTO {
    private Long id;

    private String itemCode;
    private String itemName;
    private Double totalIssued;
    private Double totalVariance;
    private String batchNumber;
}
