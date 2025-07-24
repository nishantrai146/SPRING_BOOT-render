package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RequisitionSummaryDTO {
    private Long id;
    private String transactionNumber;
    private String type;
    private String status;
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss a")
    private LocalDateTime createdAt;
    private Long warehouseId;

    private List<RequestedItemDTO> items;
}
