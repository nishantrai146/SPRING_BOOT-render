package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockAdjustment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchNo;
    private Integer oldQty;
    private Integer newQty;
    private Integer diff;
    private String reason;
    private String adjustedBy;
    private LocalDateTime timestamp;
    private Long companyId;
    private Long branchId;
}
