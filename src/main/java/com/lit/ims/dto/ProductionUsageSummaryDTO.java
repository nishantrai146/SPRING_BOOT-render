package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductionUsageSummaryDTO {
    private Long id;
    private String transactionNumber;
    private String workOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm a")
    private LocalDateTime usageDate;
}
