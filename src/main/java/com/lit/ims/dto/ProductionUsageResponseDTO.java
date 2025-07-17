package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductionUsageResponseDTO {
    private Long id;
    private String transactionNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm a")
    private LocalDateTime usageDate;

    private String workOrder;
    private String createdBy;
    private Long companyId;
    private Long branchId;

    private List<ProductionUsageItemDTO> items;
}
