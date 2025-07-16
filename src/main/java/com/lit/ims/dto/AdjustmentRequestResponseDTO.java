package com.lit.ims.dto;

import com.lit.ims.entity.AdjustmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdjustmentRequestResponseDTO {

    private Long id;
    private String batchNo;
    private Integer oldQty;
    private Integer requestedQty;
    private Integer diff;
    private String reason;
    private AdjustmentStatus status;
    private String requestedBy;
    private LocalDateTime requestedAt;

    private String itemCode;
    private String itemName;
}
