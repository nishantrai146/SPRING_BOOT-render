package com.lit.ims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "production_usage")
public class ProductionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionNumber;
    private String workOrder;
    private LocalDateTime usageDate;
    private Long companyId;
    private Long branchId;
    @OneToMany(mappedBy = "productionUsage", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProductionUsageItem> items=new ArrayList<>();
}
