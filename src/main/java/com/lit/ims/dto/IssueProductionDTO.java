package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueProductionDTO {
    private Long id;
    private String issueNumber;
    private String requisitionNumber;

    private LocalDateTime issueDate;

    private String createdBy;
    private Long destinationWarehouseId;

    private List<IssuedBatchItemDTO> batchItems;
}
