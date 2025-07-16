package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustment_requests")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockAdjustmentRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchNo;
    private Integer oldQty;
    private Integer requestedQty;
    private Integer diff;
    private String reason;

    @Enumerated(EnumType.STRING)
    private AdjustmentStatus status;

    private String requestedBy;
    private String approvedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    private Long companyId;
    private Long branchId;
}
