package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="production_usage_items")
public class ProductionUsageItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemCode;
    private String itemName;
    private String batchNumber;

    private Integer availableQty;
    private Integer usedQty;
    private Integer scrapQty;
    private Integer remainingQty;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_usage_id")
    private ProductionUsage productionUsage;

}
